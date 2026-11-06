---
'@embr-modules/charts-web': patch
'@embr-modules/charts': patch
---

(ApexCharts) Re-draw the chart when its identity changes (`options.chart.id` or `options.chart.group`).
- ApexCharts expects a chart's identity to remain constant through its lifecycle. This conflicts with initial values on Perspective bindings.
- This change forces a re-draw of a chart whenever its identity changes.
- This resolves #556.