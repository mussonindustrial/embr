import { ClientStore } from '@inductiveautomation/perspective-client'

const PROTOCOL = {
  REFRESH: 'periscope-client-resource-refresh',
}

export type ClientResourceDefinition = {
  path: string
  hash: string
  type: string
}

export type ESModule<T = unknown> = {
  [K in string]: T
} & {
  default?: T
}

export type ResourceManifest = {
  hash: string
  resources: ClientResourceDefinition[]
}

export type ResourceManifestResponse = {
  data: ResourceManifest
}

export class ClientResourceStore {
  private manifest?: ResourceManifest
  private manifestPromise?: Promise<ResourceManifest>

  readonly resources = new Map<string, ESModule>()
  readonly systemModules = new Map<string, object>()

  private readonly resourcePromises = new Map<
    string,
    Promise<ESModule | null>
  >()

  urlBase = '/data/embr-periscope/client-resource'

  clientStore?: ClientStore

  async initialize(clientStore: ClientStore) {
    this.clientStore = clientStore
    this.installScriptingGlobals()
    this.setupSystemModules()
    this.installListener()
    await this.preload()
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

  private manifestPath() {
    return `${this.urlBase}/${this.clientStore!.projectName}/manifest.json`
  }

  private resourcePath(hash: string, path: string) {
    return `${this.urlBase}/${this.clientStore!.projectName}/${hash}/${encodeURI(path)}`
  }

  private async getManifest(): Promise<ResourceManifest> {
    if (this.manifest) {
      return this.manifest
    }

    this.manifestPromise ??= this.fetchManifest()

    return this.manifestPromise
  }

  private async fetchManifest(): Promise<ResourceManifest> {
    const response = await fetch(this.manifestPath())

    if (!response.ok) {
      throw new Error(`Failed to fetch manifest: ${response.statusText}`)
    }

    const json = (await response.json()) as ResourceManifestResponse

    this.manifest = json.data

    return this.manifest
  }

  private async fetchResource(path: string): Promise<ESModule | null> {
    try {
      const manifest = await this.getManifest()

      const resource = manifest.resources.find(
        (resource) => resource.path === path
      )

      if (!resource) {
        return null
      }

      const module = await import(this.resourcePath(manifest.hash, path))

      this.resources.set(path, module)

      return module
    } catch (error) {
      console.error(`Failed to load ClientResource '${path}':`, error)

      return null
    } finally {
      this.resourcePromises.delete(path)
    }
  }

  private async preload() {
    const manifest = await this.getManifest()

    await Promise.all(
      manifest.resources.map((resource) => {
        let promise = this.resourcePromises.get(resource.path)

        if (promise == null) {
          promise = this.fetchResource(resource.path)
          this.resourcePromises.set(resource.path, promise)
        }

        return promise
      })
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
    this.systemModules.set('periscope', modules['periscope'])
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
