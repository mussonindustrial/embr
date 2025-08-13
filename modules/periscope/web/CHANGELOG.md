# @mussonindustrial/embr-js-chart-js

## 0.9.0

### Minor Changes

- 09a404b: Rename `style.css` to `embr-periscope.css` to match new Vite defaults.

### Patch Changes

- b59fe0c: Pin all JavaScript dependencies to _exact_ version matches, with minor bumps from current versions.
- b59fe0c: Move development and common dependencies to the root `package.json`.
- Updated dependencies [b59fe0c]
- Updated dependencies [b59fe0c]
  - @embr-js/perspective-client@0.6.1
  - @embr-js/utils@0.6.1

## 0.8.1

### Patch Changes

- Updated dependencies [0db6cfc]
- Updated dependencies [0db6cfc]
- Updated dependencies [0db6cfc]
  - @embr-js/perspective-client@0.6.0

## 0.8.0

### Minor Changes

- 36a7970: **(JavaScript Proxy)** `getJavaScriptProxy` no longer requires a `propertyName`.

  Previously, `getJavaScriptProxy(propertyName)` allowed a component delegate to proxy multiple properties. However, since users couldn't interact with multiple proxy targets simultaneously, this design proved ineffective—requiring multiple proxy objects for multiple properties.

  Now, a component delegate may only return a single proxied object. This encourages bundling proxyable state into one object, improving usability for component consumers.

  The `getJavaScriptProxy(propertyName)` overload is still supported, but the `propertyName` is ignored.

### Patch Changes

- Updated dependencies [36a7970]
  - @embr-js/perspective-client@0.5.0

## 0.7.5

### Patch Changes

- 8ae60a4: (Swiper Component) Add JavaScript proxy support via `component.getJavaScriptProxy('swiper')`.

## 0.7.4

## 0.7.3

### Patch Changes

- 3ea4d36: (FlexRepeaterPlus) Add instance `key` as an implicit parameter to the instance view.
- 3b53009: (Toasts) Move `pointerEvents` setting from inline styles to CSS.
  - This makes it easier for users to use the `style` property of the toast function.
  - Users no longer need to add `pointerEvents: 'all'` to every inline style definition.

## 0.7.2

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
