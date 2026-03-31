package com.mineinabyss.geary.koin

import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.junit.jupiter.api.Test
import org.kodein.di.DI

class ArchetypeEngineModuleCheck {
    @Test
    fun checkKoinModule() {
        DI.invoke {
            import(ArchetypeEngineModule().module)
        }
    }

//    @Test
//    fun createKoinModule() {
//        koinApplication {
//            properties(ArchetypeEngineModule().properties)
//            modules(ArchetypeEngineModule().module)
//            checkModules()
//        }
//    }

    @Test
    fun startGeary() {
        geary(TestEngineModule)
    }
}
