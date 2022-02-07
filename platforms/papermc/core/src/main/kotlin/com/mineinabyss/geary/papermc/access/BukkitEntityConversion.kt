package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.GearyScopeMC
import com.mineinabyss.idofront.typealiases.BukkitEntity

public fun BukkitEntity.toGeary(): GearyEntity = GearyScopeMC {
    bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }
}

//public inline fun <T> BukkitEntity.access(
//    async: Boolean = false,
//    crossinline run: suspend GearyEntity.() -> T
//): T = GearyScopeMC {
//    access(this@access, async = async) { (entity) ->
//        run(entity)
//    }
//}


//public inline fun <T> GearyScope.access(
//    vararg bukkit: BukkitEntity,
//    async: Boolean = false,
//    crossinline run: suspend (List<GearyEntity>) -> T
//): T {
//    return runBlocking(BukkitDispatcher(geary, async)) {
//        val entities = bukkit.map { it.toGeary() }
//        engine.withLock(entities) {
//            run(entities)
//        }
//    }
//}

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
public inline fun BukkitEntity.toGeary(init: GearyEntity.() -> Unit): GearyEntity =
    toGeary().apply { init() }

public fun BukkitEntity.toGearyOrNull(): GearyEntity? = GearyScopeMC().run {
    bukkit2Geary[entityId]
}

public fun GearyEntity.toBukkit(): BukkitEntity? = get()
