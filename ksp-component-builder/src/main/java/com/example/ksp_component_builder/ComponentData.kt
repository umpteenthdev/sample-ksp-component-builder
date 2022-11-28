package com.example.ksp_component_builder

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile

data class ComponentData(
    val containingFile: KSFile,
    val componentDeclaration: KSClassDeclaration,
    val annotationData: ComponentAnnotationData,
)