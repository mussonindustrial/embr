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
import { installExtensions, JavaScriptActionFactory } from './extensions'
import { waitForClientStore } from '@embr-js/perspective-client'

export {
  FlexRepeaterComponent,
  SwiperComponent,
  EmbeddedViewComponent,
  JsonViewComponent,
  PortalComponent,
  ReactComponent,
}

const components = [
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
  new EmbeddedViewComponentMeta(),
  new JsonViewComponentMeta(),
  new PortalComponentMeta(),
  new ReactComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))

const actions = [JavaScriptActionFactory]
actions.forEach((a) => ActionRegistry.register(a))

waitForClientStore().then(async (clientStore) => {
  await installExtensions(clientStore)
})
