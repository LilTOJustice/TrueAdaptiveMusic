# Optional Node Parameters

Any predicate node can have some optional parameters set. Here are the current available parameters:

| Parameter Name | Type | Description |
| :------------- | :--- | :---------- |
| trackDelay     | UInt | Adds a set number of seconds of delay after a track finishes within a predicate node |
| trackDelayNoise | UInt | Adds a random amount +- between 0 and this value to trackDelay with a floor of 0 |