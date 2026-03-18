---
'@embr-modules/periscope-web': minor
'@embr-modules/periscope': minor
---

`runJavaScript` functions now expose `perspective.context.component`. This is the client-side `ComponentModel` of the component that made the `runJavaScript` function call.
- This is `undefined` when targeting a specific `session`/`page`. 
