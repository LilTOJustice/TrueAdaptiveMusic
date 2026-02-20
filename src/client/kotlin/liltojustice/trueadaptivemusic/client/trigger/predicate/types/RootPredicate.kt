package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate

class RootPredicate(): MusicPredicate() {
    override fun test(): Boolean {
        return true
    }

    companion object: MusicPredicateCompanion<RootPredicate> {
        override fun fromJson(json: JsonObject): RootPredicate { return RootPredicate() }
    }
}
