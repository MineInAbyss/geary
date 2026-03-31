package com.mineinabyss.geary.examples.intro

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.query
import org.junit.jupiter.api.Test

class EngineSetup {
    @Test
    fun `simple engine setup and entity creation`() {
        val engine = geary(ArchetypeEngineModule(beginTickingOnStart = false))
        with(engine) {
            val a = entity {
                set<String>("Hello A")
            }
            val b = entity {
                set("Hello B")
                set(1)
            }

            findEntities { hasSet<String>(); not { has<Int>() } }.forEach {
                logger.i { "${it.id} has '${it.get<String>()}'" }
            }
        }
    }

    @Test
    fun `simple manual ticking`() {
        with(geary(ArchetypeEngineModule(beginTickingOnStart = false))) {
            system(query()).execOnAll {
                logger.i { "There are ${count()} entities: ${entities().joinToString { it.type.toString() }}!" }
            }
            tick()
            logger.i { "Creating entity" }
            entity {
                set("Hello")
            }
            tick()
        }
    }


    @Test
    fun `closeable scopes`() {
        val engine = geary(ArchetypeEngineModule(beginTickingOnStart = false))
        engine.newScope().use {
            system(query()).execOnAll {
                logger.i("System added to engine!")
            }
            engine.tick()
        }
        engine.tick()
    }
}