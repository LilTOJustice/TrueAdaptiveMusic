If a predicate/event type needs to maintain some internal state for caching, logic, or any other reason, that should be stored in the `state` of the type. Any predicate/event types' `state` must inherit from `TriggerState`. The state can be implemented in any way.

```kotlin
abstract class TriggerState
```