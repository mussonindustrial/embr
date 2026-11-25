import type { ESModule } from '@embr-js/utils/types'
import {
  ClientState,
  ClientStore,
} from '@inductiveautomation/perspective-client'

import { readComponent, createPeriscopeApi, ProjectComponent } from '@/api'
import {
  ClientBundle,
  ClientResourceDefinition,
  requestHash,
  takeClientBundle,
} from './clientBundle'
import { routes } from './routes'

const COMPONENT_RESOURCE_TYPE = 'component'
const SYSTEM_PREFIX = 'system:'

const PROTOCOL = {
  REFRESH: 'periscope-client-resource-refresh',
}

function preloadModules(urls: string[]) {
  urls.forEach((href) => {
    const link = document.createElement('link')
    link.rel = 'modulepreload'
    link.href = href
    document.head.appendChild(link)
  })
}

export class ClientResourceStore {
  private bundle?: Promise<ClientBundle>
  private ready?: Promise<void>
  private disconnected = false

  readonly resources = new Map<string, ESModule>()
  readonly systemModules = new Map<string, object>()
  readonly components = new Map<string, ProjectComponent>()

  private readonly resourcePromises = new Map<
    string,
    Promise<ESModule | null>
  >()

  clientStore?: ClientStore

  async initialize(clientStore: ClientStore) {
    this.clientStore = clientStore
    this.bundle = takeClientBundle(clientStore.projectName)
    this.installScriptingGlobals()
    this.setupSystemModules()
    this.installListener()
    this.ready = this.preload()
    await this.ready
  }

  async getComponent(id: string): Promise<ProjectComponent | null> {
    await this.ready
    return this.components.get(id) ?? null
  }

  async getComponents(): Promise<Map<string, ProjectComponent>> {
    await this.ready
    return this.components
  }

  async resource(path: string): Promise<ESModule> {
    const resource = await this.getResource(path)

    if (resource == null) {
      throw new Error(`Resource '${path}' not found.`)
    }

    return resource
  }

  getSystemModule(name: string): object {
    return this.systemModules.get(name) ?? {}
  }

  async getResource(path: string): Promise<ESModule | null> {
    const cached = this.resources.get(path)

    if (cached != null) {
      return cached
    }

    let promise = this.resourcePromises.get(path)

    if (promise == null) {
      promise = this.fetchResource(path)
      this.resourcePromises.set(path, promise)
    }

    return promise
  }

  private resourcePath(hash: string, path: string) {
    return routes.clientResource(this.clientStore!.projectName, hash, path)
  }

  private async fetchResource(path: string): Promise<ESModule | null> {
    try {
      const { manifest, urls } = await this.bundle!

      const resource = manifest.resources.find(
        (resource) => resource.path === path
      )

      if (!resource) {
        return null
      }

      const module = await import(
        urls.get(path) ?? this.resourcePath(manifest.hash, path)
      )

      this.resources.set(path, module)

      return module
    } catch (error) {
      console.error(`Failed to load ClientResource '${path}':`, error)

      return null
    } finally {
      this.resourcePromises.delete(path)
    }
  }

  private importedUrls({ manifest, urls }: ClientBundle, paths: string[]) {
    const resourcesByPath = new Map(
      manifest.resources.map((resource) => [resource.path, resource])
    )
    const visited = new Set<string>()
    const sharedUrls: string[] = []
    const localUrls: string[] = []

    const visit = (specifier: string) => {
      if (visited.has(specifier)) {
        return
      }

      visited.add(specifier)

      const resource = resourcesByPath.get(specifier)

      if (resource != null) {
        if (!urls.has(specifier)) {
          localUrls.push(this.resourcePath(manifest.hash, specifier))
        }

        resource.imports?.forEach(visit)
      } else if (specifier.startsWith(SYSTEM_PREFIX)) {
        const name = specifier.slice(SYSTEM_PREFIX.length)
        sharedUrls.push(routes.systemModule(manifest.hash, name))
      } else {
        sharedUrls.push(routes.globalModule(manifest.hash, specifier))
      }
    }

    paths.forEach(visit)

    return [...sharedUrls, ...localUrls]
  }

  private registerComponent(
    { path, component }: ClientResourceDefinition,
    module: ESModule | null
  ) {
    if (module == null || component == null || component.id === '') {
      return
    }

    if (this.components.has(component.id)) {
      console.warn(
        `Cannot register component '${component.id}' from '${path}', it is already defined.`
      )
      return
    }

    try {
      const projectComponent = readComponent(module)
      projectComponent.displayName = component.name || component.id
      this.components.set(component.id, projectComponent)
    } catch (error) {
      console.warn(`Cannot load component '${path}'.`, error)
    }
  }

  private async preload() {
    const bundle = await this.bundle!

    const components = bundle.manifest.resources
      .filter((resource) => resource.type === COMPONENT_RESOURCE_TYPE)
      .sort((a, b) => a.path.localeCompare(b.path))

    preloadModules(
      this.importedUrls(
        bundle,
        components.map((resource) => resource.path)
      )
    )

    const modules = await Promise.all(
      components.map((resource) => this.getResource(resource.path))
    )
    components.forEach((resource, index) => {
      this.registerComponent(resource, modules[index])
    })

    bundle.manifest.resources.forEach((resource) => {
      void this.getResource(resource.path)
    })
  }

  /* TODO: Getting resources should be synchronous, or at least fully resolved by the first render.
     TODO: Until this is implemented, we should not expose a stable interface
  */
  private installScriptingGlobals() {
    Embr.scripting.add('periscope', 'UNSTABLE_resource', (path: string) =>
      this.resource(path)
    )
  }

  private setupSystemModules() {
    const modules = Embr.scripting.createGlobals({
      client: this.clientStore,
      page: this.clientStore?.page,
    })

    this.systemModules.set('perspective', modules['perspective'])
    this.systemModules.set('periscope', createPeriscopeApi(this))
  }

  private installListener() {
    const clientStore = this.clientStore!

    clientStore.connection.handlers.set(PROTOCOL.REFRESH, async () => {
      window.location.reload()
    })

    clientStore.registerStateReaction(ClientState.RUNNING_DISCONNECTED, () => {
      this.disconnected = true
    })

    clientStore.registerStateReaction(ClientState.RUNNING_CONNECTED, () => {
      if (this.disconnected) {
        this.disconnected = false
        void this.reloadIfChanged()
      }
    })
  }

  private async reloadIfChanged() {
    try {
      const { manifest } = await this.bundle!
      const hash = await requestHash(this.clientStore!.projectName)

      if ((hash ?? '') !== manifest.hash) {
        window.location.reload()
      }
    } catch (error) {
      console.warn('Cannot check client resources after reconnecting.', error)
    }
  }
}

declare module '@embr-js/perspective-client' {
  interface EmbrStoreExtensions {
    clientResources: ClientResourceStore
  }
}
