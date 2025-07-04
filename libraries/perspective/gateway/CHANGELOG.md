# @embr-jvm/perspective-gateway

## 0.9.0

### Minor Changes

- 36a7970: **(JavaScript Proxy)** `getJavaScriptProxy` no longer requires a `propertyName`.

  Previously, `getJavaScriptProxy(propertyName)` allowed a component delegate to proxy multiple properties. However, since users couldn't interact with multiple proxy targets simultaneously, this design proved ineffective—requiring multiple proxy objects for multiple properties.

  Now, a component delegate may only return a single proxied object. This encourages bundling proxyable state into one object, improving usability for component consumers.

  The `getJavaScriptProxy(propertyName)` overload is still supported, but the `propertyName` is ignored.

## 0.8.0

### Minor Changes

- a8b2973: Add shared Perspective component loading utilities.

## 0.7.4

### Patch Changes

- a01a32e: Add the ability to manually register a ViewModel using `ViewLoader.addView(viewId, model)`.

## 0.7.3

### Patch Changes

- 464e49a: Change `ComponentDelegateJavaScriptProxy` timeout from 30,000 seconds to 30 seconds.

## 0.7.2

## 0.7.1

### Patch Changes

- 7f90e53: Introduce `ComponentDelegateJavaScriptProxy`, a class usable by a ComponentModelDelegate to request JavaScript execution in the client.
- 7f90e53: Move JavaScript error types to Perspective library.

## 0.7.0

## 0.6.0

### Minor Changes

- 2daa7d5: Add utility methods for writing/subscribing to ViewModel params.

## 0.5.0

### Minor Changes

- 0a9d03e: Added ViewLoader, for finding/loading Perspective ViewModels.
