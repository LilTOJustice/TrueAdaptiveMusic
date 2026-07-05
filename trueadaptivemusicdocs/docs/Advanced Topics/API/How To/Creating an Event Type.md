First off, if you are someone who likes to learn through seeing a bunch of examples, you can check out the implementations of the vanilla [events](https://github.com/LilTOJustice/TrueAdaptiveMusic/tree/master/src/client/kotlin/liltojustice/trueadaptivemusic/client/trigger/event/types), and [mixins for events](https://github.com/LilTOJustice/TrueAdaptiveMusic/tree/master/src/client/java/liltojustice/trueadaptivemusic/client/mixin/event).

## A warning about event types

Event types are more complicated to implement than predicate types, so I would recommend reading [how to do that](./Creating%20a%20Predicate%20Type.md) first. What makes them more complicated is that they will **almost** always require a mixin to implement. I say **almost** because if you are making event types as an integration to a mod you own rather than as a bridge, you can just invoke the event manually from your mod code.

If you don't already know how to create mixins, follow [this wiki](https://wiki.fabricmc.net/tutorial:mixin_introduction).


To start, create a new object under the client section of your mod. For the sake of convention, please name your object with "Event" at the end.

Here we'll name it `OnPokeBattleVictoryEvent`, since we want music to play when we win in a pokemon battle.

## Implementing the API

After creating the object you have some choices of what to do next depending on how you want your event to behave. Your object can inherit from any of the [base event types](../Reference/Trigger/Event%20Types.md).

### Empty Object

Continuing with our example, we have our object.

```kotlin
object OnPokeBattleVictoryEvent {
}
```

### Inheriting the Event Type

Since we want our event to take arguments and take input when it is triggered, but it doesn't need to store any additional data during runtime, we'll use a `StaticEventType`, which we need to provide the registry name for (we'll call it `on_poke_battle_victory`).

```kotlin
object OnPokeBattleVictoryEvent: StaticEventType<???, ???>("on_poke_battle_victory", ???) {
}
```

### Adding the Arguments

In place of the `???`, we need to define two things.

First, the structure of the arguments that our event type needs. In our example, we want to know what type of battle we are in so we can play different music depending on the battle type.

Elsewhere, we have defined an enum `BattleType`, so we'll use that as our argument. We are only able to use an enum as an argument because it is one of the [provided input widget types](../Reference/Provided%20Input%20Widget%20Types.md). If you need an argument whose type that is not in that list, you need to [create the input widget](../How%20To/Creating%20an%20Input%20Widget.md) for that type, or you will not be able to create an event of this type in the UI.

**Important Note**: It is **HIGHLY RECOMMENDED** that your arguments class is a `data class` with all members given a default value within the primary constructor.

```kotlin
object OnPokeBattleVictoryEvent: StaticEventType<OnPokeBattleVictoryEvent.Arguments, ???>("on_poke_battle_victory", typeOf<Arguments>()) {
    data class Arguments(val battleType: BattleType = BattleType.Any): TriggerArguments()
}
```

### Adding the Input

For those last `???` we need to define the structure of the input that we need our event to get when it is triggered so we can validate whether the trigger should be accepted. For this case, we just need to know what the current battle type is so we can compare it to the type that was passed as an argument.

```kotlin
object OnPokeBattleVictoryEvent: StaticEventType<OnPokeBattleVictoryEvent.Arguments, OnPokeBattleVictoryEvent.Input>("on_poke_battle_victory", typeOf<Arguments>()) {
    data class Arguments(val battleType: BattleType = BattleType.Any): TriggerArguments()
    data class Input(val battleType: BattleType): EventInput()
}
```

#### Arguments vs. Input
If you are confused about the difference between `arguments` and `input`, think about this:

The `arguments` define how the user that is using our event type in their pack is able to customize the event they create. In this case we want the user to be able to make different music play for different battle types.

On the other hand, the `input` is the information that we need to know about the state of the game **when the event is triggered**. This information might not always be the same depending on the nature of the event logic.

The `arguments` get filled when the user creates the event in their pack, and the `input` gets filled when the event is actually triggered.

### Implementing the Validate Function

Now we need to implement the `validate()` function which allows us to use both the arguments and the input to decide whether the event should actually trigger. This is really simple, we just compare the battle type of the fight that is happening (`input`), to the battle type we expect to see (`arguments`).

```kotlin
object OnPokeBattleVictoryEvent: StaticEventType<OnPokeBattleVictoryEvent.Arguments, OnPokeBattleVictoryEvent.Input>("on_poke_battle_victory", typeOf<Arguments>()) {
    data class Arguments(val battleType: BattleType = BattleType.Any): TriggerArguments()
    data class Input(val battleType: BattleType): EventInput()

    override fun validate(arguments: Arguments, input: Input): Boolean {
        return arguments.battleType == BattleType.Any || arguments.battleType == input.battleType
    }
}
```

Now most of the logic done, but we still need to trigger the event itself.

### Creating the Mixin

Now we'll need to make a mixin to trigger the event. If you are the creator of the mod are making the event for, you can just invoke the callback seen below directly in your mod code.

This particular mixin was a bit painful to work around, but here was the result (I split it into two files because java is a garbage language :P):
```java title="OnPokeBattleVictoryEventMixin.java"
@Mixin(value = BattleMessagePane.class, remap = false)
abstract class OnPokeBattleVictoryEventMixin extends AlwaysSelectedEntryListWidget<BattleMessagePane.BattleMessageLine> {
    public OnPokeBattleVictoryEventMixin(MinecraftClient minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
    }

    @Inject(
        method = "addEntry(Lcom/cobblemon/mod/common/client/gui/battle/widgets/BattleMessagePane$BattleMessageLine;)I",
        at = @At("HEAD"))
    private void addEntry(BattleMessagePane.BattleMessageLine entry, CallbackInfoReturnable<Integer> cir) {
        // Delegate the trigger logic to kotlin
        MixinExtensions.INSTANCE.addEntry(entry);
    }
}
```

```kotlin title="MixinExtensions.kt"
object MixinExtensions {
    fun addEntry(entry: BattleMessageLine) {
        val player = MinecraftClient.getInstance().player ?: return
        val userWonString = battleLang("win", player.displayName ?: "").string

        if (entry.line.getInternalString() == userWonString) {
            val enemySide = CobblemonClient.battle?.side2 ?: return

            val battleType = if (enemySide.actors.all { actor -> actor.type == ActorType.WILD }) {
                BattleType.Wild
            }
            else if (enemySide.actors.all { actor -> actor.type == ActorType.NPC }) {
                BattleType.Trainer
            }
            else if (enemySide.actors.any { actor ->
                    actor.activePokemon.any { pokemon ->
                        Constants.legendaries.contains(pokemon.battlePokemon?.displayName?.string)
                    }
            }) {
                BattleType.Legendary
            }
            else {
                BattleType.Any
            }

            // Trigger the event and pass in the battle type as input
            TAMAPI.invokeEvent(
                OnPokeBattleVictoryEvent, OnPokeBattleVictoryEvent.Input(battleType))
        }
    }
}
```

### Registering the Event Type

Finally, we just need to register the new type in our mod entrypoint.

```kotlin
...

TAMAPI.registerEventType(OnPokeBattleVictory)

...
```

And that's it! Now the event type will show in the UI, and will trigger only when we win a battle with the matching battle type.