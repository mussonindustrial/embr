import { ClientStore } from '@inductiveautomation/perspective-client'

const PROTOCOL = {
  REFRESH: 'periscope-client-resource-refresh',
}

export type ClientResourceDefinition = {
  path: string
  hash: string
  type: string
}

type ESModule<T = unknown> = {
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

export class ClientResourceManager {
  private manifest?: ResourceManifest
  private manifestPromise?: Promise<ResourceManifest>

  private resources = new Map<string, ESModule>()
  private resourcePromises = new Map<string, Promise<ESModule | null>>()

  // private ready = Promise.withResolvers<void>()

  urlBase = '/data/embr-periscope/client-resource'

  constructor(private clientStore: ClientStore) {
    // Embr.periscope.resources = this.resources
  }

  async initialize() {
    this.installGlobals()
    this.installListener()
    await this.preload()
    // this.ready.resolve()
  }

  async resource(path: string): Promise<ESModule> {
    // await this.ready.promise

    const resource = await this.getResource(path)
    if (resource == null) {
      throw new Error(`Resource '${path}' not found`)
    }

    return resource
  }

  private manifestPath() {
    return `${this.urlBase}/${this.clientStore.projectName}/manifest.json`
  }

  private resourcePath(hash: string, path: string) {
    return `${this.urlBase}/${this.clientStore.projectName}/${hash}/${encodeURI(path)}`
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

  public async getResource(path: string): Promise<ESModule | null> {
    // await this.ready.promise

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

  private installGlobals() {
    // merge(Embr.scripting.globals, {
    //   periscope: {
    //     resource: async (path: string) => {
    //       const resource = await this.getResource(path)
    //       if (resource == null) {
    //         throw new Error(`Resource '${path}' not found`)
    //       }
    //       return resource
    //     },
    //   },
    // })
    //
    // Embr.periscope.clientResourceGlobals = createScriptingGlobals({
    //   client: this.clientStore,
    //   page: this.clientStore.page,
    // })
  }

  private installListener() {
    this.clientStore.connection.handlers.set(PROTOCOL.REFRESH, async () => {
      window.location.reload()
    })
  }
}

// let managerReadyPromise: Promise<ClientResourceManager> | undefined
// export async function waitForClientResourceManager(): Promise<ClientResourceManager> {
//   if (!managerReadyPromise) {
//     managerReadyPromise = new Promise((resolve) => {
//       const interval = setInterval(() => {
//         const manager = Embr?.periscope?.clientResourceManager
//         if (manager) {
//           clearInterval(interval)
//           resolve(manager)
//         }
//       }, 0)
//     })
//   }
//
//   return await managerReadyPromise
// }
