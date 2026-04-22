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
import com.google.devtools.ksp.validate
import io.github.sgpublic.dormnet.targets.core.DormnetTarget
import io.github.sgpublic.dormnet.targets.core.DormnetTargetEntry

class DormnetTargetProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private var generated = false

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
                    qualifiedName = qualifiedName,
                    expression = if (declaration.classKind == ClassKind.OBJECT) {
                        qualifiedName
                    } else {
                        "$qualifiedName()"
                    },
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

        generated = true
        return emptyList()
    }

    private fun KSClassDeclaration.isDormnetTarget(dormnetTargetType: KSType): Boolean {
        val targetType = asStarProjectedType()
        return dormnetTargetType.isAssignableFrom(targetType)
    }

    private fun buildRegistryFile(targets: List<TargetEntry>): String {
        val entries = if (targets.isEmpty()) {
            ""
        } else {
            targets.joinToString(separator = ",\n") { target ->
                "        ${target.expression}"
            } + ",\n"
        }

        return """
            |package $REGISTRY_PACKAGE
            |
            |public object $REGISTRY_NAME {
            |    public val all: List<DormnetTarget<out LoginParams>> = listOf(
            |$entries
            |    )
            |}
            |
        """.trimMargin()
    }

    private data class TargetEntry(
        val qualifiedName: String,
        val expression: String,
    )

    private companion object {
        val DORMNET_TARGET_ENTRY = DormnetTargetEntry::class.java.name
        val DORMNET_TARGET = DormnetTarget::class.java.name
        val REGISTRY_PACKAGE = DormnetTarget::class.java.packageName
        val REGISTRY_NAME = "DormnetTargetRegistry"
    }
}
