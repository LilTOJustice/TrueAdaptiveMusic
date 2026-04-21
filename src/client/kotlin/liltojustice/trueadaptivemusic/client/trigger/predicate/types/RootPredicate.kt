package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType

class RootPredicate: BasicPredicateType(Constants.ROOT_PREDICATE_NAME) {
    override fun validate(): Boolean {
        return true
    }
}
