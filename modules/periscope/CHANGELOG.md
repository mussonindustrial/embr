# @embr-modules/periscope

## 0.16.2

### Patch Changes

- Updated dependencies [588ffb7]
  - @embr-jvm/core-common@0.9.1
  - @embr-jvm/core-designer@0.9.1
  - @embr-modules/periscope-web@0.16.2

## 0.16.1

### Patch Changes

- 05f93b2: Assign correct `groupId`.
- Updated dependencies [05f93b2]
  - @embr-modules/periscope-web@0.16.1

## 0.16.0

### Minor Changes

- f64c92d: Bump minimum compatible Ignition version from `8.1.33` to `8.1.49`.

### Patch Changes

- @embr-modules/periscope-web@0.16.0

## 0.15.0

### Minor Changes

- 8c76341: Bump minimum compatible Ignition version from `8.1.33` to `8.1.49`.

### Patch Changes

- @embr-jvm/core-common@0.9.0
- @embr-jvm/core-designer@0.9.0
- @embr-modules/periscope-web@0.15.0

## 0.14.1

### Patch Changes

- 0e6b25f: Resolve NPE related to `ClientResourceManager` initialization for `ClientResourceWorkspace`'s Welcome Page.
  - @embr-modules/periscope-web@0.14.1

## 0.14.0

### Minor Changes

- 760c0eb: Refactor Embr global stores.
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

- Updated dependencies [fc61ddd]
- Updated dependencies [760c0eb]
- Updated dependencies [88f332a]
- Updated dependencies [760c0eb]
- Updated dependencies [760c0eb]
  - @embr-jvm/perspective-gateway@0.9.2
  - @embr-jvm/core-common@0.8.2
  - @embr-jvm/core-designer@0.8.2
  - @embr-modules/periscope-web@0.14.0
  - @embr-jvm/perspective-common@0.9.2
  - @embr-jvm/perspective-designer@0.9.2

## 0.13.0

### Minor Changes

- 9fae5d4: Introduce an optional licensing model.

  ## Embr Module Licensing
  - To offer a more complete support model, Musson Industrial is introducing optional licensing for the Embr suite.
  - Don't worry, these modules will always remain free to use and open source.

  Here's what's changing:

  **Standard Edition**
  - Embr modules are free to use but will run in trial mode when unlicensed.
  - **No functionality will be impacted by an expired trial.**
  - You can remove the trial status by purchasing a low-cost license from our web store.

  **Maker Edition**
  - Embr modules are free to use with no trial mode.
    Licenses can be purchased at the [Musson Industrial Web Store](https://mussonindustrial.com/store).

  ## Expected Questions
  - **Q:** Is Embr still open source?
  - **A:** Yes! The entire Embr suite is still MIT-licensed, and this will never change.
  - **Q:** Can I continue to use Embr Charts/Periscope/Thermo/SNMP for free?
  - **A:** Yes! The modules will report as being in trial mode, but no functionality will be impacted. You will still continue to receive updates.
  - **Q:** Why should I purchase a license?
  - **A:** A license removes the trial banner and supports continued development of the Embr suite, helping ensure these modules remain useful, reliable, and actively maintained.
  - **Q:** What if I upgrade from 8.1 to 8.3?
  - **A:** License keys are not tied to a specific Ignition version and will never expire.
  - **Q:** Will you offer redundant gateway, integrator, or bulk purchase discounts?
  - **A:** We aim to keep licensing simple, affordable, and automated, so there are currently no additional discounts.
  - **Q:** I have questions regarding licenses, special setups, or long-term support guarantees.
  - **A:** We're here for you! For setups that fall outside the standard automated licensing process, please reach out so we can provide guidance.

### Patch Changes

- Updated dependencies [9fae5d4]
- Updated dependencies [9fae5d4]
- Updated dependencies [9fae5d4]
  - @embr-jvm/core-designer@0.8.1
  - @embr-jvm/core-common@0.8.1
  - @embr-modules/periscope-web@0.13.0

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
- Updated dependencies [2aa9142]
- Updated dependencies [06455f1]
- Updated dependencies [afa89e6]
- Updated dependencies [dd8a25b]
- Updated dependencies [dd8a25b]
- Updated dependencies [cb55367]
  - @embr-modules/periscope-web@0.12.0

## 0.11.0

### Minor Changes

- 7376256: Introduce `system.perspective.invokeOnQueue`.

  This function schedules a runnable for execution on the Perspective session’s execution queue.
  - The runnable must accept a single parameter representing the requested scope element.
    - This enables interaction across sessions.
  - The optional `scope` parameter ensures the specified scope remains available at execution time.
    - For example, `scope='view'` ensures the runnable will only be executed if the originating view is still active.

### Patch Changes

- Updated dependencies [7376256]
  - @embr-jvm/perspective-gateway@0.9.1
  - @embr-jvm/perspective-common@0.9.1
  - @embr-jvm/perspective-designer@0.9.1
  - @embr-modules/periscope-web@0.11.0

## 0.10.1

### Patch Changes

- 35bf7c2: `PyArgOverload`: Enforce strong return typing.
- Updated dependencies [2d6a1ad]
- Updated dependencies [35bf7c2]
- Updated dependencies [e619625]
  - @embr-jvm/core-common@0.8.0
  - @embr-jvm/core-designer@0.8.0
  - @embr-modules/periscope-web@0.10.1

## 0.10.0

### Minor Changes

- fc47d7e: Modules no longer include `kotlin-stdlib`.
  - All modules now rely on the Kotlin standard library provided with Ignition.
- 7def5bd: (`Flex Repeater +`): Clear non-running `ViewModel` instances from cache on access.

### Patch Changes

- c2ecc40: (`Flex Repeater +`): Fix a bug causing the creation of extra ViewModels for non-existent view instances.
- Updated dependencies [f36eaee]
  - @embr-jvm/core-common@0.7.1
  - @embr-jvm/core-designer@0.7.1
  - @embr-modules/periscope-web@0.10.0

## 0.9.0

### Minor Changes

- 6a74904: Rename `style.css` to `embr-periscope.css` to match new Vite defaults.

### Patch Changes

- Updated dependencies [6a74904]
- Updated dependencies [3b22851]
- Updated dependencies [3b22851]
  - @embr-modules/periscope-web@0.9.0

## 0.8.2

### Patch Changes

- 92c744f: Fake changeset.
  - @embr-modules/periscope-web@0.8.2

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
