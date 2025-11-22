package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.observers.events.OnExtend
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.configuration.components.*
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.query.query
import org.koin.core.module.dsl.scopedOf

val Prefabs = createAddon<PrefabsModule>("prefabs") {
    dependsOn(SerializableComponents)

    scopedModule {
        scopedOf(::PrefabLoader)
        scopedOf(::PrefabsModule)
    }

    onEnable {
        observe<PrefabLoaded>("Inherit prefabs on load").exec { entity.inheritPrefabsIfNeeded() }

        observe<OnSet>("Track prefabs by key").involving(query<PrefabKey>()).exec { (key) ->
            entity.addRelation<NoInherit, PrefabKey>()
        }

        observe<OnSet>("Parse ChildOnPrefab").involving(query<ChildOnPrefab>()).exec { (child) ->
            entity {
                addParent(entity)
                setAll(child.components)
            }
            entity.remove<ChildOnPrefab>()
        }

        observe<OnSet>("Parse ChildrenOnPrefab").involving(query<ChildrenOnPrefab>()).exec { (children) ->
            children.nameToComponents.forEach { (name, components) ->
                entity {
                    set(EntityName(name))
                    set(Prefab())
                    addParent(entity)
                    addRelation<NoInherit, Prefab>()
                    setAll(components)
                }
            }
            entity.remove<ChildrenOnPrefab>()
        }

        observe<OnSet>("Parse instances on prefab").involving(query<InstancesOnPrefab, PrefabKey>())
            .exec { (instances, prefabKey) ->
                entity.addRelation<NoInherit, InstancesOnPrefab>()
                instances.nameToComponents.forEach { (name, components) ->
                    entity {
                        set(PrefabKey.of(prefabKey.namespace, name))
                        set(Prefab())
                        set(InheritPrefabs(setOf(prefabKey)))
                        addRelation<NoInherit, Prefab>()
                        setAll(components)
                    }
                    logger.d("Created instance $name of prefab $prefabKey")
                }
            }

        observe<OnSet>("Parse RelationOnPrefab").involving(query<RelationOnPrefab>()).exec { (relation) ->
            try {
                val target = entity.lookup(relation.target)?.id ?: return@exec
                entity.setRelation(componentId(relation.data::class), target, relation.data)
            } finally {
                entity.remove<RelationOnPrefab>()
            }
        }

        observe<OnSet>("Parse RelationWithData").involving(query<RelationWithData<*, *>>())
            .exec { (relationWithData) ->
                val entity = entity
                val data = relationWithData.data
                val targetData = relationWithData.targetData
                if (data != null) entity.set(data, relationWithData.relation.id)
                else entity.add(relationWithData.relation.id)
                if (targetData != null) entity.set(targetData, relationWithData.target)
                entity.remove<RelationWithData<*, *>>()
            }

        observeWithData<OnExtend>("Handle CopyToInstances on extend").exec {
            val copy = event.baseEntity.toGeary().get<CopyToInstances>() ?: return@exec
            copy.decodeComponentsTo(entity)
        }

        observeWithData<ReEmitEvent>("Handle ReEmitEvent").exec {
            entity.getRelationsByKind(event.findByRelationKind).forEach { relation ->
                val entity = relation.target.toGeary()
                if (entity.exists()) entity.emit(event = event.dataComponentId, data = event.data)
            }
        }

        world.infoReader.addInfoLine("prefabs") { entity ->
            entity.prefabs.mapNotNull { it.get<PrefabKey>().toString() }.joinToString()
        }
    }
}

fun GearySetup.prefabs(configure: PrefabsModule.() -> Unit) = install(Prefabs).apply(configure)
