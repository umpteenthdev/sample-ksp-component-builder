package com.example.ksp_component_builder

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate

internal class ComponentFactoryProcessor(
    private val logger: KSPLogger,
    private val fileGenerator: ComponentFactoryFileGenerator,
) : SymbolProcessor {

    private var round: Int = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        log("=====================")
        log("Round ${++round}")

        val annotatedSymbols = resolver
            .getSymbolsWithAnnotation("com.example.core.GenerateComponentFactory")
            .groupBy { it.validate() }
        val validSymbols = annotatedSymbols[true].orEmpty()
        val symbolsForReprocessing = annotatedSymbols[false].orEmpty()
        log("Valid symbols count: ${validSymbols.size}")
        log("Invalid symbols count: ${symbolsForReprocessing.size}")

        val markedClassDeclarations = validSymbols.mapNotNull { it as? KSClassDeclaration }
        log("Classes with annotation: $markedClassDeclarations")
        val componentsData = getComponentsData(markedClassDeclarations)

        for (componentData in componentsData) {
            log("Generate file for ${componentData.componentDeclaration}")
            fileGenerator.generateFile(componentData)
        }

        return symbolsForReprocessing
    }

    private fun getDaggerComponentAnnotation(target: KSAnnotated): KSAnnotation? {
        return target.annotations
            .filter { annotation -> annotation.shortName.asString() == "Component" }
            .find { annotation ->
                annotation.annotationType
                    .resolve()
                    .declaration
                    .qualifiedName
                    ?.asString() == "dagger.Component"
            }
    }

    private fun hasComponentFactory(componentDeclaration: KSClassDeclaration): Boolean {
        return componentDeclaration.declarations
            .mapNotNull { it as? KSClassDeclaration }
            .any { childDeclaration ->
                childDeclaration.annotations
                    .any { annotation ->
                        annotation.annotationType
                            .resolve()
                            .declaration
                            .qualifiedName
                            ?.asString() == "dagger.Component.Factory"
                    }
            }
    }

    private fun getComponentAnnotationParamValue(annotation: KSAnnotation, paramName: String): List<KSType> {
        val annotationArgument = annotation.arguments
            .find { argument -> argument.name?.asString() == paramName }
        val annotationArgumentValue = annotationArgument?.value as? List<KSType>

        return annotationArgumentValue.orEmpty().distinct()
    }

    private fun getComponentsData(markedClassDeclarations: List<KSClassDeclaration>): List<ComponentData> {
        val componentsData = mutableListOf<ComponentData>()

        for (componentDeclaration in markedClassDeclarations) {
            val containingFile = componentDeclaration.containingFile
            if (containingFile == null) {
                log("There is no containing file for $componentDeclaration")
                continue
            }

            val componentAnnotation = getDaggerComponentAnnotation(componentDeclaration)
            if (componentAnnotation == null) {
                log("There is no component annotation")
                continue
            }

            val hasComponentFactory = hasComponentFactory(componentDeclaration)
            if (hasComponentFactory) {
                val componentDeclarationName = componentDeclaration.qualifiedName?.asString()
                    ?: componentDeclaration.simpleName.asString()
                logger.error("Remove @Component.Factory from '$componentDeclarationName'")
                continue
            }

            val annotationData = getComponentAnnotationData(componentAnnotation)
            log("Annotation data gathered: $annotationData")

            componentsData += ComponentData(
                containingFile = containingFile,
                componentDeclaration = componentDeclaration,
                annotationData = annotationData,
            )
        }

        return componentsData
    }

    private fun getComponentAnnotationData(componentAnnotation: KSAnnotation): ComponentAnnotationData {
        val dependencies = getComponentAnnotationParamValue(componentAnnotation, paramName = "dependencies")
        val providableDependencies = getProvidableDependencies(dependencies)
        val requiredDependencies = dependencies - providableDependencies

        val modules = getComponentAnnotationParamValue(componentAnnotation, paramName = "modules")
        val requiredModules = getRequiredModules(modules)

        return ComponentAnnotationData(
            providableDependencies = providableDependencies,
            requiredDependencies = requiredDependencies,
            requiredModules = requiredModules,
        )
    }

    private fun getProvidableDependencies(allDependencies: List<KSType>): List<KSType> {
        return allDependencies
            .filter { dependencyType ->
                val declaration = dependencyType.declaration as KSClassDeclaration
                declaration.getAllSuperTypes()
                    .map { it.declaration }
                    .filterIsInstance<KSClassDeclaration>()
                    .any { superTypeDeclaration ->
                        superTypeDeclaration.qualifiedName?.asString() == "com.example.core.ProvidableDependency"
                    }
            }
    }

    private fun getRequiredModules(allModules: List<KSType>): List<ComponentAnnotationData.RequiredModule> {
        val requiredModules = mutableListOf<ComponentAnnotationData.RequiredModule>()
        for (moduleKSType in allModules) {
            val moduleDeclaration = moduleKSType.declaration
            if (moduleDeclaration !is KSClassDeclaration || moduleDeclaration.classKind != ClassKind.CLASS) {
                continue
            }

            val constructors = moduleDeclaration.getConstructors()
            val isRequired = constructors.any { constructor -> constructor.parameters.isNotEmpty() }

            if (!isRequired) {
                continue
            }

            val hasDefaultConstructor = constructors.any { constructor ->
                constructor.parameters.all { it.hasDefault }
            }

            requiredModules += ComponentAnnotationData.RequiredModule(
                type = moduleKSType,
                hasDefaultConstructor = hasDefaultConstructor,
            )
        }

        return requiredModules
    }

    private fun log(msg: String) {
        logger.warn("[${Thread.currentThread().name}] $msg")
    }
}