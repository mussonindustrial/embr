---
'@embr-modules/periscope': minor
---

(BREAKING) New `this` and global context.

In `system.perspective.runJavaScript~` functions:
1. `this` is now a reference to the `clientStore`.
2. A new Perspective specific namespace, accessible through the global `perspective` object is provided.
    - This object contains a `context` that can be used to access the `clientStore` (i.e. `perspective.context.client`).
    - This object will be the home of future Perspective specific utilities.