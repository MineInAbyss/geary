package com.mineinabyss.geary.uuid

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.uuid.systems.untrackUuidOnRemove
import com.mineinabyss.geary.uuid.systems.trackUUIDOnAdd
import com.mineinabyss.idofront.di.DI

val uuid2Geary by DI.observe<UUID2GearyMap>()

object UUIDTracking : GearyAddonWithDefault<UUID2GearyMap> {
    override fun default() = SimpleUUID2GearyMap()

    override fun UUID2GearyMap.install() {
        geary.run {
            trackUUIDOnAdd()
            untrackUuidOnRemove()
        }
    }
}
