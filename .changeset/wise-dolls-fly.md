---
"@embr-js/utils": minor
---

`toFunction` now supplies global context first when calling the generated function. This ensures that the global context is always correct and available when function parameters are omitted.
