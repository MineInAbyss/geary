package com.mineinabyss.geary.addon

import com.mineinabyss.geary.context.GearyModule
import com.mineinabyss.geary.context.extend
import org.koin.core.component.get

public interface GearyAddonManagerContext {
    public val addonManager: GearyAddonManager
}

public val GearyModule.addonManager: GearyAddonManager by extend { get() }
