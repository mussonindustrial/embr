---
'@embr-js/perspective-client': minor
---

Add a mechanism for other packages to register scripting globals.

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