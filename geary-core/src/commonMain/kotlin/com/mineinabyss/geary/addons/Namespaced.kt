package com.mineinabyss.geary.addons

import com.mineinabyss.geary.modules.GearyModule

class Namespaced(val namespace: String)

fun GearyModule.namespace(namespace: String, configure: Namespaced.() -> Unit) = Namespaced(namespace).configure()
