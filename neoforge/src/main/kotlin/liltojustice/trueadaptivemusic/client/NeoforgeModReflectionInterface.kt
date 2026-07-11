package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.music.pack.meta.ModDependency
import net.neoforged.fml.ModList

object NeoforgeModReflectionInterface: ClientModReflectionInterface {
    override fun getModDependencies(packageNames: List<String>): List<ModDependency> {
        return ModList.get().mods
            .filter { mod -> packageNames.any { it.contains(mod.namespace) } }
            .map { ModDependency(it.modId, it.displayName) }
    }

    override fun isModLoaded(modId: String): Boolean {
        return ModList.get().mods.any { it.modId == modId }
    }
}