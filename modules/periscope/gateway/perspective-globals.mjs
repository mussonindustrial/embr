import { readdirSync, readFileSync, writeFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import { dirname, join } from 'node:path'
import vm from 'node:vm'
import ts from 'typescript'

const GLOBAL_MODULES = ['mobx', 'mobx-react-lite', 'react', 'react-dom']
const PERSPECTIVE_CLIENT = '@inductiveautomation/perspective-client'

const LIBRARY_FILE = /^(.+)-(\d+\.\d+\.\d+)(\.min)?\.js$/
const IDENTIFIER = /^[A-Za-z_$][\w$]*$/

const [scriptsDirectory, outputFile] = process.argv.slice(2)

const page = vm.createContext({})
vm.runInContext('var self = globalThis', page)

function read(file) {
  const [, specifier, version] = LIBRARY_FILE.exec(file)
  const script = readFileSync(join(scriptsDirectory, file), 'utf8')
  const umdHeader = script.slice(0, Math.max(0, script.indexOf('define.amd')))
  const requires = [...umdHeader.matchAll(/require\("([^"]+)"\)/g)]
  return {
    file,
    specifier,
    version,
    script,
    requires: requires.map(([, name]) => name),
  }
}

function load({ file, specifier, version, script }) {
  const before = Object.getOwnPropertyNames(page)
  vm.runInContext(script, page, { filename: file })
  const defined = Object.getOwnPropertyNames(page).filter(
    (name) => !before.includes(name) && !name.startsWith('__')
  )
  if (defined.length !== 1) {
    throw new Error(`${file} should define one global, not: ${defined}`)
  }

  const [global] = defined
  const exports = Object.keys(page[global])
    .filter((name) => name !== 'default' && IDENTIFIER.test(name))
    .sort()
  return { specifier, global, version, exports }
}

const unloaded = readdirSync(scriptsDirectory)
  .filter((file) => LIBRARY_FILE.test(file))
  .sort()
  .map(read)
const loaded = []

while (unloaded.length > 0) {
  const isLoaded = (name) => loaded.some(({ specifier }) => specifier === name)
  const next = unloaded.findIndex(({ requires }) => requires.every(isLoaded))
  if (next === -1) {
    const files = unloaded.map(({ file }) => file)
    throw new Error(`${files} require a library Perspective doesn't ship.`)
  }
  loaded.push(load(...unloaded.splice(next, 1)))
}

function perspectiveClient() {
  const require = createRequire(import.meta.url)
  const packageFile = require.resolve(`${PERSPECTIVE_CLIENT}/package.json`)
  const { version, types } = require(packageFile)
  const typings = join(dirname(packageFile), types)

  const program = ts.createProgram([typings], { skipLibCheck: true })
  const checker = program.getTypeChecker()
  const module = checker.getSymbolAtLocation(program.getSourceFile(typings))
  const isValue = (symbol) =>
    symbol.flags & ts.SymbolFlags.Alias
      ? isValue(checker.getAliasedSymbol(symbol))
      : symbol.flags & ts.SymbolFlags.Value
  const exports = checker
    .getExportsOfModule(module)
    .filter(isValue)
    .map((symbol) => symbol.name)
    .filter((name) => name !== 'default' && IDENTIFIER.test(name))
    .sort()
  return {
    specifier: PERSPECTIVE_CLIENT,
    global: 'PerspectiveClient',
    version,
    exports,
  }
}

const isGlobalModule = ({ specifier }) => GLOBAL_MODULES.includes(specifier)
const bySpecifier = (a, b) => a.specifier.localeCompare(b.specifier)
const libraries = loaded.filter(isGlobalModule)
const otherGlobals = loaded.filter((l) => !isGlobalModule(l)).sort(bySpecifier)

if (libraries.length !== GLOBAL_MODULES.length) {
  throw new Error(`Perspective doesn't ship all of: ${GLOBAL_MODULES}`)
}

const globalModules = [perspectiveClient(), ...libraries].sort(bySpecifier)

const perspectiveGlobals = { globalModules, otherGlobals }
writeFileSync(outputFile, JSON.stringify(perspectiveGlobals, null, 2) + '\n')
