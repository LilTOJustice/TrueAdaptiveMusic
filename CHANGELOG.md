# True Adaptive Music v2.10

---
## Fixes
- Fixes for mac ffmpeg and neo/forge screen predicate coming soon
- Improved player and mob combat detection

## Features
- Suggested Music Pack Downloads
  - **Modpack authors:** You can now include music packs in your modpack without dropping a large zip into the modpack file!
  - Just go to the music pack browser, highlight a pack, and click "Set Suggested Pack" at the top left
  - The music pack will download for your users in the background as the game starts up, and will auto select once it is ready!
  - Has no impact on modpack startup time
- No more river interruptions
  - River "biomes" are now handles specially and won't interrupt your other biome music
  - If at least one river biome is specified in the predicate, the old logic will be used
- Better toast events
  - You can now play specific music for individual advancements with the On Advancement Get event type.
- Float support for music weights
  - You can now use floating-point (decimal) numbers in the music weights parameter
- Priority sound events now apply to every sound source
  - Enhances compatibility with other music-playing mods
- TAM API 1.4.0 - Maven Support
  - TAM API has migrated off of mod platforms to maven. Bridge mod makers will need to update their maven sources to the new maven repo!

## New Predicates
- Has Advancement
  - Play specfic music if the player has earned a specfic advancement
- Can See Sky
  - Play music based on whether there is a clear path from you to the sky above
  - Option to ignore water blocks

Next up, [2.11](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/23)

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)

---