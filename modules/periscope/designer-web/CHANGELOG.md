# @embr-modules/periscope-designer-web

## 0.17.1

### Patch Changes

- @embr-modules/periscope-web@0.17.1

## 0.17.0

### Minor Changes

- 023c58b: (Client Resource) Upgrade the TypeScript editor with a project-wide TypeScript language service.
  - Accurate, type-aware syntax highlighting for TypeScript and TSX.
  - Completions across client resources, with automatic imports, and completions for `@/` resource paths and `system:` modules in imports.
  - Ctrl+click (or F12) goes to a definition, in this resource or another, including the resource an import names.
  - Hover information, signature help, symbol highlights and Find References (Shift+F12).
  - Type checking across resources, typed against the DOM, React, Periscope's system modules and Perspective's client library (the `PerspectiveClient` global, and `perspective.context`). Type errors are reported but never block saving.
  - Quick fixes (Alt+Enter), Organize Imports (Shift+Alt+O), Format (Shift+Alt+F), and inline find and replace (Ctrl+F / Ctrl+R).
  - An outline of the resource's declarations, Go to Symbol across resources (Ctrl+T), inlay hints for types and parameter names, automatic closing and linked renaming of JSX tags, and formatting as you type (on `;`, `}` and Enter).
  - The Run JavaScript action's script editor gets the same language features, checked as JavaScript, with `self` typed as the component's `ComponentStore`.
  - `react`, `react-dom`, `mobx`, `mobx-react-lite` and `@inductiveautomation/perspective-client` can be imported, with automatic imports, and load the copies Perspective already has on the page. Imports of other packages are reported as errors; type-only imports of their types are allowed.
  - `import { resource } from 'system:periscope'` now loads the resource function instead of `undefined`.

### Patch Changes

- @embr-modules/periscope-web@0.17.0

## 0.16.5

## 0.16.4

## 0.16.3

## 0.16.2

## 0.16.1

### Patch Changes

- 05f93b2: Assign correct `groupId`.

## 0.16.0

## 0.15.0

## 0.14.1

## 0.14.0

### Minor Changes

- 760c0eb: (Client Resource) Introduce Periscope Client Resource

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
