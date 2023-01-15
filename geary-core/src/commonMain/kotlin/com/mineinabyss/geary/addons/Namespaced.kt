package com.mineinabyss.geary.addons

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.GearyConfiguration
import com.mineinabyss.geary.modules.GearyModule
import kotlin.reflect.KClass

@GearyDSL
class Namespaced(val namespace: String, val gearyConf: GearyConfiguration)

