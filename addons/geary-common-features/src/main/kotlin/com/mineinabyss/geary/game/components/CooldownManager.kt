package com.mineinabyss.geary.game.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * > geary:cooldowns
 *
 * A component that manages cooldowns for an entity. Each cooldown has a unique key, but may be used to keep track of
 * anything. Typically a good idea to persist this component.
 *
 * @property completionTime A map of cooldown keys to the value of [System.currentTimeMillis] when they are complete.
 * @property incompleteCooldowns Calculated map of incomplete cooldowns.
 */
// TODO persist cooldowns on entity, but dont serialize any cooldowns that are already complete
// TODO use relations to accomplish this
@Serializable
@SerialName("geary:cooldowns")
data class CooldownManager(
    private val completionTime: MutableMap<String, Cooldown> = mutableMapOf()
) {
    val incompleteCooldowns: Map<String, Cooldown>
        get() = completionTime.filterValues {
            it.endTime > System.currentTimeMillis()
        }

    /** @return Whether a certain cooldown is complete. */
    fun isDone(key: String): Boolean {
        return (completionTime[key]?.endTime ?: return true) <= System.currentTimeMillis()
    }

    /**
     * Runs [something][run] if no cooldown is active and starts the cooldown after running it.
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * @param run What to run if this cooldown is complete.
     *
     * @return whether [run] was executed.
     */
    inline fun onCooldown(key: String, length: Duration, run: () -> Unit): Boolean {
        if (isDone(key)) {
            run()
            start(key, length.inWholeMilliseconds)
            return true
        }
        return false
    }

    /**
     * Runs [something][run] if no cooldown is active and starts the cooldown only if [run] returns true.
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * @param run What to run if this cooldown is complete. Returns whether or not to start the cooldown.
     *
     * @return whether [run] was executed.
     */
    inline fun onCooldownIf(key: String, length: Duration, run: () -> Boolean): Boolean {
        if (isDone(key))
            if (run()) {
                start(key, length.inWholeMilliseconds)
                return true
            }
        return false
    }

    /**
     * Starts a cooldown of a certain [length] under a [key].
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * */
    fun start(key: String, length: Long) {
        completionTime[key] = Cooldown(length)
    }

    /** Clears the cooldown for a [key]. */
    fun reset(key: String) {
        completionTime -= key
    }
}

