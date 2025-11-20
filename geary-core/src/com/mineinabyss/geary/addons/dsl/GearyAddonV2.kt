package com.mineinabyss.geary.addons.dsl

import com.mineinabyss.geary.modules.Geary
import org.koin.core.scope.Scope
import org.koin.dsl.ScopeDSL
import kotlin.reflect.KClass

@GearyDSL
interface GearyAddonDSL

class AddonBuilder() : GearyAddonDSL {
    private var scopedModule: ScopeDSL.() -> Unit = {}

    fun dependsOn(vararg addons: GearyAddon<*>) {}
    fun scopedModule(block: ScopeDSL.() -> Unit) {
        scopedModule = block
    }

    fun onEnable(block: AddonScope.() -> Unit) {

    }

    fun onClose(block: Scope.() -> Unit) {

    }

    fun systems(block: SystemsBuilder.() -> Unit) {
        TODO()
    }

}

class SystemsBuilder(scope: Scope) : AddonScope(scope) {
    fun single(builder: Geary.() -> AutoCloseable) {
        TODO()
    }
}

data class GearyAddon<T : Any>(
    val name: String,
    val type: KClass<T>,
    val dependencies: List<GearyAddon<*>>,
    val createScope: ScopeDSL.() -> Unit,
    val onInstall: AddonScope.() -> Unit,
    val onUninstall: AddonScope.() -> Unit,
) {
    fun overrideScope(block: ScopeDSL.() -> Unit): GearyAddon<T> {
        TODO()
    }
}

open class AddonScope(val scope: Scope) : Geary by scope.get<Geary>(), AutoCloseable {
    private val closeables = mutableListOf<AutoCloseable>()
    inline fun <reified T : Any> get() = scope.get<T>()

    /**
     * Adds an [AutoCloseable] resource that will be closed right before the addon's onClose method is called.
     */
    fun addCloseable(closeable: AutoCloseable) {
        closeables += closeable
    }

    /**
     * Adds multiple [AutoCloseable] resources that will be closed right before the addon's onClose method is called.
     */
    fun addCloseables(vararg closeables: AutoCloseable) {
        this@AddonScope.closeables += closeables
    }

    override fun close() {
        closeables.forEach { it.close() }
        scope.close()
    }
}

inline fun <reified T : Any> gearyAddon(key: String, install: AddonBuilder.() -> Unit): GearyAddon<T> {
    TODO()
}
