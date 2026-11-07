---
'@embr-modules/periscope': minor
'@embr-modules/periscope-designer-web': minor
---

(Client Resource) Upgrade the TypeScript editor with a project-wide TypeScript language service.

- Accurate, type-aware syntax highlighting for TypeScript and TSX.
- Completions across client resources, with automatic imports, and completions for `@/` resource paths and `system:` modules in imports.
- Ctrl+click (or F12) goes to a definition, in this resource or another, including the resource an import names.
- Hover information, signature help, symbol highlights and Find References (Shift+F12).
- Type checking across resources, typed against the DOM, React, Periscope's system modules and Perspective's client library (the `PerspectiveClient` global, and `perspective.context`). Type errors are reported but never block saving.
- Quick fixes (Alt+Enter), Organize Imports (Shift+Alt+O), Format (Shift+Alt+F), and inline find and replace (Ctrl+F / Ctrl+R).
- An outline of the resource's declarations, Go to Symbol across resources (Ctrl+T), inlay hints for types and parameter names, automatic closing and linked renaming of JSX tags, and formatting as you type (on `;`, `}` and Enter).
- The Run JavaScript action's script editor gets the same language features, checked as JavaScript, with `self` typed as the component's `ComponentStore`.
- Imports the browser can't load, such as `react` or other packages, are reported as errors; type-only imports of their types are allowed.
- `import { resource } from 'system:periscope'` now loads the resource function instead of `undefined`.
