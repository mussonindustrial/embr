# @embr-js/perspective-client

## 0.6.1

### Patch Changes

- 3b22851: Pin all JavaScript dependencies to _exact_ version matches, with minor bumps from current versions.
- 3b22851: Move development and common dependencies to the root `package.json`.

## 0.6.0

### Minor Changes

- 0db6cfc: Add new deep comparison hooks:
  - `useDeepCompareCallback`
  - `useDeepCompareLayoutEffect`
- 0db6cfc: Remove `useCustomCompareEffect` hook.
- 0db6cfc: Reimplement existing deep comparison hooks:
  - `useDeepCompareEffect`
  - `useDeepCompareMemo` resolves #305

## 0.5.0

### Minor Changes

- 36a7970: Move `useRefLifecycleEvents` away from default export.

## 0.4.0

### Minor Changes

- b2f5657: Add a mechanism for other packages to register scripting globals.
  - Using `getEmbrGlobals()`, other packages can access a global namespace used by Embr.
  - Keys to the `scripting.globals` object will be made available in all user-supplied JavaScript functions.

  Usage Example:

  ```typescript
  const embrGlobals = getEmbrGlobals()
  merge(embrGlobals.scripting.globals, {
    periscope: {
      toast,
    },
  })
  ```

## 0.3.0

### Minor Changes

- 8bb076d: Move common property transform logic to shared library.

### Patch Changes

- 4709891: Move common schema to `common` package

## 0.2.2

### Patch Changes

- 69904f1: Add `useDomEvents`, `useLifecycleEvents`, and `useComponentEvents` hooks.

  These hooks give a standardized interface for registering React DOM and Lifecycle (`onMount`, `onUnmount`, `onUpdate`) events.

## 0.2.1

### Patch Changes

- 8921225: (Scripting Globals) Add `perspective.sendMessage` function for firing UserScope Perspective messages.

## 0.2.0

### Minor Changes

- 7f90e53: Initial publish.
