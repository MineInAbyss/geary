package com.mineinabyss.geary.addon

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.extend
import org.koin.core.component.get

public interface GearyAddonManagerContext {
    public val addonManager: GearyAddonManager
}

public val GearyContext.addonManager: GearyAddonManager by extend { get() }
