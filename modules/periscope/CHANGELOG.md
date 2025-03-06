# @embr-modules/periscope

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
