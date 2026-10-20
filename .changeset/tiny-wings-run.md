---
'@embr-jvm/core-servlets': patch
'@embr-jvm/core-gateway': patch
'@embr-modules/periscope': patch
'@embr-modules/charts': patch
---

Move to a custom `RouteHandler` to allow for Web Resource caching.
- Both Embr Charts' and Embr Periscope's client resources are now cached in the browser.
- Remove reliance on module-id specific route mounting.
