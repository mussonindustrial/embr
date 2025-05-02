# @mussonindustrial/embr-js-chart-js

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
