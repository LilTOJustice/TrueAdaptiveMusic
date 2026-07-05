First off, if you are someone who likes to learn through seeing a bunch of examples, you can check out the implementations of the vanilla [predicates](https://github.com/LilTOJustice/TrueAdaptiveMusic/tree/master/src/client/kotlin/liltojustice/trueadaptivemusic/client/trigger/predicate/types).

To start, create a new object under the client section of your mod. For the sake of convention, please name your object with "Predicate" at the end.

Here we'll name it `PokeBattlePredicate`, since we want music to play when we are in a pokemon battle.

## Implementing the API

After creating the object you have some choices of what to do next depending on how you want your predicate to behave. Your object can inherit from any of the [base predicate types](../Reference/Trigger/Predicate%20Types.md).

### Empty Object

Continuing with our example, we have our object.

```kotlin
object PokeBattlePredicate {
}
```

### Inheriting the Predicate Type

Since we want our predicate to take arguments, but it doesn't need to store any additional data during runtime, we'll use a `StaticPredicateType`, which we need to provide the registry name for (we'll call it `poke_battle`).

```kotlin
object PokeBattlePredicate: StaticPredicateType<???>("poke_battle", ???) {
}
```

### Adding Arguments

In place of the `???`, we need to define the structure of the arguments that our predicate type needs. In our example, we want to know what type of battle we are in so we can play different music depending on the battle type.

Elsewhere, we have defined an enum `BattleType`, so we'll use that as our argument. We are only able to use an enum as an argument because it is one of the [provided input widget types](../Reference/Provided%20Input%20Widget%20Types.md). If you need an argument whose type that is not in that list, you need to [create the input widget](../How%20To/Creating%20an%20Input%20Widget.md) for that type, or you will not be able to create a predicate of this type in the UI.

**Important Note**: It is **HIGHLY RECOMMENDED** that your arguments class is a `data class` with all members given a default value within the primary constructor.

```kotlin
object PokeBattlePredicate: StaticPredicateType<PokeBattlePredicate.Arguments>("poke_battle", typeOf<Arguments>()) {
    data class Arguments(val battleType: BattleType = BattleType.Any): TriggerArguments()
}
```

### Implementing the Test Function

Now we can implement the `test()` function. In the test function we have access to our arguments, which we use to decide whether the predicate should return true.

```kotlin
object PokeBattlePredicate
    : StaticPredicateType<PokeBattlePredicate.Arguments>("poke_battle", typeOf<Arguments>()) {
    data class Arguments(val battleType: BattleType = BattleType.Any): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        return CobblemonClient.battle?.let {
            val enemySide = it.side2

            when (arguments.battleType) {
                BattleType.Any -> true
                BattleType.Wild -> enemySide.actors.all { actor -> actor.type == ActorType.WILD }
                BattleType.Trainer -> enemySide.actors.all { actor -> actor.type == ActorType.NPC }
                BattleType.Legendary -> enemySide.actors.any { actor ->
                    actor.activePokemon.any { pokemon ->
                        Constants.legendaries.contains(pokemon.battlePokemon?.displayName?.string)
                    }
                }
            }
        } ?: false
    }
}
```

### Registering the Predicate Type

Finally, we just need to register the new type in our mod entrypoint.

```kotlin
...

TAMAPI.registerPredicateType(PokeBattlePredicate)

...
```

And that's it! Now the predicate type will show in the UI, and will return true while we are in a pokemon battle based on what type of battle it is.