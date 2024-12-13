# @mussonindustrial/embr-js-utils

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
