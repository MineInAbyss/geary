package com.mineinabyss.geary.koin

import co.touchlab.kermit.LoggerConfig
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules
import org.koin.test.verify.verify
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

class ArchetypeEngineModuleCheck {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        ArchetypeEngineModule().module.verify(
            extraTypes = listOf(
                Boolean::class,
                Duration::class,
                LoggerConfig::class,
                CoroutineContext::class,
                Function0::class,
                Koin::class,
            )
        )
    }

    @Test
    fun createKoinModule() {
        koinApplication {
            properties(ArchetypeEngineModule().properties)
            modules(ArchetypeEngineModule().module)
            checkModules()
        }
    }

    @Test
    fun startGeary() {
        geary(TestEngineModule)
    }
}
