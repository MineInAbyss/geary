package com.mineinabyss.geary.uuid

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import kotlin.uuid.Uuid

val UUIDTracking = createAddon<UUID2GearyMap>("UUID Tracking") {
    scopedModule {
        scoped<UUID2GearyMap> { SimpleUUID2GearyMap() }
    }
    onEnable {
        val configuration = get<UUID2GearyMap>()

        observe<OnSet>("Track UUID on add").involving(query<Uuid>()).exec { (uuid) ->
            val regenerateUUIDOnClash = entity.has<RegenerateUUIDOnClash>()
            if (uuid in configuration)
                if (regenerateUUIDOnClash) {
                    val newUuid = Uuid.random()
                    entity.set(newUuid)
                    configuration[newUuid] = entity.id
                } else error("Tried tracking entity $entity with already existing uuid $uuid")
            else
                configuration[uuid] = entity.id
        }

        observe<OnRemove>("Untrack UUID on remove")
            .involving(query<Uuid>())
            .exec { (uuid) -> configuration.remove(uuid) }
    }
}

