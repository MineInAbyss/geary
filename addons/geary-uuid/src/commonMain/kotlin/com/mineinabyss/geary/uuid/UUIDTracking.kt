package com.mineinabyss.geary.uuid

import com.mineinabyss.geary.addons.Addon
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.uuid.systems.trackUUIDOnAdd
import com.mineinabyss.geary.uuid.systems.untrackUuidOnRemove
import com.mineinabyss.idofront.di.DI

val uuid2Geary by DI.observe<UUID2GearyMap>()

class UUIDTracking(
    val map: UUID2GearyMap = SimpleUUID2GearyMap(),
) : Addon<GearyModule, Nothing, UUID2GearyMap> {
    override fun install(app: GearyModule, configure: Nothing.() -> Unit): UUID2GearyMap {
        geary.run {
            trackUUIDOnAdd()
            untrackUuidOnRemove()
        }
        return map
    }
}
