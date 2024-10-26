package com.mineinabyss.geary.addons

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.GearySetup

@GearyDSL
class Namespaced(val namespace: String, val setup: GearySetup)

