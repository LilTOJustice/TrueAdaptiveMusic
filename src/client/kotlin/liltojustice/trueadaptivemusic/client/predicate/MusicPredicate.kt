package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.MusicTrigger
import liltojustice.trueadaptivemusic.client.ReflectionHelper
import net.minecraft.client.MinecraftClient
import kotlin.reflect.KClass

abstract class MusicPredicate: MusicTrigger {
    abstract fun test(client: MinecraftClient): Boolean

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from abstract predicate type.")
        }

        override fun fromJson(json: JsonObject): MusicPredicate {
            return MusicTrigger.fromJsonProvideSubclasses(json, getTriggerImplementerSubclasses()) as MusicPredicate
        }
    }

    interface MusicPredicateCompanion<TSelf>: MusicTrigger.MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
        override fun getTriggerImplementerSubclasses(): List<KClass<out MusicPredicate>> {
            return ReflectionHelper.getSubclassesOf(MusicPredicate::class)
        }
    }
}