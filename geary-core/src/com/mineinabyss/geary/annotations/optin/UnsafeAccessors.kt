package com.mineinabyss.geary.annotations.optin

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Reading and writing entity data without an accessor reduces speed and does not enforce null safety for accessors." +
            " Be careful when manually removing components used by other accessors."
)
annotation class UnsafeAccessors
