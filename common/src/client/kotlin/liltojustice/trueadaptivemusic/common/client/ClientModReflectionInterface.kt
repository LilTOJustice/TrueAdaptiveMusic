package liltojustice.trueadaptivemusic.common.client

import liltojustice.trueadaptivemusic.common.client.music.pack.meta.ModDependency

interface ClientModReflectionInterface {
    fun getModDependencies(packageNames: List<String>): List<ModDependency>

    fun isModLoaded(modId: String): Boolean
}