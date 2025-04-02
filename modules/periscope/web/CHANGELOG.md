# @mussonindustrial/embr-js-chart-js

## 0.7.1

## 0.7.0

### Minor Changes

- b2f5657: Add Perspective client toast feature powered by https://github.com/fkhadra/react-toastify.

  This feature is accessed through the JavaScript scoped object `periscope.toast` using the Python function `system.perspective.runJavaScriptAsync`.

  Example:

  ```python
  system.perspective.runJavaScriptAsync('''() => {
      periscope.toast('This is a toast!')
  }''')
  ```

  `periscope.toast()` is directly mapped to `react-toastify`'s `toast()` object, enabling all features of the library.

  For full documentation, see https://fkhadra.github.io/react-toastify

- b2f5657: Add `perspective.createView(props)` JavaScript-scoped function.

  This helper function enabled easier creation of views in user supplied JavaScript.
  The minimum set of parameters provided by the user is:

  - `resourcePath` - Path to the view.
  - `mountPath` - The unique mount path of the view. Must be stable.

  Example:

  ```javascript
  ;(viewPath, options) => {
    periscope.toast(({ toastProps, isPaused }) => {
      return perspective.createView({
        resourcePath: viewPath,
        mountPath: `toast-${toastProps.toastId}`,
        params: {
          text: 'Embedded View!',
        },
      })
    }, options)
  }
  ```

### Patch Changes

- Updated dependencies [b2f5657]
  - @embr-js/perspective-client@0.4.0

## 0.6.0

### Minor Changes

- dde1698: Add `Portal` component.

### Patch Changes

- 4709891: Move common schema to `common` package
- Updated dependencies [8bb076d]
- Updated dependencies [4709891]
  - @embr-js/perspective-client@0.3.0

## 0.5.6

### Patch Changes

- Updated dependencies [69904f1]
  - @embr-js/perspective-client@0.2.2

## 0.5.5

### Patch Changes

- Updated dependencies [8921225]
  - @embr-js/perspective-client@0.2.1

## 0.5.4

### Patch Changes

- a01a32e: Add `View Json` component. Allows rendering a view from a `view.json` schema, enabling dunamic view creation from inside of Ignition.

## 0.5.3

## 0.5.2

## 0.5.1

### Patch Changes

- 7a3d56e: (FlexRepeater+) Apply emitted style properties _after_ flex container specific styles. Fixes Issue #218.

## 0.5.0

### Patch Changes

- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
  - @embr-js/perspective-client@0.2.0
  - @embr-js/utils@0.6.0

## 0.4.4

## 0.4.3

## 0.4.1

## 0.4.0

## 0.2.0

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
