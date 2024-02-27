import {
    ComponentMeta,
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import {
    ExampleComponent,
    ExampleComponentMeta,
} from './components/ExampleComponent'

export { ExampleComponent }

const components: Array<ComponentMeta> = [new ExampleComponentMeta()]

components.forEach((c: ComponentMeta) => ComponentRegistry.register(c))
