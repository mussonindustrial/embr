# @mussonindustrial/embr-js-utils

## 0.6.1

### Patch Changes

- 3b22851: Pin all JavaScript dependencies to _exact_ version matches, with minor bumps from current versions.
- 3b22851: Move development and common dependencies to the root `package.json`.

## 0.6.0

### Minor Changes

- 7f90e53: (BREAKING) Move Ignition client specific utility functions to `@embr-js/perspective-client`.
- 7f90e53: (BREAKING) Major changes in `toUserScript` parsing.
  1. Strict mode is now enabled.
  2. Function body now follows standard arrow function syntax. If the body of the arrow function is a block (contained within brackets {...}) then the body **MUST** use the _return_ keyword. If the body of the arrow function is not a block (like `() => 1 + 2`) then the body **MUST NOT** use the return keyword.

## 0.5.0

### Minor Changes

- 4d7f150: (BREAKING) `toUserScript` now throws an error if function parsing fails. This is a breaking change from the previous behavior.
- 4d7f150: (`toUserScript`) Add support for `async` function parsing.
  (`isAsyncFunction`) Add utility function for matching `async` functions.

## 0.4.0

### Minor Changes

- fa2f2e7: `toUserScript` now supplies global context first when calling the generated function. This ensures that the global context is always correct and available when function parameters are omitted.

## 0.3.0

### Minor Changes

- 944e17a: Introduced new Nx based build system and CI workflow. Version bumping and patch notes are powered by changesets, with automatic publishing on the main branch.

## 0.2.0

### Minor Changes

- 7f30b05: refactored build system to support changesets

### Patch Changes

- 7f30b05: commit on changeset
