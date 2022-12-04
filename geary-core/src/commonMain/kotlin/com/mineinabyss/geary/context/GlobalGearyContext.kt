/*
 * Geary was rewritten to use the new experimental context receivers prototype feature which turned out ot be
 * very buggy. We currently use Koin for dependency injection, which can replace contexts for classes, but
 * it becomes inefficient in functions, as they need to do a hashmap lookup to access current state.
 *
 * This will be an alternative in the meantime. We get the benefit of isolated tests, while losing support for
 * multiple engines at a time. Once context receivers are out of beta, we will switch back and delete this.
 */
package com.mineinabyss.geary.context

public var geary: GearyModule = TODO()

//public val koin: Koin get() = KoinPlatformTools.defaultContext().get()
