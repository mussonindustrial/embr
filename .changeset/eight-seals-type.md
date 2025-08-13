---
'@embr-modules/charts-web': patch
'@embr-modules/charts': patch
---

ApexCharts (Legacy) - Fix bug #347 that would cause the chart to not detect changes to series/options.
- This patch updates the legacy component to use the change detection mechanism from the non-legacy component.