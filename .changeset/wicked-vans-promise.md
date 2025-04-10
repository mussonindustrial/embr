---
'@embr-modules/periscope': patch
'@embr-modules/periscope-web': patch
---

(Toasts) Move `pointerEvents` setting from inline styles to CSS.

- This makes it easier for users to use the `style` property of the toast function.
- Users no longer need to add `pointerEvents: 'all'` to every inline style definition.