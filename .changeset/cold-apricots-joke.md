---
'@embr-modules/thermo': patch
---

Reduce startup/shutdown logging levels. Ignition's default logging already logs these events, so we are effectively double logging for no reason.
