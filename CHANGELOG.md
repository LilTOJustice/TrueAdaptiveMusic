The big one

Features and Fixes:
- Join [the brand-new community discord](https://discord.gg/v64K4hNdXu)!
- Updated and improved the wiki for all the new features, [check it out](https://liltojustice.github.io/TrueAdaptiveMusic/)!
- Support for any audio file type! Check the wiki for setup instructions
- Events - Music Packs can now have events that trigger one-shot music. Check down below for the first set of vanilla types.
- Music now fades out when a jukebox is playing nearby

What made this update take so long?
- Moved into a new place and played my first full playthrough of Cyberpunk 2077 :)

New Vanilla Predicate Types:
- Riding
- MoonPhase
- GameMode
- Health
- StatusEffect
- PillagerRaid

New Vanilla Event Types:
- OnEnterPredicate (Triggers whenever the node the event is in becomes the active node)

QoL:
- Music now plays while the game is paused. It will also fade into a quieter volume when paused, then ramp back up after unpausing.
- Only start combat music if the enemy is within the player's field of vision (creepers are stealthy again).

Fixes:
- Forge should now work again!
- Vanilla music works again
- A few UI improvements
- Performance should be improved for more taxing predicates as they will now run on tick multiples instead of every tick.

Modding Improvements
- More of a detriment: All modded predicate/event types must now be registered during client initialization with TAMClient.registerPredicate/Event() or they will not be recognized. This was necessary to restore forge support.
- You can now specify the tickRate of a modded predicate type to improve performance. Override the getTickRate() function.

What's Next?:
My main effort now will be porting the mod to more minecraft versions. Meanwhile I'll be collecting feedback/using the mod in my own servers to plan out more features for 1.4!