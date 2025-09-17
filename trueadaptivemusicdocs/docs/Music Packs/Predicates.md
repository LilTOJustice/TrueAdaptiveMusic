# Predicates

In a Music Pack, Predicates are the driving force for **continuous** music being played. That is, when a predicate is selected by the music manager, the music stored within it is what is played. If the current selected predicate changes, the music changes with it.

## How is a Predicate Node Chosen

Every tick of the game, there is a music manager that traverses this tree and decides what music should be played. It will always pick the music belonging to the first satisfied node with no children that are satisfied. If a node has no children and it satisfied, that is the node that will be chosen. This is important as it allows for sectioning off different types of music for different scenarios (we'll dive deeper into this very soon). Another consequence of this is that we must have a root node that is always considered satisfied, as that allows us to define music (or no music) to play when no other predicate nodes are satisfied. This is why you are hearing music right now if you are following this tutorial in order. There are no other predicates to satisfy so it is defaulting to play music defined in the root predicate node. This behavior allows sectioning of music as it allows for something like the following scenario:

### An Example

Let's say you want to make a very simple music pack that adds combat music to minecraft. You could just add a "combat" node under the root node and put some combat music in that node and you're done... but what if you wanted the music to be different based on what dimension you are in? You could then create a dimension node as a child of the combat node. Since only one dimension predicate will be satisfied (you can't be in multiple dimensions at once), it will only play the combat music that you set for that dimension. You could also add some wandering music to each dimension node for some nice atmosphere in between combat by adding another dimension node as the child of root and giving it some music.

## Vanilla Types

!!! info

    Identifier types are explained [here](../Advanced%20Topics/TypedIdentifiers.md).

| Type Name | Parameters | True When... |
| :-------- | :--------- | :----------- |
| biome     | id: [BiomeIdentifier] | The player is in any biome within the `id` list |
| boss      | id: [EntityTypeIdentifier] | There is a boss bar on the player's screen for any of the entity types within the `id` list |
| combat    | None       | A mob is within sufficient range of the player and is attacking them |
| day       | None       | The ticks for the day is between 0 and 12999 inclusive |
| dimension | id: [DimensionIdentifier] | The player is in any dimension within the `id` list |
| first_day | None       | The total world ticks is between 0 and 24000 inclusive (within one minecraft day) |
| game_mode | gameMode: GameMode (Builtin Enum) | The player's gamemode is equal to the specified `gameMode` |
| health    | healthType: Enum('value', 'percentage'), direction: Enum('Greater', 'GreaterOrEqual', 'Lesser', 'LesserOrEqual'), health: Int | The player's health `value` or `percentage` meets the `direction` requirements with `health` |
| height    | above: Boolean, y: Int | The player's y coordinate is `above`/`below` the `y` value specified |
| moon_phase   | moonPhase: Enum('New', 'Full') | The world's moon phase matches that given by `moonPhase` specfied |
| night     | None       | The ticks for the day is between 13000 and 23999 inclusive |
| pillager_raid | None   | The player is currently in a pillager raid |
| riding    | entities: [EntityTypeIdentifier] | The entity that the player is riding is of a type within the `entities` list |
| status    | statusEffects: [StatusEffectIdentifier] | The player currently has any status effect specified within the `statusEffects` list |
| structure | id: [StructureIdentifier] | The player is within the maximum bounds of all features assigned to any of the structure types within the `id` list |
| structure_set | id: [StructureSetIdentifier] | Same as `structure`, but for sets of structure types rather than individual structure types. |
| title_screen | None    | The player is not in a world |
| weather      | weatherType: Enum('Clear', 'Rain', 'Thunder') | The weather is the type specified by `weatherType` |

Feel free to request new vanilla types. More types for integration with other mods can be added with [modded predicates](../Advanced%20Topics/Modded%20Predicates%20and%20Events.md).