package com.mineinabyss.geary.actions

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

class ActionsCancelledExceptionTest {
    @Test
    fun `has no stack trace`() {
        ActionsCancelledException().stackTrace.toList().shouldBeEmpty()
    }
}
