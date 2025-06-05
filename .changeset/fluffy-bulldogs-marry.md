---
'@embr-modules/charts-web': major  
'@embr-modules/charts': major  
'@embr-jvm/perspective-gateway': minor  
'@embr-modules/periscope-web': minor
---

**(JavaScript Proxy)** `getJavaScriptProxy` no longer requires a `propertyName`.

Previously, `getJavaScriptProxy(propertyName)` allowed a component delegate to proxy multiple properties. However, since users couldn't interact with multiple proxy targets simultaneously, this design proved ineffective—requiring multiple proxy objects for multiple properties.

Now, a component delegate may only return a single proxied object. This encourages bundling proxyable state into one object, improving usability for component consumers.

The `getJavaScriptProxy(propertyName)` overload is still supported, but the `propertyName` is ignored.
