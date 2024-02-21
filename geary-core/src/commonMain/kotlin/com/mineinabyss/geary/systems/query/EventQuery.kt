package com.mineinabyss.geary.systems.query

abstract class EventQuery() : Query() {
    val event: QueriedEntity = QueriedEntity()
    val source: QueriedEntity = QueriedEntity()
}
