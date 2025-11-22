package com.mineinabyss.geary.addons.dsl

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import org.koin.core.scope.Scope

class AddonScope(
    val addonName: String,
    val scope: Scope,
) : WorldScoped by scope.get<Geary>().newScope(), AutoCloseable {
    override val logger get() = world.logger//.withTag("Geary - $addonName")

    inline fun <reified T : Any> get() = scope.get<T>()

    override fun close() {
        super.close()
        scope.close()
    }
}