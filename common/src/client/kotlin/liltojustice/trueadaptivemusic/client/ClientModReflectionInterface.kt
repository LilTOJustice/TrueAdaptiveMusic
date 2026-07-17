package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.music.pack.meta.ModDependency

interface ClientModReflectionInterface {
    fun getModDependencies(packageNames: List<String>): List<ModDependency>

    fun isModLoaded(modId: String): Boolean
}