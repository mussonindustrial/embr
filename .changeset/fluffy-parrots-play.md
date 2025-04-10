---
'@embr-modules/periscope': patch
---

(FlexRepeaterPlus) Improve ViewModel caching.

- Move ViewModel caching from the instance level to the component level, allowing the ViewModel reference to be retained for the lifetime of the component.
- Previously, a ViewModel instance was only cached for the lifetime of its associated InstancePropsHandler, and not much care was taken to remember InstancePropsHandlers.

This resolves a bug that would occur when simultaneously (in a single update to `props.instances`):

1. Moving existing instances.
2. Adding new instances.
3. Changing the final size of the instances array.