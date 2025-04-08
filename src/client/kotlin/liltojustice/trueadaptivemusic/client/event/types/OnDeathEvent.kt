package liltojustice.trueadaptivemusic.client.event.types

class OnDeathEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnDeathEvent> {
        override fun getTypeName(): String {
            return "on_death"
        }
    }
}