---
'@embr-modules/periscope': minor
'@embr-modules/periscope-designer-web': minor
---

(React Component) Add Component client resources, which add React components to Perspective as component types of their own.

- A Component resource holds one component made with `defineComponent`. Its Component ID, name and Palette Path are set on the resource, so moving or renaming it never changes the type views use.
- `prop` and `event` describe a component's props and events, which the Designer's property editor and event configuration show. Props are typed from their description.
- A component with `position` props is a container. Its `ChildSlot` renders the children, and the Designer drops, moves, reorders and resizes children in it.
- `Root` applies Perspective's position, style, classes and DOM events to the component's outermost element, and `useProp` reads and writes a prop.
- Variants are ready-made configurations of a component, shown under it in the palette.
- The Designer's React Components palette groups components by their Palette Path, with search, pinned and recent components, and opens a component's definition. A component with no Palette Path, or marked hidden, is left out.
- A component can be rendered as a React element inside another.
- A Component resource can have a delegate, written in Python, that runs on the Gateway for each instance. `create(component)` returns an object whose `onStartup`, `onShutdown` and `handleEvent` are called as a module's delegate would be, and whose other methods can be called from scripts. The component reaches it through `delegate`, with `fireEvent` and `onEvent`.
- Client resources now load from a single cached bundle.
