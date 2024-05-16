# Embr Chart.js Module [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

An Ignition module that adds [Chart.js] powered charting components for Ignition Perspective.

> [!IMPORTANT]
> This repo is currently in active development, and breaking changes are to be expected.

## Getting Started
1. Download the [latest version] from [releases].
2. Install the module through the Ignition Gateway web interface.

## Features

### Scriptable Options
Any component property value containing a `return` statement will be converted into a JavaScript function.
The function has access to two parameters:
1. `context` - The context object is used to give contextual information when resolving options and currently only applies to scriptable options. The object is preserved, so it can be used to store and pass information between calls.
    - There are multiple levels of context objects.
      - `chart -> dataset -> data`
      - `chart -> scale -> (tick, pointLabel)`
      - `chart -> toolip`
    - Each level inherits its parent(s) and any contextual information stored in the parent is available through the child.
2. `options` - A resolver that can be used to access other options in the same context.

See [ChartJs Documentation - Scriptable Options](https://www.chartjs.org/docs/latest/general/options.html#scriptable-options) for full details.

#### Scriptable Options Example
```js
// Conditionally change the background color for a series. 
{
  "datasets": [
    {
      "data": [...],
      "label": "Dataset",
      "backgroundColor": "return context.dataIndex > 1 ? 'red' : 'blue'"
    }
  ]
}
```



## Module Documentation
- `#TODO` [Complete module documentation][documentation]
- [Chart.js documentation][Chart.js documentation]

## Changelog

The [changelog](https://github.com/mussonindustrial/embr/blob/main/modules/embr-chart-js/CHANGELOG.md) is regularly updated to reflect what's changed in each new release.

## Copyright and Licensing

Copyright (C) 2023 Musson Industrial

Free use of this software is granted under the terms of the MIT License.

[embr]: https://github.com/mussonindustrial/embr
[releases]: https://github.com/mussonindustrial/embr/releases
[documentation]: https://docs.mussonindustrial.com/
[latest version]: https://github.com/mussonindustrial/embr/releases/download/embr-chart-js-0.1.3-SNAPSHOT/Embr-Chartjs-module.modl
[Chart.js]: https://www.chartjs.org/
[Chart.js documentation]: https://www.chartjs.org/docs/latest/
