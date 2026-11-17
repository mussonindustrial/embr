import type { ESModule } from '@embr-js/utils/types'
import { ClientStore } from '@inductiveautomation/perspective-client'

import {
  createPeriscopeApi,
  keyOf,
  LibraryComponent,
  libraryOf,
} from '../api/periscopeApi'
import {
  type ClientBundle,
  clientResourceUrl,
  takeClientBundle,
} from './clientBundle'

const COMPONENT_LIBRARY = 'component-library'
const SYSTEM_MODULE_BASE = '/data/embr-periscope/system-module'
const GLOBAL_MODULE_BASE = '/data/embr-periscope/global-module'
const SYSTEM_PREFIX = 'system:'

const PROTOCOL = {
  REFRESH: 'periscope-client-resource-refresh',
}

function preloadModules(urls: string[]) {
  for (const href of urls) {
    const link = document.createElement('link')
    link.rel = 'modulepreload'
    link.href = href
    document.head.appendChild(link)
  }
}

export class ClientResourceStore {
  private loading?: Promise<ClientBundle>
  private loaded?: Promise<void>

  readonly resources = new Map<string, ESModule>()
  readonly systemModules = new Map<string, object>()
  readonly components = new Map<string, LibraryComponent>()

  private readonly resourcePromises = new Map<
    string,
    Promise<ESModule | null>
  >()

  clientStore?: ClientStore

  async initialize(clientStore: ClientStore) {
    this.clientStore = clientStore
    this.loading = takeClientBundle(clientStore.projectName)
    this.installScriptingGlobals()
    this.setupSystemModules()
    this.installListener()
    this.loaded = this.preload()
    await this.loaded
  }

  private registerLibrary(path: string, module: ESModule) {
    const { namespace, components } = libraryOf(module)

    for (const component of components) {
      const key = keyOf(namespace, component.periscope.id)
      const existing = this.components.get(key)

      if (existing == null) {
        this.components.set(key, component)
      } else if (existing !== component) {
        console.error(`Component '${key}' is already defined ('${path}')`)
      }
    }
  }

  async libraryComponent(key: string): Promise<LibraryComponent | undefined> {
    await this.loaded
    return this.components.get(key)
  }

  async libraryComponents(): Promise<Map<string, LibraryComponent>> {
    await this.loaded
    return this.components
  }

  async resource(path: string): Promise<ESModule> {
    const resource = await this.getResource(path)

    if (resource == null) {
      throw new Error(`Resource '${path}' not found`)
    }

    return resource
  }

  public getSystemModule(name: string): object | undefined {
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
    return clientResourceUrl(this.clientStore!.projectName, hash, path)
  }

  private async fetchResource(path: string): Promise<ESModule | null> {
    try {
      const { manifest, urls } = await this.loading!

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
        sharedUrls.push(`${SYSTEM_MODULE_BASE}/${manifest.hash}/${name}`)
      } else {
        sharedUrls.push(`${GLOBAL_MODULE_BASE}/${manifest.hash}/${specifier}`)
      }
    }

    paths.forEach(visit)

    return [...sharedUrls, ...localUrls]
  }

  private async loadLibrary(path: string) {
    const module = await this.getResource(path)

    if (module == null) {
      return
    }

    try {
      this.registerLibrary(path, module)
    } catch (error) {
      console.error(`Error loading component library '${path}'`, error)
    }
  }

  private async preload() {
    const bundle = await this.loading!

    const libraryPaths = bundle.manifest.resources
      .filter((resource) => resource.type === COMPONENT_LIBRARY)
      .map((resource) => resource.path)

    preloadModules(this.importedUrls(bundle, libraryPaths))

    await Promise.all(libraryPaths.map((path) => this.loadLibrary(path)))

    void Promise.all(
      bundle.manifest.resources.map((resource) =>
        this.getResource(resource.path)
      )
    )
  }

  /* TODO: Getting resources should be synchronous, or at least fully resolved by the first render.
     TODO: Until this is implemented, we should not expose a stable interface
  */
  private installScriptingGlobals() {
    Embr.scripting.add(
      'periscope',
      'UNSTABLE_resource',
      async (path: string) => {
        const resource = await this.getResource(path)

        if (resource == null) {
          throw new Error(`Resource '${path}' not found`)
        }

        return resource
      }
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
    this.clientStore!.connection.handlers.set(PROTOCOL.REFRESH, async () => {
      window.location.reload()
    })
  }
}

declare module '@embr-js/perspective-client' {
  interface EmbrStoreExtensions {
    clientResources: ClientResourceStore
  }
}
