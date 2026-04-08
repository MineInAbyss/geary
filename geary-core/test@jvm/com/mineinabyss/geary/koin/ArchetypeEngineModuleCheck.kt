package com.mineinabyss.geary.koin

import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.junit.jupiter.api.Test

class ArchetypeEngineModuleCheck {
    @Test
    fun startGeary() {
        geary(TestEngineModule)
    }
}
