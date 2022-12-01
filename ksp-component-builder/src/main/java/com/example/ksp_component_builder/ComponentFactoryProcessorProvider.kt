package com.example.ksp_component_builder

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class ComponentFactoryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("""
            ComponentFactoryProcessorProvider.create
             |kotlinVersion: ${environment.kotlinVersion}
             |apiVersion: ${environment.apiVersion}
             |compilerVersion: ${environment.compilerVersion}
             |platforms: ${environment.platforms}
             |options: ${environment.options}
        """.trimIndent())

        return ComponentFactoryProcessor(
            logger = environment.logger,
            fileGenerator = ComponentFactoryFileGenerator(
                codeGenerator = environment.codeGenerator,
            )
        )
    }
}