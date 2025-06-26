# @embr-modules/periscope

## 0.8.1

### Patch Changes

- 48ac511: (JsonView Component) Do not attempt to initialize a view without a root component.
  - @embr-modules/periscope-web@0.8.1

## 0.8.0

### Patch Changes

- 36a7970: Migrate from deprecated `moduleDependencies` to supported `moduleDependencySpecs` in `build.gradle.kts`.
- Updated dependencies [36a7970]
- Updated dependencies [36a7970]
  - @embr-jvm/perspective-gateway@0.9.0
  - @embr-modules/periscope-web@0.8.0
  - @embr-jvm/perspective-designer@0.9.0
  - @embr-jvm/perspective-common@0.9.0

## 0.7.5

### Patch Changes

- 8ae60a4: (Swiper Component) Add JavaScript proxy support via `component.getJavaScriptProxy('swiper')`.
- Updated dependencies [8ae60a4]
  - @embr-modules/periscope-web@0.7.5

## 0.7.4

### Patch Changes

- Updated dependencies [a8b2973]
  - @embr-jvm/perspective-designer@0.8.0
  - @embr-jvm/perspective-gateway@0.8.0
  - @embr-jvm/perspective-common@0.8.0
  - @embr-jvm/core-common@0.7.0
  - @embr-jvm/core-designer@0.7.0
  - @embr-modules/periscope-web@0.7.4

## 0.7.3

### Patch Changes

- 3ea4d36: (FlexRepeaterPlus) Add instance `key` as an implicit parameter to the instance view.
- 1bd2a1a: (FlexRepeaterPlus) Improve ViewModel caching.

  - Move ViewModel caching from the instance level to the component level, allowing the ViewModel reference to be retained for the lifetime of the component.
  - Previously, a ViewModel instance was only cached for the lifetime of its associated InstancePropsHandler, and not much care was taken to remember InstancePropsHandlers.

  This resolves a bug that would occur when simultaneously (in a single update to `props.instances`):

  1. Moving existing instances.
  2. Adding new instances.
  3. Changing the final size of the instances array.

- 3b53009: (Toasts) Move `pointerEvents` setting from inline styles to CSS.

  - This makes it easier for users to use the `style` property of the toast function.
  - Users no longer need to add `pointerEvents: 'all'` to every inline style definition.

- Updated dependencies [3ea4d36]
- Updated dependencies [3b53009]
  - @embr-modules/periscope-web@0.7.3

## 0.7.2

### Patch Changes

- 40ad4a7: Fix Perspective component schema validation errors on startup. #265

  - This patch uses `DelegatedClassLoader` to simultaneously resolve schema definitions from both Perspective's and our own resources.

- Updated dependencies [40ad4a7]
  - @embr-jvm/core-common@0.6.1
  - @embr-jvm/core-designer@0.6.1
  - @embr-modules/periscope-web@0.7.2

## 0.7.1

### Patch Changes

- 7d41dd1: Perspective Toasts have been changed to respect mounted docked views.

  - Toasts are now contained within the center viewport.
  - @embr-modules/periscope-web@0.7.1

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
- Updated dependencies [b2f5657]
  - @embr-modules/periscope-web@0.7.0

## 0.6.0

### Minor Changes

- dde1698: Add `Portal` component.

### Patch Changes

- Updated dependencies [4709891]
- Updated dependencies [dde1698]
  - @embr-modules/periscope-web@0.6.0

## 0.5.6

### Patch Changes

- @embr-modules/periscope-web@0.5.6

## 0.5.5

### Patch Changes

- @embr-modules/periscope-web@0.5.5

## 0.5.4

### Patch Changes

- a01a32e: Add `View Json` component. Allows rendering a view from a `view.json` schema, enabling dunamic view creation from inside of Ignition.
- Updated dependencies [a01a32e]
- Updated dependencies [a01a32e]
  - @embr-modules/periscope-web@0.5.4
  - @embr-jvm/perspective-gateway@0.7.4
  - @embr-jvm/perspective-common@0.7.4
  - @embr-jvm/perspective-designer@0.7.4

## 0.5.3

### Patch Changes

- 4186e27: Make all `runJavaScript` functions share the same timeout values (30 seconds).
- Updated dependencies [464e49a]
  - @embr-jvm/perspective-gateway@0.7.3
  - @embr-jvm/perspective-common@0.7.3
  - @embr-jvm/perspective-designer@0.7.3
  - @embr-modules/periscope-web@0.5.3

## 0.5.2

### Patch Changes

- 730396c: Fix #221 by injecting Periscope browser resource requirement into Perspective components.
- Updated dependencies [730396c]
  - @embr-jvm/perspective-common@0.7.2
  - @embr-jvm/perspective-designer@0.7.2
  - @embr-jvm/perspective-gateway@0.7.2
  - @embr-modules/periscope-web@0.5.2

## 0.5.1

### Patch Changes

- 7a3d56e: (FlexRepeater+) Apply emitted style properties _after_ flex container specific styles. Fixes Issue #218.
- Updated dependencies [7a3d56e]
  - @embr-modules/periscope-web@0.5.1

## 0.5.0

### Minor Changes

- 7f90e53: (BREAKING) New `this` and global context.

  In `system.perspective.runJavaScript~` functions:

  1. `this` is now a reference to the `clientStore`.
  2. A new Perspective specific namespace, accessible through the global `perspective` object is provided.
     - This object contains a `context` that can be used to access the `clientStore` (i.e. `perspective.context.client`).
     - This object will be the home of future Perspective specific utilities.

- 7f90e53: (BREAKING) Major changes in `toUserScript` parsing.
  1. Strict mode is now enabled.
  2. Function body now follows standard arrow function syntax. If the body of the arrow function is a block (contained within brackets {...}) then the body **MUST** use the _return_ keyword. If the body of the arrow function is not a block (like `() => 1 + 2`) then the body **MUST NOT** use the return keyword.

### Patch Changes

- 7f90e53: (`runJavaScript` Functions) More robust error handling. Errors are now properly logged in the gateway for both Async and Blocking calls.

  (`runJavaScriptAsync`) The original view, page, and session thread local variables are now correctly restored before running the callback function.

- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
  - @embr-jvm/core-common@0.6.0
  - @embr-jvm/perspective-gateway@0.7.1
  - @embr-jvm/perspective-designer@0.7.1
  - @embr-jvm/perspective-common@0.7.1
  - @embr-jvm/core-designer@0.6.0
  - @embr-modules/periscope-web@0.5.0

## 0.4.4

### Patch Changes

- 0302b5e: (Fix) Reduce `runJavaScript` timeout from 30,000 seconds to 30 seconds.
  - @embr-modules/periscope-web@0.4.4

## 0.4.3

### Patch Changes

- 4d7f150: (Scripting) Added `system.perspective.runJavaScriptBlocking()` and `system.perspective.runJavaScriptAsync()` functions.
- Updated dependencies [4d7f150]
  - @embr-jvm/core-common@0.5.0
  - @embr-modules/periscope-web@0.4.3
  - @embr-jvm/core-designer@0.5.0

## 0.4.1

### Patch Changes

- 6f46916: (chore) Correct package.json dependencies to included `perspective-common`, `perspective-designer`, and `perspective-gateway` references.
- Updated dependencies [6f46916]
  - @embr-jvm/perspective-common@0.7.0
  - @embr-jvm/perspective-designer@0.7.0
  - @embr-jvm/perspective-gateway@0.7.0
  - @embr-modules/periscope-web@0.4.1

## 0.4.0

### Minor Changes

- 2daa7d5: (Embedded View +): Introduce `Embedded View +` component.
- 2daa7d5: (Flex Repeater +): Rewrite `Flex Repeater +` based on experiences learned from `Embedded View +`.

### Patch Changes

- @embr-modules/periscope-web@0.4.0

## 0.2.0

### Minor Changes

- 0a9d03e: Initial Release (Flex Repeater + and Swiper components)

### Patch Changes

- 0a9d03e: Move Perspective component descriptor extensions to shared libraries.
- Updated dependencies [0a9d03e]
  - @embr-jvm/core-common@0.4.1
  - @embr-jvm/core-designer@0.4.1
  - @embr-modules/periscope-web@0.2.0
