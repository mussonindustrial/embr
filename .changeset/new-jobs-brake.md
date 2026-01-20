---
'@embr-jvm/core-common': minor
---

`PyArgOverload`: Add support for parameterized types (`List`, `Map`, etc.).
- Recursively up-check parameterized/generic/wildcard types until a base class is found.
