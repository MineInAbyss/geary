import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary

abstract class GearyBenchmark : Geary by geary(TestEngineModule).start()
