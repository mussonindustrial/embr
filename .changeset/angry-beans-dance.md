---
"@embr-jvm/core-common": minor
---

(BREAKING) Add support to `PyArgOverload` for nullable types. Arguments are now defined using `KType`'s, and nullability checks are applied during function matching. If an argument is missing it is considered null and if the argument is allowed to be null it is considered a valid match.
