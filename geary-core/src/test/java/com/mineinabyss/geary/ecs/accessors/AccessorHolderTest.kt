package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.accessors.building.getOrDefault
import com.mineinabyss.geary.ecs.accessors.building.map
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.invoke
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class AccessorHolderTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    object FancyQuery : Query() {
        val TargetScope.default by getOrDefault<String>("empty!")
        val TargetScope.mapped by get<Int>().map { it.toString() }
    }

    @Test
    fun fancyAccessors() {
        val entity = Engine.entity()
        //TODO put back when Koin comes
//        FancyQuery.toList().isEmpty() shouldBe true
        FancyQuery.none { it.entity == entity } shouldBe true
        entity.set(1)
        FancyQuery {
            first { it.entity == entity }.apply {
                default shouldBe "empty!"
                mapped shouldBe "1"
            }
        }
    }
}
