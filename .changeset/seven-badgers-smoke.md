---
'@embr-modules/charts-web': minor
'@embr-modules/charts': minor
---

Expose chart component's `ComponentDelegate`.
- Provide's access through `this.delegate` in all props callback functions.
- Importantly, this allows access to the underlying charting component through `this.delegate.proxy.ref`.