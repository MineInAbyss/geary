package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GearyEntityTests {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun setPersisting() {
        val entity = Engine.entity {
            setPersisting("Test")
        }
        val relations = entity.type.getArchetype().relations[componentId<PersistingComponent>().toLong()]
        relations.size shouldBe 1
        relations.first().component shouldBe (componentId<String>() or HOLDS_DATA)
        entity.getPersistingComponents().shouldContainExactly("Test")
    }

    @Test
    fun setAllPersisting(){
        val entity = Engine.entity {
            set("Test")
            set(1)
        }
        val entitySetAll = Engine.entity {
            setAll(listOf("Test", 1))
        }
        entity.type shouldContainExactly entitySetAll.type
    }
}
