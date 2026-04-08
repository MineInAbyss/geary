package com.mineinabyss.geary.examples.addons

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.addons.gearyAddon
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.geary
import org.junit.jupiter.api.Test

class CustomAddon {
    data class Example(val value: Int)

    val module = gearyAddon("my-name") {
        val example by single { Example(10) }

        println("Created my addon with value ${example.value}!")

        addCloseable {
            println("Closing my addon!")
        }
    }

    @Test
    fun `creating custom addons`() {
        geary(ArchetypeEngineModule()) {
            install(module)
        }.close()
    }
}