# @mussonindustrial/embr-js-chart-js

## 3.0.7

## 3.0.6

### Patch Changes

- ae20d04: Fake changeset

## 3.0.5

## 3.0.4

## 3.0.3

## 3.0.2

### Patch Changes

- 151a940: (ApexCharts Component) Use type specific `PropertyTree` accessors.
- 151a940: (ApexCharts Component) Specify that the default schema should contain an empty `series` array.
  - This resolves an issue that would occur when rendering a Radar chart with non-persistent bindings. https://forum.inductiveautomation.com/t/musson-industrial-s-embr-charts-module/91618/271

## 3.0.1

### Patch Changes

- Updated dependencies [0db6cfc]
- Updated dependencies [0db6cfc]
- Updated dependencies [0db6cfc]
  - @embr-js/perspective-client@0.6.0

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

- Updated dependencies [36a7970]
  - @embr-js/perspective-client@0.5.0

## 2.2.6

### Patch Changes

- cded1e1: Update `chart.js` dependency to `4.4.9`.

## 2.2.5

## 2.2.4

### Patch Changes

- Updated dependencies [b2f5657]
  - @embr-js/perspective-client@0.4.0

## 2.2.3

### Patch Changes

- a670956: Add `chartjs-plugin-dragdata` plugin by @artus9033.
  - Add schema definition for `options.plugins.dragData`
  - Update license to include attribution.

## 2.2.2

## 2.2.1

### Patch Changes

- 4709891: Add `events.chart.lifecycle` callback functions, tied to the lifecycle of the chart reference.
- 8bb076d: Move common property transform logic to shared library.
- 4709891: Move common schema to `common` package
- Updated dependencies [8bb076d]
- Updated dependencies [4709891]
  - @embr-js/perspective-client@0.3.0

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
  - @embr-js/perspective-client@0.2.2

## 2.1.1

### Patch Changes

- a44e437: (`Chart.js`, Fix) Properly register Hierarchical scale with from plugin `chartjs-plugin-hierarchical`.

## 2.1.0

### Minor Changes

- 8921225: Include `@embr-js/perspective-client` and `@embr-js/utils` as run time dependencies.

### Patch Changes

- Updated dependencies [8921225]
  - @embr-js/perspective-client@0.2.1

## 2.0.4

## 2.0.3

### Patch Changes

- 0500c22: Bump `Chart.js` dependency to `4.4.8`.

## 2.0.2

### Patch Changes

- 39d5957: Add 'timestack' scale by @jkmnt (https://github.com/jkmnt/chartjs-scale-timestack).

  Update License.

## 2.0.1

## 2.0.0

## 1.4.2

## 1.4.1

## 1.4.0

## 1.3.5

## 0.4.2

### Patch Changes

- db13393: Update Chart.js dependency to 4.4.4

## 0.4.1

### Patch Changes

- 9c45437: Fix handling of empty/non-existent data.dataset properties.

## 0.4.0

### Minor Changes

- 6e4abb8: Retarget to Ignition SDK version 8.1.33 (first version on Java 17)

## 0.3.0

### Minor Changes

- 944e17a: Introduced new Nx based build system and CI workflow. Version bumping and patch notes are powered by changesets, with automatic publishing on the main branch.

## 0.2.0

### Minor Changes

- 7f30b05: refactored build system to support changesets

### Patch Changes

- 7f30b05: commit on changeset
