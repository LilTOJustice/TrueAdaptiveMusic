# True Adaptive Music v2.4

## New Features
### Node parameter: Compatibility Mode
- Requires vanilla music to be enabled for the node
- Completely disabled TAM music logic, allowing stronger compatibility
- **Only use this if you absolutely need to for non-TAM music to play!**

## New Predicate Types
### StructurePiece
- Requires world host to have TAM 
- Plays music when the player is in a given structure piece
- Warning: The identifiers for structure pieces are not reader-friendly, so you may need to look them up
- Also structure pieces with nested children will not work, since they can't be searched for (even recursively)
### BlockNearby
- Plays music when the player is a given distance from a given block or set of blocks

## Fixes/Other changes
- Drastically reduced mod size by switching to a reduced build of FFMpeg with less supported codecs (mp3, ogg, flac, wav)
- Added a warning for predicates that require world host to have TAM installed.
- Bumped api support to 1.1.3
- Browser now auto-refreshes the first time it opens after game startup

Next up is [2.4](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/16)

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)