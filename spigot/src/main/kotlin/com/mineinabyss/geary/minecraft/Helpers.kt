package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


/** Gets [Geary] via Bukkit once, then sends that reference back afterwards */
public val geary: Geary by lazy { JavaPlugin.getPlugin(Geary::class.java) }

internal fun debug(message: Any?) {
    if (GearyConfig.data.debug) broadcast(message)
}

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
public var PersistentDataContainer.isGearyEntity: Boolean
    get() = has(Engine.componentsKey, PersistentDataType.BYTE)
    set(value) =
        when {
            value -> set(Engine.componentsKey, PersistentDataType.BYTE, 1) //TODO are there any empty keys?
            else -> remove(Engine.componentsKey)
        }
