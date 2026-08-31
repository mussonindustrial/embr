# @mussonindustrial/embr-js-chart-js

## 0.16.3

## 0.16.2

## 0.16.1

### Patch Changes

- 05f93b2: Assign correct `groupId`.

## 0.16.0

## 0.15.0

## 0.14.1

## 0.14.0

### Minor Changes

- 760c0eb: (Client Resource) Introduce Periscope Client Resource

  Client Resources are files compiled to JavaScript that are loaded by the Perspective client on launch.

  There are currently two supported types:
  1. **TypeScript** (supports JavaScript, TypeScript, JSX, and TSX)
  2. **CSS Modules**

  All Client Resources run on page load/refresh, serving as a direct, robust alternative to Markdown Injection. They can also provide React components, which can be used via a new dedicated React Perspective component or anywhere you would normally use an `EmbeddedView`.

  #### Technical Details:
  - **ES Module Delivery:** Each Client Resource is served to the client as an ES Module.
  - **Aggressive Caching:** Client Resources are fully cached by the client and are available immediately on disk when it is time to render.
  - **Cache Busting:** The gateway performs URL rewriting when serving resources to handle cache busting.
  - **Hash-Based Invalidation:** URLs are rewritten using a combined hash of all Client Resources in the current project. If no resources are modified, they remain cached by the client indefinitely. If any single resource changes, all resources will be re-downloaded to ensure consistency.

- 760c0eb: (React Component) Add `React` component. This component allows you to render a Client Resource as a React component.

### Patch Changes

- Updated dependencies [760c0eb]
  - @embr-js/perspective-client@0.7.0

## 0.13.0

## 0.12.0

### Minor Changes

- dd8a25b: `runJavaScript` functions now expose `perspective.context.component`. This is the client-side `ComponentModel` of the component that made the `runJavaScript` function call.
  - This is `undefined` when targeting a specific `session`/`page`.
- dd8a25b: `runJavaScript` functions now expose `perspective.context.view`, the client-side `ViewModel` of the view that made the `runJavaScript` call.
  - This is `undefined` when targeting a specific `session`/`page`.

### Patch Changes

- 2aa9142: Toast container now smoothly transitions when docks are open/closed.
- 06455f1: Remove dock padding when screen size suggests a mobile device. This allows the toast to span the full device width when docks are present on mobile.
- afa89e6: Move z-index to the toast root container. Resolves #435.
- cb55367: Refactor to properly distribute functions between `perspective-client` and `utils`. `utils` should be usable by any React project, and `perspective-client` should contain all helpers that are specific to Perspective.
- Updated dependencies [dd8a25b]
- Updated dependencies [ca09383]
- Updated dependencies [cb55367]
  - @embr-js/perspective-client@0.6.2
  - @embr-js/utils@0.6.2

## 0.11.0

## 0.10.1

## 0.10.0

## 0.9.0

### Minor Changes

- 6a74904: Rename `style.css` to `embr-periscope.css` to match new Vite defaults.

### Patch Changes

- 3b22851: Pin all JavaScript dependencies to _exact_ version matches, with minor bumps from current versions.
- 3b22851: Move development and common dependencies to the root `package.json`.
- Updated dependencies [3b22851]
- Updated dependencies [3b22851]
  - @embr-js/perspective-client@0.6.1
  - @embr-js/utils@0.6.1

## 0.8.2

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
