package com.mineinabyss.geary.ecs.api.services

import kotlin.reflect.KClass

public object GearyServices {
    private val serviceGetters = mutableListOf<GearyServiceProvider>()

    public fun setServiceProvider(serviceProvider: GearyServiceProvider) {
        serviceGetters.clear()
        registerServiceProvider(serviceProvider)
    }

    public fun registerServiceProvider(serviceProvider: GearyServiceProvider) {
        serviceGetters.add(serviceProvider)
    }

    public fun <T : Any> getService(kClass: KClass<T>): T? =
        serviceGetters.asSequence()
            .mapNotNull { it.getService(kClass) }
            .firstOrNull()
}

public interface GearyServiceProvider {
    public fun <T : Any> getService(service: KClass<T>): T?
}

public inline fun <reified T : Any> gearyService(): T =
    gearyServiceOrNull() ?: error("Service ${T::class.simpleName} not found!")

public inline fun <reified T : Any> gearyServiceOrNull(): T? =
    GearyServices.getService(T::class)

