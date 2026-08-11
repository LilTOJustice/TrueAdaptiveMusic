# True Adaptive Music v2.8.0 - A bunch of fixes

---
## New Features:
### Exit Delay Node Option
- You can now set an exit delay for a node. This functions similar to an enter delay, just backwards. If a node is exited and the next node has an enter delay, the higher delay will be chosen to wait for the next track to play.
### Disable Ambience User Option
- From the user options (Sound Options -> TAM -> Options), you can now disable ambience played by TAM. This is useful if you are using an ambience mod and don't want the pack you are using to conflict.

## New Predicate Types:
### Time of day
- The time of day predicate allows setting music to play during a specific time range during a minecraft day. E.g. playing specific music for ticks 8000-12000.


## Fixes
- Fixed natural number input being annoying
- Fixed music weight option missing for sound event tracks
- Fixed structure predicates sometimes not being detected until the world is rejoined
- Fixed parallel music node option not giving a loop start point option
- Fixed fading not occurring when entering/leaving a parallel node context
- Fixed deleting tracks not removing the UI for music weight for that track
- NeoForge/Forge: Fixed missing config button in mod menu

Next up, [2.9](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/21) with a bunch more community-requested features!

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)

---