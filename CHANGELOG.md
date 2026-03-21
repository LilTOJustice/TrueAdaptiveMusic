True Adaptive Music v2.0 - Going Gold

This is a rather large update. Hope you all enjoy it <3

### New Features:
#### Parallel Music
  - A big feature that I have wanted to make since I started working on this mod
  - Allows for music to work how it does in games that have multiple versions of the same music that transitions as the situation changes
    - Some examples: FF7 Remakes, Ultrakill, Banjo Kazooie, Deus Ex: Human Revolution
  - You can designate a node as "parallel" which will make all music in that node and all children's music run in parallel, so when the node changes, the music is already in the correct spot to seamlessly transition
    - This allows you to make sections of your pack be parallel separately from other sections
  - Some rules:
    - Only one track is allowed
    - Tracks must be exactly the same length to work properly
    - Recommended that tracks have proper looping as well - Start perfectly matches the end or the start point for the loop.
#### In-game Pack Browser
  - Another massive feature that I only thought of more recently
  - In the pack selection UI, hit the "Get more packs" button to open the browser
  - Music packs are pulled from the community discord and updates are synced every 10 minutes
  - See details on each pack, and download with one click!
#### TAM Logic Thread Separation
  - This one is an optimization to allow predicates to react faster without hurting performance.
  - Theoretically this should increase the performance of TAM, but let me know if you run into performance issues!
#### Vanilla Music Override
  - Allows setting a node to fall back to music that would play without TAM enabled when selected.

### New Predicate Types:
#### Scoreboard
  - Probably the most powerful predicate type
  - Tracks the value of a given scoreboard id, allowing virtually any game data to influence music!
  - Works no matter what in singleplayer, but only works in multiplayer servers if the scoreboard has a slot assigned
#### Team
  - Triggers while the player is on the given team
#### PvP
  - The combat predicate now works with Minecraft - Player. A player is considered hostile if they are not on your team.
#### Player Attribute
  - Triggers while the player has the given [attribute](https://minecraft.wiki/w/Attribute).

### UI Improvements:
- Pack export now shows in a new screen (I promise your game didn't freeze before)
- Discord link in pack browser/selection UI
- Comparators (Greater, Lesser, etc.) are now language agnostic

### Other Improvements:
- All node music/ambience will now play before it starts to repeat
- Zip file locking is fixed, so hopefully no more export errors
- Improve performance of loading music from legacy packs
- Removed the overworld_caves biome/dimension since they don't work anyway

True Adaptive Music is now "feature complete" but that doesn't mean that work on it stops here.

I will still be adding more features and fixing bugs as they come up (please keep the reports/suggestions coming!), as well as maintaining support for all current minecraft versions and new ones as they release.

Maybe I'll support forge/neoforge officially sometime but no promises!

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)

Early planning is on for [2.1](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/11)!