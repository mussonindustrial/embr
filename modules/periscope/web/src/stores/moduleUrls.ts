import type { ResourceManifest } from './clientBundle'

export type ModuleUrls = Map<string, string>

export type BundledModule = {
  source: string
  imports: [start: number, end: number][]
}

type BundledModules = {
  manifest: ResourceManifest
  modules: Record<string, BundledModule>
  urlOf: (path: string) => string
}

type Dependencies = (path: string) => string[]

function dependenciesOf(manifest: ResourceManifest): Dependencies {
  const resourcesByPath = new Map(
    manifest.resources.map((resource) => [resource.path, resource])
  )

  return (path: string) => {
    const imports = resourcesByPath.get(path)?.imports ?? []
    return imports.filter((specifier) => resourcesByPath.has(specifier))
  }
}

function importOrder(manifest: ResourceManifest, dependencies: Dependencies) {
  const cyclic = new Set<string>()
  const visitState = new Map<string, 'open' | 'done'>()
  const order: string[] = []

  const visit = (path: string) => {
    const state = visitState.get(path)

    if (state === 'open') {
      cyclic.add(path)
    }

    if (state != null) {
      return
    }

    visitState.set(path, 'open')

    dependencies(path).forEach(visit)

    visitState.set(path, 'done')
    order.push(path)
  }

  manifest.resources.forEach((resource) => visit(resource.path))

  return { order, cyclic }
}

function addDependencies(paths: Set<string>, dependencies: Dependencies) {
  const addDependenciesOf = (path: string) => {
    dependencies(path).forEach((dependency) => {
      if (!paths.has(dependency)) {
        paths.add(dependency)
        addDependenciesOf(dependency)
      }
    })
  }

  ;[...paths].forEach(addDependenciesOf)
}

export function replaceImports(
  source: string,
  imports: BundledModule['imports'],
  resolve: (specifier: string) => string
): string {
  const parts: string[] = []
  let position = 0

  imports.forEach(([start, end]) => {
    parts.push(source.slice(position, start), resolve(source.slice(start, end)))
    position = end
  })
  parts.push(source.slice(position))

  return parts.join('')
}

function blobUrl(
  { modules, urlOf }: BundledModules,
  path: string,
  dependencies: Dependencies,
  urls: ModuleUrls
): string {
  const origin = window.location.origin
  const { source, imports } = modules[path]
  const bundledUrls = new Map<string, string>()

  dependencies(path).forEach((dependency) => {
    const dependencyUrl = urlOf(dependency)
    const bundledUrl = urls.get(dependency) ?? origin + dependencyUrl

    bundledUrls.set(dependencyUrl, bundledUrl)
    bundledUrls.set(decodeURI(dependencyUrl), bundledUrl)
  })

  const resolve = (specifier: string) => {
    const bundledUrl = bundledUrls.get(specifier)

    if (bundledUrl != null) {
      return bundledUrl
    }

    return specifier.startsWith('/') ? origin + specifier : specifier
  }

  const blob = new Blob(
    [
      `${replaceImports(source, imports, resolve)}\n//# sourceURL=${origin}${urlOf(path)}\n`,
    ],
    { type: 'text/javascript' }
  )

  return URL.createObjectURL(blob)
}

export function moduleUrls(bundled: BundledModules): ModuleUrls {
  const dependencies = dependenciesOf(bundled.manifest)
  const { order, cyclic } = importOrder(bundled.manifest, dependencies)

  const unbundled = new Set(cyclic)

  order.forEach((path) => {
    if (bundled.modules[path] == null) {
      unbundled.add(path)
    }
  })

  addDependencies(unbundled, dependencies)

  const urls: ModuleUrls = new Map()

  order.forEach((path) => {
    if (!unbundled.has(path)) {
      urls.set(path, blobUrl(bundled, path, dependencies, urls))
    }
  })

  return urls
}
