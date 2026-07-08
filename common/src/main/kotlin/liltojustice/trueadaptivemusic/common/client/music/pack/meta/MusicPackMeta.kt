package liltojustice.trueadaptivemusic.common.client.music.pack.meta

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.ModReflectionCommon
import liltojustice.trueadaptivemusic.common.client.music.tree.MusicTree

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
            val packageNames = mutableSetOf<String>()
            rules.traverse { node, _ ->
                node.predicates.forEach { predicate ->
                    packageNames.add(predicate::class.java.packageName)
                }
            }

            return ModReflectionCommon.getModDependencies(packageNames.toList())
        }
    }
}