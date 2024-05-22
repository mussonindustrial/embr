# Embr Charts Module [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

An Ignition module that adds a collection of enhanced Perspective charting components.

> [!IMPORTANT]
> This repo is currently in active development, and breaking changes are to be expected.

## Getting Started
1. Download the [latest version] from [releases].
2. Install the module through the Ignition Gateway web interface.

## Features

### [Chart.js] Component

![chart-js.png](./docs/examples/chart-js.png)

#### Plugin Support
The Chart.js community has developed many addons and plugins. A selection of these addons are bundled with the module.

Details on configuring each plugin are outside the scope of this module's documentation. Please consult the plugin's documentation for complete details.

##### Chart Types
| Name                                                               | Description                                             | Included     |
|--------------------------------------------------------------------|---------------------------------------------------------|--------------|
| [boxplot](https://github.com/sgratzl/chartjs-chart-boxplot)        | Adds boxplot and violin plot chart type                 | ✅            |
| [error-bars](https://github.com/sgratzl/chartjs-chart-error-bars)  | Adds diverse error bar variants of standard chart types | 📋 (planned) |
| [funnel](https://github.com/sgratzl/chartjs-chart-funnel)          | Adds funnel chart type                                  | 📋 (planned) |
| [graph](https://github.com/sgratzl/chartjs-chart-graph)            | Adds graph chart types such as a force directed graph   | ✅            |
| [matrix](https://github.com/kurkle/chartjs-chart-matrix)           | Adds matrix chart type                                  | ✅            |
| [pcp](https://github.com/sgratzl/chartjs-chart-pcp)                | Adds parallel coordinates plot chart type               | ✅            |
| [sankey](https://github.com/kurkle/chartjs-chart-sankey)           | Adds sankey diagram chart type                          | ✅            |
| [stacked100](https://github.com/y-takey/chartjs-plugin-stacked100) | Draws 100% stacked bar chart                            | ✅            |
| [treemap](https://github.com/kurkle/chartjs-chart-treemap)         | Adds treemap chart type                                 | ✅            |
| [venn](https://github.com/upsetjs/chartjs-chart-venn)              | Adds venn and euler chart type                          | 📋 (planned) |
| [word-cloud](https://github.com/sgratzl/chartjs-chart-wordcloud)   | Adds word-cloud chart type                              | 📋 (planned) |

##### Plugins
###### Styling

| Name                                                              | Description                | Included     |
|-------------------------------------------------------------------|----------------------------|--------------| 
| [autocolors](https://github.com/kurkle/chartjs-plugin-autocolors) | Automatic color generation | 📋 (planned) |
| [gradient](https://github.com/kurkle/chartjs-plugin-gradient)     | Easy gradients             | ✅            |

###### Features

| Name                                                                    | Description                                                                 | Included          |
|-------------------------------------------------------------------------|-----------------------------------------------------------------------------|-------------------|
| [annotation](https://github.com/chartjs/chartjs-plugin-annotation)      | Draws lines, boxes, points, labels, polygons and ellipses on the chart area | ✅                 |
| [crosshair](https://github.com/abelheinsbroek/chartjs-plugin-crosshair) | Adds a data crosshair to line and scatter charts                            | ❌                 |
| [datalabels](https://github.com/chartjs/chartjs-plugin-datalabels)      | Displays labels on data for any type of charts                              | ✅                 |
| [hierarchical](https://github.com/sgratzl/chartjs-plugin-hierarchical)  | Adds hierarchical scales that can be collapsed, expanded, and focused       | ✅                 |
| [image-label](https://github.com/yunusemrejs/chartjs-image-label)       | Displays image labels on data for doughnut charts                           | 📋 (planned)      |

##### Interactions

| Name                                                                           | Description                                                   | Included     |
|--------------------------------------------------------------------------------|---------------------------------------------------------------|--------------|
| [a11y-legend](https://github.com/julianna-langston/chartjs-plugin-a11y-legend) | Provides keyboard accessibility for chart legends             | 📋 (planned) |
| [deferred](https://github.com/chartjs/chartjs-plugin-deferred)                 | Defers initial chart update until chart scrolls into viewport | 📋 (planned) |
| [zoom](https://github.com/chartjs/chartjs-plugin-zoom)                         | Enables zooming and panning on charts                         | ✅            |

#### Scriptable Options
##### CSS Custom Property
Any component property value starting with `var(--` will use the corresponding CSS variable's value at render time.

> **_NOTE:_** The property value is only evaluated during the render. Changing the property value will have no effect until the chart is re-rendered.

###### CSS Custom Property Example
```js
// Use var(--my-background-color) custom property
{
  "datasets": [
    {
      "data": [...],
      "label": "Dataset",
      "backgroundColor": "var(--my-background-color)"
    }
  ]
}
```


##### JavaScript Function
Any component property value beginning with a `<script>` statement will be converted into a JavaScript function.
> **_NOTE:_** The ending script tag (`</script>`) is optional. 

The function has access to two parameters:
1. `context` - The context object is used to give contextual information when resolving options and currently only applies to scriptable options. The object is preserved, so it can be used to store and pass information between calls.
    - There are multiple levels of context objects.
      - `chart -> dataset -> data`
      - `chart -> scale -> (tick, pointLabel)`
      - `chart -> toolip`
    - Each level inherits its parent(s) and any contextual information stored in the parent is available through the child.
2. `options` - A resolver that can be used to access other options in the same context.
3. `self` - A reference to the Perspective component props. This allows access to all properties on the Perspective component (i.e. `self.custom.myCustomProperty`).

See [ChartJs Documentation - Scriptable Options](https://www.chartjs.org/docs/latest/general/options.html#scriptable-options) for full details.

##### JavaScript Function Example
```js
// Conditionally change the background color for a series. 
{
  "datasets": [
    {
      "data": [...],
      "label": "Dataset",
      "backgroundColor": "<script> return context.dataIndex > 1 ? 'red' : 'blue'"
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
[Chart.js Addons]: https://github.com/chartjs/awesome
