# @mussonindustrial/embr-charts

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
