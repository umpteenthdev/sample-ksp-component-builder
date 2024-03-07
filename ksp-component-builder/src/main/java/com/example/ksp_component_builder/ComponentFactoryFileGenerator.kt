package com.example.ksp_component_builder

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal class ComponentFactoryFileGenerator(
    private val codeGenerator: CodeGenerator,
) {

    fun generateFile(componentData: ComponentData) {
        val fileSpec = getFileSpec(componentData)
        write(fileSpec, componentData.containingFile)
    }

    private fun getFileSpec(componentData: ComponentData): FileSpec {
        val componentType = componentData.componentDeclaration.asType(emptyList())
        val componentFactorySpec = getFactoryObjectSpec(
            componentType = componentType,
            requiredModules = componentData.annotationData.requiredModules,
            providableDependencies = componentData.annotationData.providableDependencies,
            requiredDependencies = componentData.annotationData.requiredDependencies,
        )

        return FileSpec.builder(
            packageName = componentData.containingFile.packageName.asString(),
            fileName = "${componentType.declaration.simpleName.asString()}Factory"
        )
            .indent("    ")
            .addImport("com.example.core", "DependenciesProvider")
            .addImport("com.example.core", "provide")
            .addType(componentFactorySpec.componentFactoryObjectSpec)
            .build()
    }

    private fun getFactoryObjectSpec(
        componentType: KSType,
        requiredModules: List<ComponentAnnotationData.RequiredModule>,
        providableDependencies: List<KSType>,
        requiredDependencies: List<KSType>,
    ): Spec {
        val providableDependenciesParameterSpecs = mutableListOf<ParameterSpec>()
        val requiredDependenciesParameterSpecs = mutableListOf<ParameterSpec>()

        requiredDependenciesParameterSpecs += getRequiredDependenciesParameterSpecs(requiredDependencies)
        requiredDependenciesParameterSpecs += getRequiredModulesParameterSpecs(requiredModules)
        providableDependenciesParameterSpecs += getProvidableDependenciesParameterSpecs(providableDependencies)
        val constructorParameterSpecs = requiredDependenciesParameterSpecs + providableDependenciesParameterSpecs

        val createComponentFunSpec = getCreateComponentFactoryFunSpec(
            componentType = componentType,
            constructorParameterSpecs = constructorParameterSpecs,
        )

        val componentFactoryObjectSpec = getComponentFactoryObjectSpec(
            componentType = componentType,
            createComponentFunSpec = createComponentFunSpec,
        )

        return Spec(
            providableDependenciesParameterSpecs = providableDependenciesParameterSpecs,
            requiredDependenciesParameterSpecs = requiredDependenciesParameterSpecs,
            componentFactoryObjectSpec = componentFactoryObjectSpec,
        )
    }

    private fun getRequiredModulesParameterSpecs(
        requiredModules: List<ComponentAnnotationData.RequiredModule>,
    ): List<ParameterSpec> {
        val constructorParameterSpecs = mutableListOf<ParameterSpec>()
        val sortedModules = requiredModules.sortedBy { it.hasDefaultConstructor }

        for (module in sortedModules) {
            constructorParameterSpecs += getConstructorParameterSpec(module.type) { builder ->
                if (module.hasDefaultConstructor) {
                    builder.defaultValue("${module.type.declaration.simpleName.asString()}()")
                }
            }
        }

        return constructorParameterSpecs
    }

    private fun getRequiredDependenciesParameterSpecs(
        requiredDependencies: List<KSType>,
    ): List<ParameterSpec> {
        val constructorParameterSpecs = mutableListOf<ParameterSpec>()
        for (type in requiredDependencies) {
            constructorParameterSpecs += getConstructorParameterSpec(type)
        }
        return constructorParameterSpecs
    }

    private fun getProvidableDependenciesParameterSpecs(types: List<KSType>): List<ParameterSpec> {
        val constructorParameterSpecs = mutableListOf<ParameterSpec>()
        for (type in types) {
            constructorParameterSpecs += getConstructorParameterSpec(type) { builder ->
                builder.defaultValue("DependenciesProvider.instance.provide()")
            }
        }
        return constructorParameterSpecs
    }

    private inline fun getConstructorParameterSpec(
        type: KSType,
        action: (ParameterSpec.Builder) -> Unit = {},
    ): ParameterSpec {
        val typeSimpleName = type.declaration.simpleName.asString()
        val builder = ParameterSpec.builder(
            name = typeSimpleName.replaceFirstChar(Char::lowercase),
            type = type.toTypeName()
        )
        action(builder)
        return builder.build()
    }

    private fun getCreateComponentFactoryFunSpec(
        componentType: KSType,
        constructorParameterSpecs: List<ParameterSpec>
    ): FunSpec {

        val createComponentFunSpecBuilder = FunSpec.builder("createComponent")
            .addModifiers(KModifier.INTERNAL)
            .returns(componentType.toTypeName())
            .addParameters(constructorParameterSpecs)
            .addStatement("val builder = Dagger${componentType.declaration.simpleName.asString()}.builder()")

        for (parameterSpec in constructorParameterSpecs) {
            createComponentFunSpecBuilder
                .addStatement(".${parameterSpec.name}(${parameterSpec.name})")
        }

        return createComponentFunSpecBuilder
            .addStatement("return builder.build()")
            .build()
    }

    private fun getComponentFactoryObjectSpec(
        componentType: KSType,
        createComponentFunSpec: FunSpec,
    ): TypeSpec {
        return TypeSpec.objectBuilder(name = "${componentType.declaration.simpleName.asString()}Factory")
            .addModifiers(KModifier.INTERNAL)
            .addFunction(createComponentFunSpec)
            .addKdoc(
                """
                    File is generated by [com.example.ksp_component_builder.ComponentFactoryProcessor]
                """.trimIndent()
            )
            .build()
    }

    private fun write(spec: FileSpec, source: KSFile) {
        val dependencies = Dependencies(aggregating = false, source)
        val fos = try {
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = spec.packageName,
                fileName = spec.name,
            )
        } catch (e: FileAlreadyExistsException) {
            e.file.outputStream()
        }

        OutputStreamWriter(fos, StandardCharsets.UTF_8).use(spec::writeTo)
    }

    data class Spec(
        val providableDependenciesParameterSpecs: List<ParameterSpec>,
        val requiredDependenciesParameterSpecs: List<ParameterSpec>,
        val componentFactoryObjectSpec: TypeSpec,
    )
}