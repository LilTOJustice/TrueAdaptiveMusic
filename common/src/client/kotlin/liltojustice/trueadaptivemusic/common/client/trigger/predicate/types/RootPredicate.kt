package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.common.Constants
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType

object RootPredicate: BasicPredicateType(Constants.ROOT_PREDICATE_NAME) {
    override fun test(): Boolean {
        return true
    }
}
