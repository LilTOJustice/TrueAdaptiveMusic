package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

object MusicEventFactory: MusicTriggerFactory<MusicEvent>(
    MusicEventRegistry, { json, e -> ErrorEvent(json, e.message ?: "Unknown") }) {
}