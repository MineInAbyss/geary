package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.services.GearyServiceProvider
import com.mineinabyss.geary.ecs.api.services.GearyServices
import kotlin.reflect.KClass

class TestGearyServiceProvider(
    val map: Map<KClass<out Any>, Any>
) : GearyServiceProvider {
    override fun <T : Any> getService(service: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[service] as? T
    }
}

fun setEngineServiceProvider(engine: Engine) =
    GearyServices.setServiceProvider(TestGearyServiceProvider(mapOf(Engine::class to engine)))
