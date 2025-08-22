package liltojustice.trueadaptivemusic.client.trigger.event

import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerFactory

class MusicEventFactory(musicEventRegistry: MusicEventRegistry): MusicTriggerFactory<MusicEvent>(
    musicEventRegistry, { json, e -> ErrorEvent(json, e.message ?: "Unknown") }) {
}