import {
  ActionRegistry,
  ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import {
  SwiperComponent,
  SwiperComponentMeta,
  FlexRepeaterComponent,
  FlexRepeaterComponentMeta,
  EmbeddedViewComponent,
  EmbeddedViewComponentMeta,
  JsonViewComponent,
  JsonViewComponentMeta,
  PortalComponent,
  PortalComponentMeta,
  ReactComponent,
  ReactComponentMeta,
} from './components'
import {
  installExtensions,
  installLifecycleEvents,
  installProjectComponents,
  JavaScriptActionFactory,
} from './extensions'
import { waitForClientStore } from '@embr-js/perspective-client'
import { prefetchClientBundle } from './stores'

export {
  FlexRepeaterComponent,
  SwiperComponent,
  EmbeddedViewComponent,
  JsonViewComponent,
  PortalComponent,
  ReactComponent,
}

prefetchClientBundle()

const components = [
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
  new EmbeddedViewComponentMeta(),
  new JsonViewComponentMeta(),
  new PortalComponentMeta(),
  new ReactComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))

installProjectComponents()

const actions = [JavaScriptActionFactory]
actions.forEach((a) => ActionRegistry.register(a))

installLifecycleEvents()

waitForClientStore().then(async (clientStore) => {
  await installExtensions(clientStore)
})
