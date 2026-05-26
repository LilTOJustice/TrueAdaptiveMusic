# True Adaptive Music v2.5.0

## New Features:
### Node Option: Require Children
- Allows for a functional "AND" between nodes by required at least one child of a node to be satisfied, otherwise the node is skipped.
### Manual Sound Event Entry
- Since some mods don't put "music." in their sound event IDs, some modded music doesn't show in the lists. To remedy this, you can now use an identifier you type into the search bar by clicking "Use Custom" from the dropdown.
### Pack Option: Priority Sound Events
- Add any sound events that you want to preempt the TAM engine logic. When one of these events is requested in the Minecraft sound engine, TAM will take a backseat until the music finishes. Great for working out any compatibility issues.
  - Works with manual sound event entry :P

## Predicate Types:
### Speed
- Allows music to play based on the horizontal/vertical/overall speed of the player. (Horizontal only in 1.20.1)


## Fixes:
- Fixed a crash that occurs when music tries to play for an empty asset folder
- Fixed an occasional crash that would occur when joining a world
- Fixed the TAM sound engine breaking when resource packs are switched out

Next up is some super secret stuff :)

Please also reach out if you would like support to be added for a new language. I am still working on finishing Russian support :)