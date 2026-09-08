# True Adaptive Music v2.9.0

---
## New Features:
### Ignore Persistence Node Option Revamp
- Can now be used regardless of whether the music pack option "Persist Node Music" is enabled.
- Will now simply invert the behavior of "Persist Node Music" if enabled.
### Node Titles
- You can now set node titles to make debugging/pack creation easier (node colors coming in 2.10)
### Hide "True Adaptive Music" Button Option
- Enable this to hide the "True Adaptive Music" button that shows in the sound options.
- Useful for modpack creators who want to abstract away the musical identity of their packs.

## New Predicate Types:
### Screen
- Allows music to be played when the user is viewing a specific GUI screen.
- Discovers screens based on their fully-qualified class name, which will work with screens created by mods as well!
### Item Nearby
- Allows music to be played when an item(s) is on the ground within a certain radius (optionally if it is in your inventory as well).
### Item
- Allows music to be played if a given item(s) is in the player's inventory.
- Can be specified to the player's hotbar, either hand, or specifically Main/Offhand as well.

## Fixes
- Removed the 32-character limit in textboxes.
- Fixed wither combat detection.
- Fixed a crash on certain systems when typing an illegal filename character (e.g. ':').
- Forge 1.20.1 - Fixed crash on startup for dedicated servers.

Next up, [2.10](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/22) with a bunch more community-requested features!

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)

---