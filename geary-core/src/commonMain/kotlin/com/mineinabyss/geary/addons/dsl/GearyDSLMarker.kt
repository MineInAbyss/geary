package com.mineinabyss.geary.addons.dsl

/**
 * A marker annotations for DSLs.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class GearyDSLMarker
