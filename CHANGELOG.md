# True Adaptive Music v2.9.3

---
## Fixes
- Fixed crash caused by PrioritySoundEvents not being serialized properly across all modloaders
- Fixed game not closing immediately due to leaked timer objects, causing it to wait and eventually force close from the jvm watchdog
- Added back the common "c" tags
- MC 26.3 - Fixed a UI bug that prevented click-dragging nodes from working

## Known Issues
- Screen predicate UI does not work with NeoForge/Forge yet due to issues with the NeoForge/Forge classloaders
  - Hopefully can fix in a future update
  - You can still create the pack in Fabric and use it in NeoForge/Forge
- Mac binaries are missing for ffmpeg, ffprobe, etc.
  - Coming soon. Will take some work since I have my own custom FFmpeg build I made to reduce the file size of the mod

Next up, [2.10](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/22) with a bunch more community-requested features!

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)

---