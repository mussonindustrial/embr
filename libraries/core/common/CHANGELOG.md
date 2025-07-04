# @mussonindustrial/embr-jvm-core-common

## 0.7.0

### Minor Changes

- a8b2973: Add shared Perspective component loading utilities.

## 0.6.1

### Patch Changes

- 40ad4a7: Introduce `DelegatedClassLoader`, which will search through a list of `ClassLoaders` in order to find a class or resource.

## 0.6.0

### Minor Changes

- 7f90e53: (BREAKING) Add support to `PyArgOverload` for nullable types. Arguments are now defined using `KType`'s, and nullability checks are applied during function matching. If an argument is missing it is considered null and if the argument is allowed to be null it is considered a valid match.
- 7f90e53: (BREAKING) PyArgOverload overloads are passed function arguments as a map instead of a list. It should have been this way from the beginning, in order to reduce human error in extracting parameters.

## 0.5.0

### Minor Changes

- 4d7f150: (BREAKING) `PyArgOverloadBuilder` now works with KTypes instead of KClasses. This is to allow for nullability checks (KType contains nullability information).

## 0.4.1

### Patch Changes

- 0a9d03e: (ReflectUtils) Add methods for accessing private methods and properties through super classes.

## 0.4.0

### Minor Changes

- 9182d09: Break resources into shared libraries to support future inter-module work.

### Patch Changes

- 9182d09: New Feature: `PyArgOverload` and `PyArgOverloadBuilder` allow for creating overloaded functions using Python keyword arguments.

## 0.3.0

### Minor Changes

- 6e4abb8: Retarget to Ignition SDK version 8.1.33 (first version on Java 17)

## 0.2.0

### Minor Changes

- 944e17a: Introduced new Nx based build system and CI workflow. Version bumping and patch notes are powered by changesets, with automatic publishing on the main branch.

## 0.1.1

### Patch Changes

- 003ec12: another test patch
- bb93e9f: linting
