import { ComponentMeta, ComponentRegistry } from '@inductiveautomation/perspective-client';
import { SimpleComponent, SimpleComponentMeta } from './components/SimpleComponent';

export { SimpleComponent };

// as new components are implemented, import them, and add their meta to this array
const components: Array<ComponentMeta> = [
    new SimpleComponentMeta()
];

// iterate through our components, registering each one with the registry.  Don't forget to register on the Java side too!
components.forEach((c: ComponentMeta) => ComponentRegistry.register(c) );
