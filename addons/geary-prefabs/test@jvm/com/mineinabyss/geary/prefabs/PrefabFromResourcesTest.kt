package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabsDSLExtensions.fromJarResourceDirectory
import com.mineinabyss.geary.prefabs.PrefabsDSLExtensions.fromJarResources
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serialization
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class PrefabFromResourcesTest {
    private fun world() = geary(TestEngineModule) {
        serialization {
            format("yml", ::YamlFormat)
        }
    }

    @Test
    fun `should load prefabs from resource file`() {
        val world = world().configure {
            namespace("test") {
                prefabs {
                    fromJarResources(PrefabFromResourcesTest::class, "prefabs/prefabA.yml")
                }
            }
        }.start()

        with(world) {
            entityOfOrNull(PrefabKey.of("test:prefabA")) shouldNotBe null
            entityOfOrNull(PrefabKey.of("test:prefabB")) shouldBe null
        }
    }

    @Test
    fun `should load prefabs from resources directory`() {
        val world = world().configure {
            namespace("test") {
                prefabs {
                    fromJarResourceDirectory(PrefabFromResourcesTest::class, "prefabs")
                }
            }
        }.start()
        with(world) {
            entityOfOrNull(PrefabKey.of("test:prefabA")) shouldNotBe null
            entityOfOrNull(PrefabKey.of("test:prefabB")) shouldNotBe null
        }
    }
}
