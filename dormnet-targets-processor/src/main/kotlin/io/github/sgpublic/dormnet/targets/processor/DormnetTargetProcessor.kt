package io.github.sgpublic.dormnet.targets.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.validate
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class DormnetTargetProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val maintainersOutput: String?,
    targetStrings: String?,
) : SymbolProcessor {
    private var generated = false
    private val stringResources = targetStrings
        ?.takeIf { it.isNotBlank() }
        ?.let { loadStringResources(File(it)) }
        .orEmpty()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) {
            return emptyList()
        }

        val symbols = resolver
            .getSymbolsWithAnnotation(DORMNET_TARGET_ENTRY)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        val invalidSymbols = symbols.filterNot { it.validate() }
        if (invalidSymbols.isNotEmpty()) {
            return invalidSymbols
        }

        val dormnetTargetType = resolver
            .getClassDeclarationByName(resolver.getKSNameFromString(DORMNET_TARGET))
            ?.asStarProjectedType()
        if (dormnetTargetType == null) {
            logger.error("Unable to resolve DormnetTarget.")
            return symbols
        }

        val targets = symbols.mapNotNull { declaration ->
            val qualifiedName = declaration.qualifiedName?.asString()
            when {
                qualifiedName == null -> {
                    logger.error("@DormnetTargetEntry requires a named class or object.", declaration)
                    null
                }
                declaration.classKind != ClassKind.CLASS && declaration.classKind != ClassKind.OBJECT -> {
                    logger.error("@DormnetTargetEntry can only be used on Kotlin class or object declarations.", declaration)
                    null
                }
                !declaration.isDormnetTarget(dormnetTargetType) -> {
                    logger.error("@DormnetTargetEntry declaration must extend DormnetTarget<*>.", declaration)
                    null
                }
                declaration.classKind == ClassKind.CLASS && Modifier.ABSTRACT in declaration.modifiers -> {
                    logger.error("@DormnetTargetEntry class must not be abstract.", declaration)
                    null
                }
                declaration.classKind == ClassKind.CLASS && declaration.primaryConstructor
                    ?.parameters
                    ?.isNotEmpty() == true -> {
                    logger.error("@DormnetTargetEntry class must have a no-arg constructor.", declaration)
                    null
                }
                else -> TargetEntry(
                    name = declaration.simpleName.asString().toEnumEntryName(),
                    displayName = declaration.displayName(),
                    qualifiedName = qualifiedName,
                    expression = if (declaration.classKind == ClassKind.OBJECT) {
                        qualifiedName
                    } else {
                        "$qualifiedName()"
                    },
                    maintainers = declaration.maintainers(),
                )
            }
        }.sortedBy { it.qualifiedName }

        val dependencies = Dependencies(
            aggregating = true,
            sources = symbols.mapNotNull { it.containingFile }.toTypedArray(),
        )
        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = REGISTRY_PACKAGE,
            fileName = REGISTRY_NAME,
        ).bufferedWriter().use { writer ->
            writer.write(buildRegistryFile(targets))
        }
        generateMaintainersFile(targets)

        generated = true
        return emptyList()
    }

    private fun KSClassDeclaration.isDormnetTarget(dormnetTargetType: KSType): Boolean {
        val targetType = asStarProjectedType()
        return dormnetTargetType.isAssignableFrom(targetType)
    }

    private fun KSClassDeclaration.displayName(): String {
        val resourceName = titleResourceName()
        return resourceName?.let { stringResources[it] } ?: simpleName.asString()
    }

    private fun KSClassDeclaration.titleResourceName(): String? {
        val location = location as? FileLocation ?: return null
        return runCatching {
            File(location.filePath)
                .readLines()
                .drop(location.lineNumber - 1)
                .firstNotNullOfOrNull { line ->
                    TITLE_RESOURCE_REGEX.find(line)?.groupValues?.get(1)
                }
        }.getOrNull()
    }

    private fun KSClassDeclaration.maintainers(): List<String> {
        return annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == DORMNET_TARGET_ENTRY
            }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "maintainer" }
            ?.value
            .let { value ->
                when (value) {
                    is List<*> -> value.filterIsInstance<String>()
                    is Array<*> -> value.filterIsInstance<String>()
                    is String -> listOf(value)
                    else -> emptyList()
                }
            }
    }

    private fun loadStringResources(file: File): Map<String, String> {
        if (!file.isFile) {
            logger.warn("Dormnet target strings file does not exist: ${file.absolutePath}")
            return emptyMap()
        }

        return runCatching {
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)
            val strings = document.getElementsByTagName("string")
            buildMap {
                for (index in 0 until strings.length) {
                    val node = strings.item(index)
                    val name = node.attributes?.getNamedItem("name")?.nodeValue ?: continue
                    put(name, node.textContent)
                }
            }
        }.getOrElse { error ->
            logger.warn("Unable to parse Dormnet target strings file: ${error.message}")
            emptyMap()
        }
    }

    private fun buildRegistryFile(targets: List<TargetEntry>): String {
        val entries = if (targets.isEmpty()) {
            ""
        } else {
            targets.joinToString(separator = ",\n") { target ->
                "    ${target.name}(${target.expression})"
            } + ";\n"
        }

        return """
            |package $REGISTRY_PACKAGE
            |
            |public enum class $REGISTRY_NAME(
            |    public val impl: DormnetTarget<out LoginParams>,
            |) {
            |$entries
            |    public companion object {
            |        public val all: List<DormnetTarget<out LoginParams>> = entries.map { it.impl }
            |    }
            |}
            |
        """.trimMargin()
    }

    private fun generateMaintainersFile(targets: List<TargetEntry>) {
        val output = maintainersOutput?.takeIf { it.isNotBlank() } ?: return
        val outputFile = File(output)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(buildMaintainersFile(targets))
    }

    private fun buildMaintainersFile(targets: List<TargetEntry>): String {
        val rows = targets.map { target ->
            val maintainers = target.maintainers.joinToString(separator = ", ") { "@$it" }
            "| ${target.displayName} | $maintainers |"
        }

        return listOf(
            "# 学校维护者",
            "",
            "> 此文件由 KSP 自动生成，请修改 `@DormnetTargetEntry` 中的 `maintainer` 字段。",
            "",
            "| 学校 | 维护者 |",
            "| --- | --- |",
        ).plus(rows)
            .joinToString(separator = "\n", postfix = "\n")
    }

    private fun String.toEnumEntryName(): String {
        return fold(StringBuilder()) { result, char ->
            when {
                char.isLetterOrDigit() -> result.append(char.uppercaseChar())
                result.lastOrNull() != '_' -> result.append('_')
                else -> result
            }
        }.toString()
            .trim('_')
            .let { name ->
                when {
                    name.isEmpty() -> "TARGET"
                    name.first().isDigit() -> "_$name"
                    else -> name
                }
            }
    }

    private data class TargetEntry(
        val name: String,
        val displayName: String,
        val qualifiedName: String,
        val expression: String,
        val maintainers: List<String>,
    )

    private companion object {
        val DORMNET_TARGET_ENTRY = DormnetTargetEntry::class.java.name
        val DORMNET_TARGET = DormnetTarget::class.java.name
        val REGISTRY_PACKAGE = DormnetTarget::class.java.packageName
        val REGISTRY_NAME = "DormnetTargetRegistry"
        val TITLE_RESOURCE_REGEX = Regex("""\btitle\s*:[^=]*=\s*Res\.string\.([A-Za-z_][A-Za-z0-9_]*)""")
    }
}
