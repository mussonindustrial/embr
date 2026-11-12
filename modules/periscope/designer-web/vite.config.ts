import { defineConfig } from 'vite'

import { resolve } from 'path'
import { GLOBALS } from './src/typescript/language/globals'
import { TypingsManifest } from './vite/typings/collectTypings'
import { typingsPlugin } from './vite/typings/typingsPlugin'

const packageName = 'embr-periscope-designer-web'

const typings: TypingsManifest = {
  libs: ['es2020', 'dom', 'dom.iterable'],
  packages: [
    { name: '@types/react' },
    { name: '@types/react-dom' },
    { name: '@types/prop-types' },
    { name: 'csstype', docs: false },
    { name: '@inductiveautomation/perspective-client' },
    { name: '@inductiveautomation/perspective-common' },
    { name: 'mobx' },
    { name: 'mobx-react-lite' },
  ],
  globals: GLOBALS,
  systemModules: {
    perspective: '@embr-js/perspective-client/api',
    periscope: '@embr-modules/periscope-web/api',
  },
}

export default defineConfig(({ mode }) => ({
  plugins: [typingsPlugin(typings, `${packageName}-typings.json`)],
  build: {
    outDir: './dist',
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      fileName: () => {
        return `${packageName}.js`
      },
      cssFileName: `${packageName}`,
      name: 'EmbrPeriscopeDesigner',
      formats: ['umd'],
    },
  },
  test: {
    fileParallelism: mode !== 'benchmark',
    globals: true,
    environment: 'node',
    include: ['test/**/*.test.ts'],
    reporters: ['default'],
    coverage: {
      provider: 'v8',
    },
  },
  define: {
    'process.env.NODE_ENV': JSON.stringify(mode),
  },
}))
