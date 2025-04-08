---
'@embr-modules/periscope': patch
'@embr-modules/charts': patch
---

Fix Perspective component schema validation errors on startup. #265

- This patch uses `DelegatedClassLoader` to simultaneously resolve schema definitions from both Perspective's and our own resources.