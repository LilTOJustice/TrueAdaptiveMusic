package liltojustice.trueadaptivemusic.client.music.pack.meta

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import net.fabricmc.loader.api.FabricLoader

data class MusicPackMeta(val requiredBridgeMods: List<ModDependency> = emptyList()) {
    fun jsonEncode(): String {
        return json.toJson(this)
    }

    companion object {
        private val json = GsonBuilder()
            .setPrettyPrinting()
            .create()

        fun jsonDecode(string: String): MusicPackMeta {
            return json.fromJson(string, MusicPackMeta::class.java)
        }

        fun init(rules: MusicTree): MusicPackMeta {
            return MusicPackMeta(getRequiredBridgeMods(rules))
        }

        private fun getRequiredBridgeMods(rules: MusicTree): List<ModDependency> {
            val requiredBridgeMods = mutableListOf<ModDependency>()
            val packageNames = mutableSetOf<String>()
            rules.traverse { node, _ ->
                node.predicates.forEach { predicate ->
                    packageNames.add(predicate::class.java.packageName)
                }
            }

            FabricLoader.getInstance().allMods.map { it.metadata }.forEach { metadata ->
                if (packageNames.any { packageName -> packageName.contains(metadata.id) }) {
                    requiredBridgeMods.add(ModDependency(metadata.id, metadata.name))
                }
            }

            return requiredBridgeMods.toList()
        }
    }
}