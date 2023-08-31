package com.mineinabyss.geary.events

/**
 * A handler which will run a check on an event that requests one.
 */
// TODO reimplement as checking listener
//abstract class CheckHandler(
//    parentListener: Listener,
//    sourceNullable: Boolean
//) : Handler(parentListener, sourceNullable) {
//    init {
//        parentListener.event.mutableFamily.has<RequestCheck>()
//    }
//
//    abstract fun check(source: SourceScope?, target: TargetScope, event: EventScope): Boolean
//
//    override fun handle(source: SourceScope?, target: TargetScope, event: EventScope) {
//        if (!check(source, target, event)) event.entity.apply {
//            remove<RequestCheck>()
//            add<FailedCheck>()
//        }
//    }
//}
