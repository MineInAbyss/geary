package com.mineinabyss.geary.uuid

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.uuid.systems.trackUUIDOnAdd
import com.mineinabyss.geary.uuid.systems.untrackUuidOnRemove

val UUIDTracking = createAddon<UUID2GearyMap>("UUID Tracking", { SimpleUUID2GearyMap() }) {
    onStart {
        trackUUIDOnAdd(configuration)
        untrackUuidOnRemove(configuration)
    }
}

