# Modded Predicates and Events

!!! warning

    This is an advanced topic that requires at least some programming experience in Java/Kotlin or other similar high-level languages.
    Modding experience (particularly in Minecraft) is also recommended, but far from required. I assume here that you already know how to create a mod for Minecraft.
    If you are completely new and want to learn how to create a mod, follow the [fabric tutorial here](https://wiki.fabricmc.net/tutorial:start) as well as using [this page](https://docs.fabricmc.net/develop/getting-started/setting-up) to set up your development environment. **Feel free to [join the discord](https://discord.gg/v64K4hNdXu) and ask for help as well!**

True Adaptive Music allows the creation of custom predicate and event types via creating **Bridge Mods**! This allows for a whole new level of Music Pack creation as predicates and events can be made that are specific to other mods.

One great example is with the Cobblemon mod. This fantastic mod adds the world of Pokémon to Minecraft, and one staple of Pokémon is the battles! There's just one problem, the battles have absolutely no music. What if we could make a new predicate type that specifically is true when the user is in a battle? Then we could specify some battle music to play when it's true!

If you want to create a bridge mod, take a look at the [API Setup](API/How%20To/API%20Setup%20(for%20Bridge%20Mods).md), and then follow the tutorials for creating [predicate types](./API/How%20To/Creating%20a%20Predicate%20Type.md), [event types](./API/How%20To/Creating%20an%20Event%20Type.md), and [input widgets](./API/How%20To/Creating%20an%20Input%20Widget.md).