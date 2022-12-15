package com.mineinabyss.geary.annotations.optin

/**
 * Currently no safety guaranteed due to internal scheduling issues.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class ExperimentalAsyncGearyAPI
