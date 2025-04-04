---
'@embr-js/perspective-client': patch
---

Increase the frequency used for `waitForClientStore` polling.

- The previous frequency allowed time for startup events to trigger before Periscope's `installImportMap` could run.