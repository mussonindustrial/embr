---
'@embr-js/perspective-client': patch
'@embr-js/utils': patch
'@embr-modules/periscope-web': patch
'@embr-modules/charts-web': patch
---

Refactor to properly distribute functions between `perspective-client` and `utils`. `utils` should be usable by any React project, and `perspective-client` should contain all helpers that are specific to Perspective.
