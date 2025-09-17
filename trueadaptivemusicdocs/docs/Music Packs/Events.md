# Events

In a Music Pack, Events are one-shot music that is played when some in-game event occurs, for example the player getting an advancement, or waking up from a bed. When an event plays, any ongoing music will quiet down until the event music finishes.

## The Event Pool

Events are tied to predicate nodes, in that each predicate node contains a list of events. When that predicate node is chosen, the event pool (the list of events that are able to be triggered) is updated. The current event pool is always the aggregate of the current predicate node's events and those of it's ancestors all the way up to `root`. If there is a conflict between two events between parent and child, the child will always take priority.

## Vanilla Types

!!! info

    Identifier types are explained [here](../Advanced%20Topics/TypedIdentifiers.md).

| Type Name | Parameters | Triggered When... |
| :-------- | :--------- | :---------------- |
| on_advancement_get | None | The player gets an advancement toast |
| on_boss_defeat | bosses: [EntityTypeIdentifier] | A boss is defeated nearby |
| on_day_start | None    | The ticks this day hits 0 |
| on_death  | None       | The player encounters the death screen |
| on_enter_predicate | None | The active predicate becomes the predicate that this event is part of |
| on_join_world | None   | The player's world gets set |
| on_night_start | None  | The ticks this day hits 13000 |
| on_recipe_unlock | None | The player gets a recipe unlock toast |
| on_tutorial_popup | None | The player gets a tutorial toast |
| on_wake_up | None        | The player's sleep chat closes without the "Leave Bed" button pressed |

Feel free to request new vanilla types. More types for integration with other mods can be added with [modded events](../Advanced%20Topics/Modded%20Predicates%20and%20Events.md).