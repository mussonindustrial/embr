---
'@embr-modules/periscope-web': minor
'@embr-modules/periscope': minor
---

`runJavaScript` functions now expose `perspective.context.view`, the client-side `ViewModel` of the view that made the `runJavaScript` call.
- This is `undefined` when targeting a specific `session`/`page`. 
