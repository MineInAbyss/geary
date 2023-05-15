package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TypeRolesTest {

    @Test
    fun hasRoleTest() {
        val actual = 1uL or HOLDS_DATA
        actual.hasRole(HOLDS_DATA) shouldBe true
    }

    @Test
    fun hasNotRoleTest() {
        val actual = 1uL or HOLDS_DATA
        actual.hasRole(RELATION) shouldBe false
    }

    @Test
    fun withRoleTest() {
        val actual = 1uL
        actual.withRole(HOLDS_DATA) shouldBe (actual or HOLDS_DATA)
    }

    @Test
    fun withoutRoleTest() {
        val actual = 1uL
        actual.withRole(HOLDS_DATA).withoutRole(HOLDS_DATA) shouldBe actual
    }

    @Test
    fun alterRoleTest() {
        val actual = 1uL
        actual.withRole(HOLDS_DATA).withInvertedRole(HOLDS_DATA) shouldBe actual
    }
}
