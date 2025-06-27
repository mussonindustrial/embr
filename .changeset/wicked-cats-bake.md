---
'@embr-modules/charts-web': patch
'@embr-modules/charts': patch
---

(ApexCharts Component) Specify that the default schema should contain an empty `series` array.
- This resolves an issue that would occur when rendering a Radar chart with non-persistent bindings. https://forum.inductiveautomation.com/t/musson-industrial-s-embr-charts-module/91618/271