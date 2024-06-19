package com.mineinabyss.geary.addons

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.GearyModule

@GearyDSL
class Namespaced(val namespace: String, val module: GearyModule)

