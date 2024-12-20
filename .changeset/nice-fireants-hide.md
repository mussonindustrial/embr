---
'@embr-modules/charts': minor
---

(Feature) Added `component.getJavaScriptProxy(property)` component function.
This function allows you to access a JavaScriptProxy object by property name, then run client-side JavaScript against it.
Current proxy-able properties: 
- `chart`: Chart.js chart instance.

Example: 
```python
# Component Event
chart = self.getJavaScriptProxy('chart')
chart.runAsync("() => this.resetZoom('resize')")
```
