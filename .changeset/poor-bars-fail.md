---
'@embr-modules/periscope': patch
---

Introduce PyStore feature for sharing arbitrary Python and JVM objects across Perspective views, pages, and sessions.

- Add `system.perspective.pyStore(name, path [, scope])` scripting support for reading and modifying shared PyStore state.
- Add `pyStore(name, path [, scope])` expression support with reactive updates when referenced PyStore values change.
- Support view, page, and session scopes, with page as the default.
- Automatically release PyStore references when the associated Perspective scope ends.
- Support nested path access across PyStore-managed values, Python objects, mappings, sequences, Java lists and arrays, and JavaBean properties.
- Add change subscriptions and explicit touch() support for notifying bindings after objects are modified directly.
- Add deferNotifications() for grouping multiple writes into a single reactive notification while keeping writes immediately visible.
- Add one-time PyStore initialization support, allowing a store to lazily populate its initial state the first time it is accessed while preventing duplicate initialization after a successful initializer run.
