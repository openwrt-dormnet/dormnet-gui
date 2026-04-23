package io.github.sgpublic.dormnet.targets.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DormnetTargetProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DormnetTargetProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            maintainersOutput = environment.options["dormnet.maintainersOutput"],
            targetStrings = environment.options["dormnet.targetStrings"],
        )
    }
}
