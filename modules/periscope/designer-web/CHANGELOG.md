# @embr-modules/periscope-designer-web

## 0.14.1

## 0.14.0

### Minor Changes

- 4675d8f: (Client Resource) Introduce Periscope Client Resource

  Client Resources are files compiled to JavaScript that are loaded by the Perspective client on launch.

  There are currently two supported types:
  1. **TypeScript** (supports JavaScript, TypeScript, JSX, and TSX)
  2. **CSS Modules**

  All Client Resources run on page load/refresh, serving as a direct, robust alternative to Markdown Injection. They can also provide React components, which can be used via a new dedicated React Perspective component or anywhere you would normally use an `EmbeddedView`.

  #### Technical Details:
  - **ES Module Delivery:** Each Client Resource is served to the client as an ES Module.
  - **Aggressive Caching:** Client Resources are fully cached by the client and are available immediately on disk when it is time to render.
  - **Cache Busting:** The gateway performs URL rewriting when serving resources to handle cache busting.
  - **Hash-Based Invalidation:** URLs are rewritten using a combined hash of all Client Resources in the current project. If no resources are modified, they remain cached by the client indefinitely. If any single resource changes, all resources will be re-downloaded to ensure consistency.
