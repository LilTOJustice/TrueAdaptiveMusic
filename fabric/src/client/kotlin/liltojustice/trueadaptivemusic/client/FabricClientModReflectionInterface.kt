package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.common.client.ClientModReflectionInterface
import liltojustice.trueadaptivemusic.common.client.music.pack.meta.ModDependency
import net.fabricmc.loader.api.FabricLoader

object FabricClientModReflectionInterface: ClientModReflectionInterface {
    override fun getModDependencies(packageNames: List<String>): List<ModDependency> {
        return FabricLoader.getInstance().allMods
            .filter { mod -> packageNames.any { packageName -> packageName.contains(mod.metadata.id) } }
            .map { mod -> ModDependency(mod.metadata.id, mod.metadata.name) }
    }

    override fun isModLoaded(modId: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }
}