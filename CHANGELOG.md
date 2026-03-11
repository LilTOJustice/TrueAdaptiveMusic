True Adaptive Music v1.8 - The Loopy Update

**True Adaptive Music will now appear in the Forge/Neoforge environment for Modrinth/Curseforge with a dependency on Sinytra Connector. Since Sinytra is limited to 1.20.1 and 1.21.1, those will be the only available versions until they port to other versions of Minecraft.**

New Features:
- Music Looping
  - Music can be set to loop within a node
  - Support for "intros" where the loop doesn't start until a specific point in the song
  - The music file must be edited to have the end match the start for looping to work properly
- Persistent Node Music
  - Music can be set as "persistent" on a per-pack basis in the pack options (where the description is set)
  - If set to true, music will finish before changing after the active node changes
  - Naturally, this disables fading between nodes

UI Improvements:
- Horizontal Scrolling in the Pack Structure Panel
- Collapsible/Expandable nodes

Other Improvements:
- Music fading and clamping sounds better
- Required bridge mods are now stored in pack metadata
  - If you load a pack that requires a mod you don't have, the pack selection UI will show a warning.

Additional Notes:
- **Attention Pack Creators:** Your pack descriptions will be cleared and need to be set again

[2.0](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/10) is up next :)