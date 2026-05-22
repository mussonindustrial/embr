import { defineConfig } from 'vite'

import { resolve } from 'path'

const packageName = 'embr-periscope-designer-web'

export default defineConfig(({ mode }) => ({
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
    include: [
      'src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
      '__tests__/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
    ],
    reporters: ['default'],
    coverage: {
      provider: 'v8',
    },
  },
  define: {
    'process.env.NODE_ENV': JSON.stringify(mode),
  },
}))
