package com.mineinabyss.geary.benchmarks.helpers

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary

abstract class GearyBenchmark : Geary by geary(TestEngineModule) {
    init {
        Logger.setMinSeverity(Severity.Warn)
    }
}
