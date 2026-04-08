package com.mineinabyss.geary.examples.intro

import com.mineinabyss.dependencies.addCloseable
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
            addCloseable { logger.i { "Closing engine" } }
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
        engine.close()
    }

    @Test
    fun `simple manual ticking`() {
        geary(ArchetypeEngineModule(beginTickingOnStart = false)).use {
            addCloseable { logger.i { "Closing engine" } }
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
        }
        engine.tick()
    }
}