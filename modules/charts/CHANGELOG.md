# @mussonindustrial/embr-charts

## 3.1.0

### Minor Changes

- 9216f8b: (Chart.js Component) - Reperform property transformations when the chart's parent is changed.
  - This allows CSS properties to correctly resolve to the parent element.
  - Resolves #354.

### Patch Changes

- Updated dependencies [9216f8b]
  - @embr-modules/charts-web@3.1.0

## 3.0.10

### Patch Changes

- 4c53074: ApexCharts (Legacy) - Fix bug #347 that would cause the chart to not detect changes to series/options.
  - This patch updates the legacy component to use the change detection mechanism from the non-legacy component.
- Updated dependencies [4c53074]
- Updated dependencies [3b22851]
- Updated dependencies [3b22851]
  - @embr-modules/charts-web@3.0.10

## 3.0.9

### Patch Changes

- be9954e: Fake changeset
  - @embr-modules/charts-web@3.0.9

## 3.0.8

### Patch Changes

- dcf5a8c: Fake changeset
  - @embr-modules/charts-web@3.0.8

## 3.0.7

### Patch Changes

- 92c744f: Fake changeset.
  - @embr-modules/charts-web@3.0.7

## 3.0.6

### Patch Changes

- ae20d04: Fake changeset
- Updated dependencies [ae20d04]
  - @embr-modules/charts-web@3.0.6

## 3.0.5

### Patch Changes

- ef7ae2a: Fake changeset
  - @embr-modules/charts-web@3.0.5

## 3.0.4

### Patch Changes

- 0426b64: Fake changeset
  - @embr-modules/charts-web@3.0.4

## 3.0.3

### Patch Changes

- 1a866c9: Fake changeset
  - @embr-modules/charts-web@3.0.3

## 3.0.2

### Patch Changes

- 151a940: (ApexCharts Component) Use type specific `PropertyTree` accessors.
- 151a940: (ApexCharts Component) Specify that the default schema should contain an empty `series` array.
  - This resolves an issue that would occur when rendering a Radar chart with non-persistent bindings. https://forum.inductiveautomation.com/t/musson-industrial-s-embr-charts-module/91618/271
- Updated dependencies [151a940]
- Updated dependencies [151a940]
  - @embr-modules/charts-web@3.0.2

## 3.0.1

### Patch Changes

- @embr-modules/charts-web@3.0.1

## 3.0.0

### Major Changes

- 36a7970: **(JavaScript Proxy)** `getJavaScriptProxy` no longer requires a `propertyName`.

  Previously, `getJavaScriptProxy(propertyName)` allowed a component delegate to proxy multiple properties. However, since users couldn't interact with multiple proxy targets simultaneously, this design proved ineffective—requiring multiple proxy objects for multiple properties.

  Now, a component delegate may only return a single proxied object. This encourages bundling proxyable state into one object, improving usability for component consumers.

  The `getJavaScriptProxy(propertyName)` overload is still supported, but the `propertyName` is ignored.

- 36a7970: Add ApexCharts (Legacy) component.
  - This component is a one-for-one replacement of the ApexCharts component from the [Kyvis-Labs/ignition-apexcharts-module](https://github.com/Kyvis-Labs/ignition-apexcharts-module).
  - This component matches the behavior, features, and property schema of the original Kyvis-Labs component version `1.0.23`.
  - If both the Kyvis-Labs module and Embr-Charts are installed simultaneously, Embr-Charts will register and use the ApexCharts (Legacy) component in place of the Kyvis-Labs version, effectively overriding it to ensure compatibility and consistency.

- 36a7970: Add `ApexCharts` component.
  - This is a new implementation of the `ApexCharts` charting library as an Ignition component.
  - Benefits over the `Legacy` component include:
    - Simplified rendering lifecycle (i.e. quicker to render/update)
    - JavaScript proxy support for direct chart interaction.
    - Improved designer property schema support.
    - Expanded selection of default component variants.

### Patch Changes

- 36a7970: Migrate from deprecated `moduleDependencies` to supported `moduleDependencySpecs` in `build.gradle.kts`.
- Updated dependencies [36a7970]
- Updated dependencies [36a7970]
- Updated dependencies [36a7970]
- Updated dependencies [36a7970]
  - @embr-modules/charts-web@3.0.0
  - @embr-jvm/perspective-gateway@0.9.0
  - @embr-jvm/perspective-designer@0.9.0
  - @embr-jvm/perspective-common@0.9.0

## 2.2.6

### Patch Changes

- cded1e1: Update `chart.js` dependency to `4.4.9`.
- Updated dependencies [cded1e1]
- Updated dependencies [a8b2973]
  - @embr-modules/charts-web@2.2.6
  - @embr-jvm/perspective-designer@0.8.0
  - @embr-jvm/perspective-gateway@0.8.0
  - @embr-jvm/perspective-common@0.8.0
  - @embr-jvm/core-common@0.7.0
  - @embr-jvm/core-designer@0.7.0

## 2.2.5

### Patch Changes

- 40ad4a7: Fix Perspective component schema validation errors on startup. #265
  - This patch uses `DelegatedClassLoader` to simultaneously resolve schema definitions from both Perspective's and our own resources.

- Updated dependencies [40ad4a7]
  - @embr-jvm/core-common@0.6.1
  - @embr-jvm/core-designer@0.6.1
  - @embr-modules/charts-web@2.2.5

## 2.2.4

### Patch Changes

- @embr-modules/charts-web@2.2.4

## 2.2.3

### Patch Changes

- a670956: Add `chartjs-plugin-dragdata` plugin by @artus9033.
  - Add schema definition for `options.plugins.dragData`
  - Update license to include attribution.
- Updated dependencies [a670956]
  - @embr-modules/charts-web@2.2.3

## 2.2.2

### Patch Changes

- acb6c82: Add `options.scale[id].position` property to the designer schema.
- feb0383: Add `options.scale[scaleId].bounds` property to the designer schema.
  - @embr-modules/charts-web@2.2.2

## 2.2.1

### Patch Changes

- 4709891: Add `events.chart.lifecycle` callback functions, tied to the lifecycle of the chart reference.
- 4709891: Move common schema to `common` package
- Updated dependencies [4709891]
- Updated dependencies [8bb076d]
- Updated dependencies [4709891]
  - @embr-modules/charts-web@2.2.1

## 2.2.0

### Minor Changes

- 69904f1: Deprecate component property `events.beforeRender`.

  This property has been replaced by `events.lifecycle.onUpdate`.

### Patch Changes

- 69904f1: Add DOM and Lifecycle event properties.

  Two new component events categories have been provided; DOM events and Lifecycle events.
  - Lifecycle events: `onMount`, `onRender`, and `onUnmount` events.
  - DOM events: `onCopy`, `onCut`, `onPaste`, `onCompositionEnd`, `onCompositionStart`, `onCompositionUpdate`, `onFocus`, `onBlur`, `onChange`, `onBeforeInput`, `onInput`, `onReset`, `onSubmit`, `onInvalid`, `onLoad`, `onError`, `onKeyDown`, `onKeyPress`, `onKeyUp`, `onAbort`, `onCanPlay`, `onCanPlayThrough`, `onDurationChange`, `onEmptied`, `onEncrypted`, `onEnded`, `onLoadedData`, `onLoadedMetadata`, `onPause`, `onPlay`, `onPlaying`, `onProgress`, `onRateChange`, `onResize`, `onSeeked`, `onSeeking`, `onStalled`, `onSuspend`, `onTimeUpdate`, `onVolumeChange`, `onWaiting`, `onAuxClick`, `onClick`, `onContextMenu`, `onDoubleClick`, `onDrag`, `onDragEnd`, `onDragEnter`, `onDragExit`, `onDragLeave`, `onDragOver`, `onDragStart`, `onDrop`, `onMouseDown`, `onMouseEnter`, `onMouseLeave`, `onMouseMove`, `onMouseOut`, `onMouseOver`, `onMouseUp`, `onSelect`, `onTouchCancel`, `onTouchEnd`, `onTouchMove`, `onTouchStart`, `onPointerDown`, `onPointerMove`, `onPointerUp`, `onPointerCancel`, `onPointerEnter`, `onPointerLeave`, `onPointerOver`, `onPointerOut`, `onScroll`, `onWheel`, `onAnimationStart`, `onAnimationEnd`, `onAnimationIteration`, and `onTransitionEnd`.

- Updated dependencies [69904f1]
- Updated dependencies [69904f1]
  - @embr-modules/charts-web@2.2.0

## 2.1.1

### Patch Changes

- a44e437: (`Chart.js`, Fix) Adjust `Chart.js` property schema to allow for objects as labels. This is needed for `chartjs-plugin-hierarchical`.
- a44e437: (`Chart.js`, Fix) Properly register Hierarchical scale with from plugin `chartjs-plugin-hierarchical`.
- Updated dependencies [a44e437]
  - @embr-modules/charts-web@2.1.1

## 2.1.0

### Patch Changes

- Updated dependencies [8921225]
  - @embr-modules/charts-web@2.1.0

## 2.0.4

### Patch Changes

- Updated dependencies [a01a32e]
  - @embr-jvm/perspective-gateway@0.7.4
  - @embr-jvm/perspective-common@0.7.4
  - @embr-jvm/perspective-designer@0.7.4
  - @embr-modules/charts-web@2.0.4

## 2.0.3

### Patch Changes

- Updated dependencies [464e49a]
- Updated dependencies [0500c22]
  - @embr-jvm/perspective-gateway@0.7.3
  - @embr-modules/charts-web@2.0.3
  - @embr-jvm/perspective-common@0.7.3
  - @embr-jvm/perspective-designer@0.7.3

## 2.0.2

### Patch Changes

- 39d5957: Add 'timestack' scale by @jkmnt (https://github.com/jkmnt/chartjs-scale-timestack).

  Update License.

- Updated dependencies [39d5957]
  - @embr-modules/charts-web@2.0.2

## 2.0.1

### Patch Changes

- Updated dependencies [730396c]
  - @embr-jvm/perspective-common@0.7.2
  - @embr-jvm/perspective-designer@0.7.2
  - @embr-jvm/perspective-gateway@0.7.2
  - @embr-modules/charts-web@2.0.1

## 2.0.0

### Major Changes

- 7f90e53: (BREAKING) New scriptable properties `this` and global context.

  In scriptable property functions:
  1.  `this` is now a reference to the component itself.
  2.  Global variables `self` and `client` have been removed.
      - These have been replaced with a Perspective specific namespace, accessible through the global `perspective` object.
      - This object contains a `context` that can be used to access the `clientStore` (i.e. `perspective.context.client`).
      -     - This object will be the home of future Perspective specific utilities.

- 7f90e53: (BREAKING) Major changes in `toUserScript` parsing.
  1. Strict mode is now enabled.
  2. Function body now follows standard arrow function syntax. If the body of the arrow function is a block (contained within brackets {...}) then the body **MUST** use the _return_ keyword. If the body of the arrow function is not a block (like `() => 1 + 2`) then the body **MUST NOT** use the return keyword.

### Minor Changes

- 7f90e53: (Feature) Added `component.getJavaScriptProxy(property)` component function.
  This function allows you to access a JavaScriptProxy object by property name, then run client-side JavaScript against it.
  Current proxy-able properties:
  - `chart`: Chart.js chart instance.

  Example:

  ```python
  # Component Event
  chart = self.getJavaScriptProxy('chart')
  chart.runAsync("() => this.resetZoom('resize')")
  ```

- 7f90e53: (Chart.js) Update `chartjs-zoom-plugin` to 2.2.0

### Patch Changes

- 7f90e53: (Dependencies) Update `chartjs-chart-funnel` to `4.2.4`
  (Dependencies) Update `chartjs-chart-geo` to `4.3.4`
  (Dependencies) Update `chartjs-chart-graph` to `4.3.4`
  (Dependencies) Fix `chartjs-chart-matrix` to `2.0.1`
  (Dependencies) Update `chartjs-chart-pcp` to `4.3.4`
  (Dependencies) Update `chartjs-chart-sankey` to `0.13.0`
  (Dependencies) Update `chartjs-chart-treemap` to `3.1.0`
  (Dependencies) Update `chartjs-chart-venn` to `4.3.5`
  (Dependencies) Update `chartjs-chart-wordcloud` to `4.4.4`
  (Dependencies) Update `chartjs-plugin-annotation` to `3.1.0`
  (Dependencies) Update `chartjs-plugin-autocolors` to `0.3.1`
  (Dependencies) Fix `chartjs-plugin-crosshair` to `2.0.0`
  (Dependencies) Fix `chartjs-plugin-datalabels` to `2.2.0`
  (Dependencies) Fix `chartjs-plugin-gradient` to `0.6.1`
  (Dependencies) Update `chartjs-plugin-hierarchical` to `4.4.4`
  (Dependencies) Update `chartjs-plugin-stacked100` to `1.7.0`
  (Dependencies) Update `chartjs-plugin-zoom` to `2.2.0`
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
  - @embr-jvm/core-common@0.6.0
  - @embr-jvm/perspective-gateway@0.7.1
  - @embr-jvm/perspective-designer@0.7.1
  - @embr-jvm/perspective-common@0.7.1
  - @embr-jvm/core-designer@0.6.0
  - @embr-modules/charts-web@2.0.0

## 1.4.2

### Patch Changes

- Updated dependencies [4d7f150]
  - @embr-jvm/core-common@0.5.0
  - @embr-modules/charts-web@1.4.2
  - @embr-jvm/core-designer@0.5.0

## 1.4.1

### Patch Changes

- 6f46916: (chore) Correct package.json dependencies to included `perspective-common`, `perspective-designer`, and `perspective-gateway` references.
- Updated dependencies [6f46916]
  - @embr-jvm/perspective-common@0.7.0
  - @embr-jvm/perspective-designer@0.7.0
  - @embr-jvm/perspective-gateway@0.7.0
  - @embr-modules/charts-web@1.4.1

## 1.4.0

### Minor Changes

- 60d382e: (Web) Memoize property transforms.
- 60d382e: Add optional `updateMode` and `redraw` component properties.

### Patch Changes

- 60d382e: (Web) Reorganize javascript exports.
- 60d382e: (Web) Register `TreeChart` and `TreeController` controllers from `chartjs-chart-graph`.
  - @embr-modules/charts-web@1.4.0

## 1.3.5

### Patch Changes

- 0a9d03e: Move Perspective component descriptor extensions to shared libraries.
- Updated dependencies [0a9d03e]
  - @embr-jvm/core-common@0.4.1
  - @embr-jvm/core-designer@0.4.1
  - @embr-modules/charts-web@1.3.5

## 1.3.4

### Patch Changes

- db13393: Move Embr `chart-js` dependency into Embr Charts project and renamed to `charts-web`
- Updated dependencies [db13393]
  - @embr-js/chart-js@0.4.2

## 1.3.3

### Patch Changes

- 02a59ba: republish previously pulled patch release

## 1.3.2

### Patch Changes

- bd7e4b6: chore: update ignition module plugin
- Updated dependencies [9c45437]
  - @embr-js/chart-js@0.4.1

## 1.3.1

### Patch Changes

- Updated dependencies [9182d09]
- Updated dependencies [9182d09]
  - @embr-jvm/core-designer@0.4.0
  - @embr-jvm/core-common@0.4.0

## 1.3.0

### Minor Changes

- 6e4abb8: Retarget to Ignition SDK version 8.1.33 (first version on Java 17)

### Patch Changes

- Updated dependencies [6e4abb8]
  - @embr-js/chart-js@0.4.0
  - @embr-jvm/core-common@0.3.0
  - @embr-jvm/core-designer@0.3.0

## 1.2.1

### Patch Changes

- 48279e9: republished to github and npm

## 1.2.0

### Minor Changes

- 944e17a: Introduced new Nx based build system and CI workflow. Version bumping and patch notes are powered by changesets, with automatic publishing on the main branch.

### Patch Changes

- Updated dependencies [944e17a]
  - @mussonindustrial/embr-js-chart-js@0.3.0
  - @mussonindustrial/embr-jvm-core-common@0.2.0
  - @mussonindustrial/embr-jvm-core-designer@0.2.0

## 1.1.1

### Patch Changes

- Updated dependencies [7f30b05]
- Updated dependencies [003ec12]
- Updated dependencies [bb93e9f]
- Updated dependencies [7f30b05]
  - @mussonindustrial/embr-js-chart-js@0.2.0
  - @mussonindustrial/embr-jvm-core-common@0.1.1
  - @mussonindustrial/embr-jvm-core-designer@0.1.1
