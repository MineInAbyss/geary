package com.mineinabyss.geary.addons

import com.mineinabyss.idofront.di.DIContext

interface Application {
    val di: DIContext
    val environment: ApplicationEnvironment
}


open class ApplicationEnvironment(
) {

}

inline fun Application.dependencies(configure: DIContext.() -> Unit) = di.apply(configure)

inline fun <reified T : Any> Application.getOrNull() = di.getOrNull<T>()

inline fun <reified T : Any> Application.get() = di.get<T>()

/**
 * Installs a [addon] into this application, if it is not yet installed.
 */
inline fun <A : Application, B : Any, reified F : Any> A.install(
    addon: Addon<A, B, F>,
    noinline configure: B.() -> Unit = {},
): F {
    di.getOrNull<F>()?.let { return it }

    val installed = addon.install(this, configure)
    di.add<F>(installed)
    return installed
}
