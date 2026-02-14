True Adaptive Music v1.6

Features:
- New "Ambience" track:
  - Plays alongside the music track
  - By default, nodes inherit ambience music
  - Volume controllable with the ambience slider
- Reworked UI
  - All options now have a tooltip that describes them
  - Text is now "prettified", with much more human-friendly display text
- Audio normalization
  - If ffmpeg is installed, all music/ambience will be played normalized to vanilla music's sound levels
  - This means that super quiet/loud mp3s will be raised/lowered to normal volume comparable to vanilla music

Fixes:
- Completely replaced Minecraft's sound engine
  - True Adaptive Music now hosts all of its sound in a custom sound engine running alongside vanilla
  - Mods can no longer prevent music from playing
  - Music will continue to play even when vanilla sound would normally stop (e.g. joining or leaving a world)
  - Performance is significantly improved, with the hitching that used to occur when starting an MP3 now gone
  - Perfectly smooth UI mouseover previews
- Phantoms are now working with the combat predicate
- Fixed some text not being colored in the UI
- Fixed UI text inputs resetting to default values when an improper character is entered

[1.7](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/8) is starting development soon