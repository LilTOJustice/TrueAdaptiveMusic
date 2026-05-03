# True Adaptive Music v2.3

## New Features
### Server-Side Support
- Predicates like the Structure/Structure Set predicates didn't work unless you are the host of the world, since the server never sends that data to the client.
- Now the server will send this data to the client if the server has True Adaptive Music installed, making those predicates functional even if you aren't the host.
- The following predicates should now work properly/are now possible:
  - Structure
  - Structure Set
  - Spawn Point Nearby (*New predicate type, see below*)
  - Custom (*New predicate type, see below*)
- I plan on improving the TAM API to allow utilizing this feature within bridge mods in a later update.

### Biome Tags
- Biome tags can now be set in the Biome predicate

## New Predicate Types
### Spawn Point Nearby
- Allows specific music to play when you are within a certain number of blocks distance from your current respawn point. Great for having "home base" music.
- **Requires server support** (see above)

### Custom
- The new most powerful predicate type. By providing a json file following the [predicates spec](https://minecraft.wiki/w/Predicate) in the new "predicates" directory, you can select that file with this predicate to have it evaluate every second of in-game time (20 ticks).
- This allows creating a predicate for basically anything you can imagine, as long as it is supported in the minecraft predicate specification.
- **Requires server support** (see above)

## Fixes
- Fixed warden combat not triggering
- Improved predicate caching system, preventing situations where the predicate will rapidly change when leaving a node. This should improve the behavior of Persistent Node Music feature.


Next up is [2.4](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/16)

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)