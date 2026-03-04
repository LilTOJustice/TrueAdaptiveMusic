True Adaptive Music v1.7 - The Foundation Update

A note to the users: 99% of this update is focused on improving the pack/mod creation experience. The only new feature here for regular users is the first in the list, immersive ambience.

Features:
- Immersive Ambience
  - Any stereo ambience tracks will now pan around the player as they look around, creating a much more immersive soundscape!
  - Ambience now crossfades seamlessly as tracks change. If there is only one ambience track, it will even crossfade with itself, preventing a sudden "stop".
- Updated Wiki
  - So much with pack creation and bridge mod creation has changed with this update. So I'm finally updating the [wiki](https://liltojustice.github.io/TrueAdaptiveMusic/) again. Please take a look if you are looking into making a pack or bridge mod.
- Hybrid Predicates
  - Each node in the pack can now hold multiple predicates, allowing creation of hybrid predicates (equivalent to a logical OR.) For example, you can have the same music play for both a specific structure and weather effect at the same time.
- Folders as music tracks
  - Folders can now be used as track selections in the UI. When selected, a random track from the folder will be chosen whenever music would play.
- Copy pasting nodes
  - You can now copy nodes by holding shift while clicking and dragging a node. If you hold ctrl, you will also copy all the children of the selected node.
- UI improvements
  - As a consequence of the hybrid predicates feature, the widgets for configuring a node and configuring a predicate are seperate. Click the gear icon to edit a node, and click on the predicate name to edit the predicate.
  - The event view widget now splits the node view widget, which should make working with events much less visually taxing.
  - Spacing of the internal widgets should feel smoother.
- Bridge modding improvements
  - It is now possible and just as easy as fabric to create bridge mods for forge/neoforge mods. Follow the wiki for details on how to create a forge/neoforge bridge mod.
  - Serialization Improvements
    - You no longer need to implement to/fromJson methods when creating a modded trigger type.

Minor Fixes/Changes:
- Fixed some errors coming from the sound engine
- Now fully decoupled from the vanilla minecraft sound engine. No more compatibility issues with Sound Physics Remastered!

Additional Notes:
- As a result of the new features in this update, I had to create a completely different pack structure. While normally this would break existing packs, I created a system that should still read packs in an old format. Please let me know if there are any issues with loading legacy packs! Modded predicates/events will need to be updated as well, with more info in the wiki.

[1.8](https://github.com/LilTOJustice/TrueAdaptiveMusic/milestone/9) is up next :)