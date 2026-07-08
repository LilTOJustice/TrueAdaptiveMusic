package liltojustice.trueadaptivemusic

import liltojustice.trueadaptivemusic.common.client.music.pack.meta.ModDependency
import net.neoforged.fml.ModList
import net.neoforged.neoforgespi.language.IModFileInfo

object ModReflectionCommon {
    fun getModDependencies(packageNames: List<String>): List<ModDependency> {
        return ModList.get().mods
            .filter { mod -> packageNames.any { it.contains(mod.namespace) } }
            .map { ModDependency(it.modId, it.displayName) }
    }

    fun isModLoaded(modId: String): Boolean {
        return ModList.get().mods.any { it.modId == modId }
    }
}