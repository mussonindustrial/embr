import type { Plugin } from 'vite'
import type { Typings } from '../../src/language'
import { collectTypings, TypingsManifest } from './collectTypings'

const VIRTUAL_MODULE = 'virtual:periscope-typings'
const RESOLVED_VIRTUAL_MODULE = '\0' + VIRTUAL_MODULE

export function typingsPlugin(
  manifest: TypingsManifest,
  fileName: string
): Plugin {
  let root = process.cwd()
  let typings: Typings | undefined
  const collect = () => (typings ??= collectTypings(manifest, root))

  return {
    name: 'embr-periscope-typings',
    configResolved(config) {
      root = config.root
    },
    resolveId(id) {
      return id === VIRTUAL_MODULE ? RESOLVED_VIRTUAL_MODULE : null
    },
    load(id) {
      if (id !== RESOLVED_VIRTUAL_MODULE) return null
      return `export default ${JSON.stringify(collect())}`
    },
    generateBundle() {
      this.emitFile({
        type: 'asset',
        fileName,
        source: JSON.stringify(collect()),
      })
    },
  }
}
