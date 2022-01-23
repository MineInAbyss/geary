package com.mineinabyss.geary.webconsole.data

import kotlinx.serialization.Serializable

@Serializable
data class ComponentInfo(
    val name: String,
    val data: String,
) {
}

@Serializable
data class EntityInfo(
    val info: String,
//    val components: Map<String, String>,
) {
    companion object {
        val path = "/entity"
    }
}
