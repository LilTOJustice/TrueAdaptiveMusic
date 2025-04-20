package liltojustice.trueadaptivemusic.client.gui.widget.utility

interface DataWrapped<TImpl: DataWrapped<TImpl>> {
    var customData: Any?

    fun withCustomData(data: Any?): TImpl {
        customData = data
        return this as TImpl
    }
}