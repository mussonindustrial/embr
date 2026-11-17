import { type ModuleUrls, moduleUrls } from './moduleUrls'

export type ClientResourceDefinition = {
  path: string
  hash: string
  type: string
  imports?: string[]
}

export type ResourceManifest = {
  hash: string
  resources: ClientResourceDefinition[]
}

export type ClientBundle = { manifest: ResourceManifest; urls: ModuleUrls }

type BundleResponse = {
  hash: string
  manifest: ResourceManifest
  modules: Record<string, string>
}

type BundleRequest = {
  abort: AbortController
  response: Promise<BundleResponse | null>
}

const CLIENT_RESOURCE_BASE = '/data/embr-periscope/client-resource'
const BUNDLE_BASE = '/data/embr-periscope/client-bundle'
const LAST_HASH = 'embr-periscope.hash.'
const CLIENT_PAGE = /^\/data\/perspective\/client\/([^/]+)/

const EMPTY: ClientBundle = {
  manifest: { hash: '', resources: [] },
  urls: new Map(),
}

let prefetched: { project: string; bundle: Promise<ClientBundle> } | undefined

export function prefetch() {
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

export function clientResourceUrl(project: string, hash: string, path: string) {
  return `${CLIENT_RESOURCE_BASE}/${project}/${hash}/${encodeURI(path)}`
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
      return EMPTY
    }

    const cachedBundle =
      hash === lastHash ? await cachedRequest?.response : null
    const bundle = cachedBundle ?? (await requestBundle(project, hash).response)

    if (bundle == null) {
      rememberHash(project, null)
      throw new Error(`Could not load client bundle '${hash}'`)
    }

    const urls = moduleUrls({
      ...bundle,
      urlOf: (path) => clientResourceUrl(project, hash, path),
    })

    return { manifest: bundle.manifest, urls }
  } catch (error) {
    cachedRequest?.abort.abort()
    console.error('Error loading client resources', error)

    return EMPTY
  }
}

async function requestHash(project: string): Promise<string | null> {
  const url = `${CLIENT_RESOURCE_BASE}/${project}/hash.json`
  const response = await fetch(url, { cache: 'no-store' })

  if (!response.ok) {
    throw new Error(`Error fetching resource hash: ${response.statusText}`)
  }

  const { data } = (await response.json()) as { data: { hash?: string } }

  return data.hash ?? null
}

function requestBundle(project: string, hash: string): BundleRequest {
  const abort = new AbortController()
  const url = `${BUNDLE_BASE}/${project}/${encodeURIComponent(hash)}`

  return { abort, response: fetchBundle(url, hash, abort.signal) }
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

function readLastHash(project: string): string | null {
  try {
    return localStorage.getItem(LAST_HASH + project)
  } catch {
    return null
  }
}

function rememberHash(project: string, hash: string | null) {
  try {
    if (hash == null) {
      localStorage.removeItem(LAST_HASH + project)
    } else {
      localStorage.setItem(LAST_HASH + project, hash)
    }
  } catch {
    return
  }
}
