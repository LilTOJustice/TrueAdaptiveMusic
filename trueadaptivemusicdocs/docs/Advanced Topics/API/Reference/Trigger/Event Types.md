This page will go over the classes your event type can inherit from in increasing level of complexity. Examples implementing these can be found [here](https://github.com/LilTOJustice/TrueAdaptiveMusic/tree/master/src/client/kotlin/liltojustice/trueadaptivemusic/client/trigger/event/types).

If an event type doesn't take any arguments and doesn't need to keep track of any internal state during runtime, it can just inherit from `BasicEventType`.

```kotlin
abstract class BasicEventType(typeName: String)
```

If an event type takes only [`arguments`](./Trigger%20Arguments.md) and [`input`](./Event%20Input.md), then it can inherit from `StaticEventType`.

```kotlin
abstract class StaticEventType<TArg: TriggerArguments, TInput: EventInput>(typeName: String, argumentType: KType) {
    abstract fun validate(arguments: TArg): Boolean
}
```

If an event type takes [`arguments`](./Trigger%20Arguments.md) and has [`state`](./Trigger%20State.md), then it can inherit from `CloseEventType`.

```kotlin
abstract class ClosedEventType<TArg: TriggerArguments, TState: TriggerState>(typeName: String, argumentType: KType) {
    abstract fun validate(arguments: TArg, state: TState): Boolean
}
```

Finally, if an event type should take all three, it can inherit from the `EventType`.

```kotlin
abstract class EventType<TArg: TriggerArguments, TState: TriggerState, TInput: EventInput>(typeName: String, argumentType: KType) {
    // Optional, but you probably need it
    open fun validate(arguments: TArg, state: TState, input: TInput): Boolean {
        return true
    }

    abstract fun createState(arguments: TArg): TState
}
```