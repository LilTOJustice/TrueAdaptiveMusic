package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.client.MinecraftClient
import kotlin.reflect.KClass

abstract class MusicPredicate: MusicTrigger {
    abstract fun test(client: MinecraftClient): Boolean

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun getTypeName(): String {
            throw MusicTriggerException("Attempt to get type name from abstract predicate type.")
        }

        override fun fromJson(json: JsonObject): MusicPredicate {
            try {
                return MusicTrigger.fromJsonProvideSubclasses(json, getTriggerImplementerSubclasses()) as MusicPredicate
            }
            catch (e: MusicTriggerException) {
                return ErrorPredicate(json, e.message ?: "Unknown")
            }
        }
    }

    interface MusicPredicateCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
        override fun getTriggerImplementerSubclasses(): List<KClass<out MusicPredicate>> {
            return ReflectionHelper.getSubclassesOf(MusicPredicate::class)
        }
    }
}