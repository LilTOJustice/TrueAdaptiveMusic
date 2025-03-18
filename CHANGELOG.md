Features for Everyone:
- Music now resumes from where it left off when passing between two situations, e.g. entering and re-entering combat
- Logs are now sent through Log4j through fabric as per the fabric standard, rather than using STDOUT. This means logs will now look cleaner and will blend with other mods' logs.

Features for MusicPack Creators:
- New logic for how predicate nodes are chosen! Check the [wiki](https://liltojustice.github.io/TrueAdaptiveMusic/#how-is-a-predicate-node-chosen)
- For predicates with Identifier parameters, you can now select multiple for one node. The predicate will be true if it would for any of the given IDs.
- New predicate types
  - First Day: True during the first 24000 ticks of a world
  - Weather: True when the current weather matches what is chosen (clear, rain, thunder)
  - Height: True when the player's y-coordinate is above/below a chosen y-value
- From within the MusicPack editor, music will now play as a preview when the mouse cursor is hovered over any external file in the music selector.

Fixes:
- Sources in the music manager would leak, causing minecraft to eventually hit its max source count of 8. This would prevent any new music from playing.
- Combat predicate satisfaction should be much more stable, resulting in smoother combat transitions.

What's Next?
See the list of planned features/fixes for [1.2](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/3)