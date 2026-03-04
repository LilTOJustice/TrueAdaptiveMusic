package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate

class RootPredicate: MusicPredicate() {
    override fun test(): Boolean {
        return true
    }
}
