package com.mineinabyss.geary.koin

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.koin.core.error.NoDefinitionFoundException
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Testing assumptions about how koin scopes work
 */
class KoinScopeTests {
    class A(val test: String)

    class B(val test: String)

    @Test
    fun `two scopes with different roots can't access each other`() {
        val app = koinApplication {
            modules(module {
                scope<A> {
                }
            })
        }
        val app2 = koinApplication {
            modules(module {
                scope<B> {
                    scoped { "B" }
                }
            })
        }

        val a = app.koin.createScope<A>()
        val b = app2.koin.createScope<B>()
        a.linkTo(b)

        shouldThrow<NoDefinitionFoundException> {
            a.get<String>()
        }
    }
}