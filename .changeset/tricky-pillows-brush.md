---
'@embr-modules/periscope-web': minor
'@embr-modules/periscope': minor
---

Add an `importmap` script tag to Perspective on load.

- This `importmap` allows for easily loading resource from the Web Library.
  - `@library/${path}` is mapped directly into the Web Library.

This example loads a file from the Web Library (`/data/periscope/web-library/my-folder/testing.js`) and executes a function that it exports:

```python
system.perspective.runJavaScriptAsync('''async () => {
    const { exampleFunction } = await import('@library/my-folder/testing.js')
    exampleFunction()
}''')
```