package com.mineinabyss.geary.api.addon

/**
 * Different phases of Geary's startup process.
 *
 * They are executed top to bottom.
 */
public enum class GearyLoadPhase {
    /** Loading serializers via autoscan or manually. */
    REGISTER_SERIALIZERS,

    /** Creating formats (ex for `.yml` files.) */
    REGISTER_FORMATS,

    /** Going through every prefab file and loading them one by one. */
    LOAD_PREFABS,

    /** All previous registration tasks have been completed. */
    ENABLE,
}
