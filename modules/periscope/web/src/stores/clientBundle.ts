import { BundledModule, ModuleUrls, moduleUrls } from './moduleUrls'
import { routes } from './routes'

export type ClientResourceDefinition = {
  path: string
  hash: string
  type: string
  imports?: string[]
  component?: { id: string; name: string }
}

export type ResourceManifest = {
  hash: string
  resources: ClientResourceDefinition[]
}

export type ClientBundle = { manifest: ResourceManifest; urls: ModuleUrls }

type BundleResponse = {
  hash: string
  manifest: ResourceManifest
  modules: Record<string, BundledModule>
}

type BundleRequest = {
  abort: AbortController
  response: Promise<BundleResponse | null>
}

const HASH_KEY_PREFIX = 'embr-periscope.hash.'
const CLIENT_PAGE = /^\/data\/perspective\/client\/([^/]+)/

const EMPTY_BUNDLE: ClientBundle = {
  manifest: { hash: '', resources: [] },
  urls: new Map(),
}

let prefetched: { project: string; bundle: Promise<ClientBundle> } | undefined

function readLastHash(project: string): string | null {
  try {
    return localStorage.getItem(HASH_KEY_PREFIX + project)
  } catch {
    return null
  }
}

function rememberHash(project: string, hash: string | null) {
  try {
    if (hash == null) {
      localStorage.removeItem(HASH_KEY_PREFIX + project)
    } else {
      localStorage.setItem(HASH_KEY_PREFIX + project, hash)
    }
  } catch {
    return
  }
}

export async function requestHash(project: string): Promise<string | null> {
  const response = await fetch(routes.clientResourceHash(project), {
    cache: 'no-store',
  })

  if (!response.ok) {
    throw new Error(
      `Failed to fetch client resource hash: ${response.statusText}.`
    )
  }

  const { data } = (await response.json()) as { data: { hash?: string } }

  return data.hash ?? null
}

async function fetchBundle(
  url: string,
  hash: string,
  signal: AbortSignal
): Promise<BundleResponse | null> {
  try {
    const response = await fetch(url, { signal })

    if (!response.ok) {
      return null
    }

    const bundle = (await response.json()) as BundleResponse | null
    const isCurrent = bundle?.hash === hash && bundle.manifest?.hash === hash

    return isCurrent ? bundle : null
  } catch {
    return null
  }
}

function requestBundle(project: string, hash: string): BundleRequest {
  const abort = new AbortController()
  const url = routes.clientBundle(project, hash)

  return { abort, response: fetchBundle(url, hash, abort.signal) }
}

async function loadClientBundle(project: string): Promise<ClientBundle> {
  const lastHash = readLastHash(project)
  const cachedRequest =
    lastHash == null ? undefined : requestBundle(project, lastHash)

  try {
    const hash = await requestHash(project)

    if (hash !== lastHash) {
      cachedRequest?.abort.abort()
    }

    rememberHash(project, hash)

    if (hash == null) {
      return EMPTY_BUNDLE
    }

    const cachedBundle =
      hash === lastHash ? await cachedRequest?.response : null
    const bundle = cachedBundle ?? (await requestBundle(project, hash).response)

    if (bundle == null) {
      rememberHash(project, null)
      throw new Error(`Failed to load client bundle '${hash}'.`)
    }

    const urls = moduleUrls({
      ...bundle,
      urlOf: (path) => routes.clientResource(project, hash, path),
    })

    return { manifest: bundle.manifest, urls }
  } catch (error) {
    cachedRequest?.abort.abort()
    console.warn('Cannot load client resources.', error)

    return EMPTY_BUNDLE
  }
}

export function prefetchClientBundle() {
  const project = CLIENT_PAGE.exec(window.location.pathname)?.[1]

  if (project == null) {
    return
  }

  prefetched = { project, bundle: loadClientBundle(project) }
}

export function takeClientBundle(project: string): Promise<ClientBundle> {
  const bundle =
    prefetched?.project === project
      ? prefetched.bundle
      : loadClientBundle(project)

  prefetched = undefined

  return bundle
}
