package com.mineinabyss.geary.components

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.random.Random

class ComponentAsEntityProviderTest : GearyTest() {
    @Test
    fun `should correctly register reserved components`() {
        entity()
        componentId<Any>() shouldBe ReservedComponents.ANY
    }
}

class Something {

    class A(val test: String) {
    }

    class B(val test: String)

    @Test
    fun main() {
        val app = koinApplication() {
            modules(module {
                single { "default" }
                scope<A> {
                    scoped {
                        registerCallback(object : ScopeCallback {
                            override fun onScopeClose(scope: Scope) {
                                println("Closed")
                            }
                        })
                        Random.nextInt()
                    }
                    scoped {
                        get<Double>().toString()
                    }
                }
                scope<B> {
                    scoped { get<Int>().toString() }
                    scoped<Double> { 0.1 }
//                    scoped<String> { "B" + Random.nextInt(100) }
                }
            })
        }
        app.koin.createScope<A>("A").apply {
            get<Int>()
        }.close()
        val scopeB = app.koin.createScope<A>("A").get<Int>().let { println(it) }
//        val scopeB = app.koin.createScope<B>().apply {
//            linkTo(app.koin.getScope("A"))
//        }

//        scopeA.linkTo(scopeB)
//        scopeA.get<Int>().let { println(it) }
//        scopeB.get<String>().let { println(it) }
//        scopeA.close()
    }
}