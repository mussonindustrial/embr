---
'@embr-modules/periscope': minor
---

Introduce `system.perspective.invokeLater`.

This function schedules a runnable for execution on the Perspective session’s execution queue.

- If `delay` is `0`, the runnable is appended to the end of the queue.
- The optional `scope` parameter ensures that the specified scope remains available at execution time.
    - For example, `scope='view'` ensures the originating view is still active when the runnable executes.