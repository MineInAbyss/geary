package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:cooldowns")
public class CooldownManager : GearyComponent {
    private val completionTime: MutableMap<String, Long> = mutableMapOf()

    public val incompleteCooldowns: Map<String, Long> get() = completionTime.filterValues { it > System.currentTimeMillis() }

    public fun isDone(key: String): Boolean {
        return (completionTime[key] ?: return true) <= System.currentTimeMillis()
    }

    public inline fun onCooldown(key: String, cooldown: Long, run: () -> Unit): Boolean {
        if (isDone(key)) {
            run()
            start(key, cooldown)
            return true
        }
        return false
    }

    public inline fun onCooldownIf(key: String, cooldown: Long, run: () -> Boolean): Boolean {
        if (isDone(key))
            if (run()) {
                start(key, cooldown)
                return true
            }
        return false
    }

    public fun start(key: String, cooldown: Long) {
        completionTime[key] = System.currentTimeMillis() + cooldown
    }

    public fun reset(key: String) {
        completionTime -= key
    }
}
