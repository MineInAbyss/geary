package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.helpers.GearyKoinComponent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.get

public abstract class GearyPlugin: JavaPlugin()

public val GearyKoinComponent.plugin: GearyPlugin get() = get()
