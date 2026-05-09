If a predicate/event type needs to take arguments, those arguments must be implemented as a subclass of `TriggerArguments`.

```kotlin
abstract class TriggerArguments
```

It is typically recommended to implement this as a data class to ensure proper interfacing with the API.

!!! warning

    **DO NOT** include any members in the body of the class. They will not be initialized. Instead, include any extra data in the [`state`](./Trigger%20State.md) of your type.

If you need an argument that is not one of the [provided input widget types](../Provided%20Input%20Widget%20Types.md), then you need to [implement and register your own input widget](../../How%20To/Creating%20an%20Input%20Widget.md).