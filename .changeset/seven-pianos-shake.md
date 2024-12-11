---
'@embr-modules/periscope': patch
---

(`runJavaScript` Functions) More robust error handling. Errors are now properly logged in the gateway for both Async and Blocking calls.

(`runJavaScriptAsync`) The original view, page, and session thread local variables are now correctly restored before running the callback function.
