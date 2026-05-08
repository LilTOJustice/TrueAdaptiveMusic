This page will go over the classes your predicate type can inherit from in increasing level of complexity. Examples implementing these can be found [here](https://github.com/LilTOJustice/TrueAdaptiveMusic/tree/master/src/client/kotlin/liltojustice/trueadaptivemusic/client/trigger/predicate/types).

If a predicate type doesn't take any arguments and doesn't need to keep track of any internal state during runtime, it can just inherit from `BasicPredicateType`

```kotlin
abstract class BasicPredicateType(typeName: String) {
    abstract fun test(): Boolean
}
```

If a predicate type takes [`arguments`](./Trigger%20Arguments.md) but it don't need it to have an internal [`state`](./Trigger%20State.md), it can inherit from `StaticPredicateType`

```kotlin
abstract class StaticPredicateType<TArg: TriggerArguments>(typeName: String, argumentType: KType) {
    abstract fun test(arguments: TArg): Boolean
}
```

Finally, if a predicate type takes both, it can inherit from `PredicateType`.

```kotlin
abstract class PredicateType<TArg: TriggerArguments, TState: TriggerState>(typeName: String, argumentType: KType) {
    abstract fun test(arguments: TArg, state: TState): Boolean
    abstract fun createState(arguments: TArg): TState
}
```