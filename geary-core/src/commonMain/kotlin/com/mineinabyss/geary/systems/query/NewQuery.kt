package com.mineinabyss.geary.systems.query

import kotlin.time.Duration


inline fun <T : Query> system(
    query: () -> T,
//    interval: Duration = Duration.ZERO,
    onTick: T.() -> Unit,
): System {
//    onTick(query)
}

class SystemTick<T : Query> {
    inline fun forEach(run: T.() -> Unit) {

    }
}

class SystemBuilder<T : Query>(val query: T) {
    inline fun onTick(run: T.() -> Unit): SystemBuilder<T> {
        SystemTick<T>().forEach(run)
        return this
    }

    var interval: Duration = Duration.ZERO
}

class System

class TestQuery : EventQuery() {
    var string by target.get<String>()
    val int by event.get<Int>().map { it.toString() }
}


fun printStringSystem() = system(fun() = object : Query() {
    var string by target.get<String>()
}) {
    println(string)
}

fun main() {
    system(::TestQuery) {
        println(string)
    }

    system(fun() = object : Query() {
        var string by target.get<String>().removable()
    }) {
        println(string)
    }
}
