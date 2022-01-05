package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import com.mineinabyss.geary.ecs.query.Query
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class AccessorHolderTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    object FancyQuery : Query() {
        val AffectedScope.default by getOrDefault<String>("empty!")
        val AffectedScope.mapped by get<Int>().map { it.toString() }
    }

    @Test
    fun fancyAccessors() {
        val entity = Engine.entity()
        FancyQuery.none { it.entity == entity } shouldBe true
        entity.set(1)
        FancyQuery.apply {
            find { it.entity == entity }!!.apply {
                default shouldBe "empty!"
                mapped shouldBe "1"
            }
        }
    }
}
