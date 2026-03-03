---
'@embr-modules/periscope': minor
---

Introduce `system.perspective.invokeOnQueue`.

This function schedules a runnable for execution on the Perspective session’s execution queue.

- The runnable must accept a single parameter representing the requested scope element.
  - This enables interaction across sessions.
- The optional `scope` parameter ensures the specified scope remains available at execution time.
  - For example, `scope='view'` ensures the runnable will only be executed if the originating view is still active.