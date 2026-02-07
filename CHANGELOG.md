True Adaptive Music v1.5

Features and Fixes:
- All external audio is now normalized, so all tracks will follow the same loudness standard (-16 LUFS)
- New inheritMusic node parameter (if turned on, the node will include the music from its parent in addition to its own)
- You can now add descriptions for your packs in the UI (go to "Edit Pack" -> "Meta")
- Smoother sigmoid curve fading for crossfades (fading should feel smoother)
- "Music Toast" support for minecraft versions that have that feature
- The same track will no longer play twice in a row (if possible)
- Guardians now trigger the combat predicate
- Fixed a crash when hitting enter on the pack naming screen
- Vanilla advancement toast will no longer play if advancement event is set
- Improved performance for ffmpeg streaming
- Improved performance with debug hud on
- And some small other fixes...

New Vanilla Event Types:
- OnPause

What's Next?:
1.5 of course :) See current planned features here https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/6