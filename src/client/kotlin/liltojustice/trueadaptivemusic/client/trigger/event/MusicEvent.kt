package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger

abstract class MusicEvent: MusicTrigger() {
    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            MusicEventRegistry[this::class]
    }

    companion object: MusicEventCompanion<MusicEvent> {
    }

    interface MusicEventCompanion<TSelf>: MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }
    }
}