---
'@embr-modules/charts': major
---

(BREAKING) New scriptable properties `this` and global context.

In scriptable property functions:
1. `this` is now a reference to the component itself.
2. Global variables `self` and `client` have been removed.
    - These have been replaced with a Perspective specific namespace, accessible through the global `perspective` object.
    - This object contains a `context` that can be used to access the `clientStore` (i.e. `perspective.context.client`).
    -     - This object will be the home of future Perspective specific utilities.