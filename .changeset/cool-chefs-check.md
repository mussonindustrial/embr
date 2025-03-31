---
'@embr-modules/periscope': minor
'@embr-modules/periscope-web': minor
---

Add Perspective client toast feature powered by https://github.com/fkhadra/react-toastify.

This feature is accessed through the JavaScript scoped object `periscope.toast` using the Python function `system.perspective.runJavaScriptAsync`.

Example:
```python
system.perspective.runJavaScriptAsync('''() => {		
    periscope.toast('This is a toast!')
}''')
```

`periscope.toast()` is directly mapped to `react-toastify`'s `toast()` object, enabling all features of the library.

For full documentation, see https://fkhadra.github.io/react-toastify
