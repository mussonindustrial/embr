import { createRequire } from 'node:module'
import { dirname, join } from 'node:path'
import ts from 'typescript'
import type { Plugin } from 'vite'

const PERSPECTIVE_CLIENT = '@inductiveautomation/perspective-client'
const LIBRARY_GLOBALS = {
  mobx: 'mobx',
  'mobx-react-lite': 'mobxReactLite',
  react: 'React',
  'react-dom': 'ReactDOM',
}
const IDENTIFIER = /^[A-Za-z_$][\w$]*$/

const require = createRequire(import.meta.url)
const sdkFile = require.resolve(`${PERSPECTIVE_CLIENT}/package.json`)
const sdk = require(sdkFile)

function named(exports: string[]) {
  return exports
    .filter((name) => name !== 'default' && IDENTIFIER.test(name))
    .sort()
}

function satisfies(version: string, range: string) {
  if (!range.startsWith('^')) return version === range
  const [major, minor, patch] = version.split('.').map(Number)
  const [min, minMinor, minPatch] = range.slice(1).split('.').map(Number)
  return (
    major === min &&
    (minor > minMinor || (minor === minMinor && patch >= minPatch))
  )
}

function library([specifier, global]: [string, string]) {
  const { version } = require(`${specifier}/package.json`)
  const range = sdk.dependencies[specifier]
  if (!satisfies(version, range)) {
    throw new Error(
      `${specifier} ${version} is installed, but Perspective ${sdk.version} needs ${range}.`
    )
  }
  const exports = named(Object.keys(require(specifier)))
  return { specifier, global, version, exports }
}

function perspectiveClient() {
  const typings = join(dirname(sdkFile), sdk.types)
  const program = ts.createProgram([typings], { skipLibCheck: true })
  const checker = program.getTypeChecker()
  const module = checker.getSymbolAtLocation(program.getSourceFile(typings)!)!
  const isValue = (symbol: ts.Symbol): boolean =>
    symbol.flags & ts.SymbolFlags.Alias
      ? isValue(checker.getAliasedSymbol(symbol))
      : (symbol.flags & ts.SymbolFlags.Value) !== 0
  const exports = named(
    checker
      .getExportsOfModule(module)
      .filter(isValue)
      .map((symbol) => symbol.name)
  )
  return {
    specifier: PERSPECTIVE_CLIENT,
    global: 'PerspectiveClient',
    version: sdk.version as string,
    exports,
  }
}

export default function perspectiveGlobals(): Plugin {
  return {
    name: 'perspective-globals',
    generateBundle() {
      const globalModules = [
        perspectiveClient(),
        ...Object.entries(LIBRARY_GLOBALS).map(library),
      ]
      this.emitFile({
        type: 'asset',
        fileName: 'perspective-globals.json',
        source: JSON.stringify({ globalModules }, null, 2) + '\n',
      })
    },
  }
}
