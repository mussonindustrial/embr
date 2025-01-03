---
'@embr-jvm/perspective-designer': patch
---

Add manual delegate functions for `DesignerComponentDescriptor`. Delegated methods with default implementations are not overriden, as per https://youtrack.jetbrains.com/issue/KT-18324.
