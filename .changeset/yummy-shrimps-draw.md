---
'@embr-modules/periscope': patch
---

(Client Resource): Fix 404 errors for inherited resources.
- Inherited resources were visible in the manifest, but not accessible through the resource handler endpoint.