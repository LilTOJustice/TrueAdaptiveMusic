If an event needs to be fed game state input when the event is triggered, the event type should include an `EventInput` implementation. This input can be implemented any way, as it will be instantiated when the event is triggered.

```kotlin
abstract class EventInput
```