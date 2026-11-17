import type { ResourceManifest } from './clientBundle'

export type ModuleUrls = Map<string, string>

type BundledModules = {
  manifest: ResourceManifest
  modules: Record<string, string>
  urlOf: (path: string) => string
}

type Dependencies = (path: string) => string[]

const RELATIVE_IMPORT = /\b(?:from|import)\s*\(?\s*['"]\.\.?\//
const ABSOLUTE_IMPORT = /(\b(?:from|import)\s*\(?\s*)(['"])\//g

export function moduleUrls(bundled: BundledModules): ModuleUrls {
  const dependencies = dependenciesOf(bundled.manifest)
  const { order, cyclic } = importOrder(bundled.manifest, dependencies)

  const unbundled = new Set(cyclic)

  for (const path of order) {
    if (isUnbundleable(bundled.modules[path])) {
      unbundled.add(path)
    }
  }

  addDependencies(unbundled, dependencies)

  const urls: ModuleUrls = new Map()

  for (const path of order) {
    if (!unbundled.has(path)) {
      urls.set(path, blobUrl(bundled, path, dependencies, urls))
    }
  }

  return urls
}

function isUnbundleable(source: string | undefined): boolean {
  return source == null || RELATIVE_IMPORT.test(source)
}

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

    for (const dependency of dependencies(path)) {
      visit(dependency)
    }

    visitState.set(path, 'done')
    order.push(path)
  }

  for (const resource of manifest.resources) {
    visit(resource.path)
  }

  return { order, cyclic }
}

function addDependencies(paths: Set<string>, dependencies: Dependencies) {
  const addDependenciesOf = (path: string) => {
    for (const dependency of dependencies(path)) {
      if (!paths.has(dependency)) {
        paths.add(dependency)
        addDependenciesOf(dependency)
      }
    }
  }

  for (const path of [...paths]) {
    addDependenciesOf(path)
  }
}

function blobUrl(
  { modules, urlOf }: BundledModules,
  path: string,
  dependencies: Dependencies,
  urls: ModuleUrls
): string {
  const origin = window.location.origin
  let source = modules[path]

  for (const dependency of dependencies(path)) {
    const dependencyUrl = urlOf(dependency)
    const resolvedUrl = urls.get(dependency) ?? dependencyUrl

    source = replaceSpecifier(source, dependencyUrl, resolvedUrl)
  }

  source = source.replace(ABSOLUTE_IMPORT, `$1$2${origin}/`)

  const blob = new Blob(
    [`${source}\n//# sourceURL=${origin}${urlOf(path)}\n`],
    {
      type: 'text/javascript',
    }
  )

  return URL.createObjectURL(blob)
}

function replaceSpecifier(source: string, from: string, to: string): string {
  return source
    .replaceAll(`'${from}'`, `'${to}'`)
    .replaceAll(`"${from}"`, `"${to}"`)
}
