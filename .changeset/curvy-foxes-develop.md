---
'@embr-modules/periscope': minor
'@embr-modules/periscope-web': minor
---

Add `perspective.createView(props)` JavaScript-scoped function.

This helper function enabled easier creation of views in user supplied JavaScript.
The minimum set of parameters provided by the user is:
- `resourcePath` - Path to the view.
- `mountPath` - The unique mount path of the view. Must be stable.

Example:
```javascript
(viewPath, options) => {		
    periscope.toast(({ toastProps, isPaused }) => {
        return perspective.createView({
            resourcePath: viewPath,
            mountPath: `toast-${toastProps.toastId}`,
            params: {
                text: 'Embedded View!',
            }
        })
    }, options)
}
```