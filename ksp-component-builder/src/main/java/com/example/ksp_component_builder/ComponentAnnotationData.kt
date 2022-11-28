package com.example.ksp_component_builder

import com.google.devtools.ksp.symbol.KSType

data class ComponentAnnotationData(
    val providableDependencies: List<KSType>,
    val requiredDependencies: List<KSType>,
    val requiredModules: List<RequiredModule>,
) {

    data class RequiredModule(
        val type: KSType,
        val hasDefaultConstructor: Boolean,
    )
}