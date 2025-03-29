import { defineConfig } from 'vite'
import { resolve } from 'path'

const packageName = 'embr-perspective-client'

export default defineConfig(({ mode }) => ({
  build: {
    outDir: './dist',
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      fileName: (_, entryName) => {
        return `${packageName}-${entryName}.js`
      },
      name: packageName,
      formats: ['umd'],
    },
    rollupOptions: {
      external: [
        '@inductiveautomation/perspective-client',
        '@inductiveautomation/perspective-components',
        '@inductiveautomation/perspective-designer',
        'react',
        'react-dom',
        'moment',
      ],
      output: {
        globals: {
          '@inductiveautomation/perspective-client': 'PerspectiveClient',
          '@inductiveautomation/perspective-components':
            'PerspectiveComponents',
          '@inductiveautomation/perspective-designer': 'PerspectiveDesigner',
          react: 'React',
          'react-dom': 'ReactDOM',
          moment: 'moment',
        },
      },
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
      reportsDirectory: '../../../coverage/lib/js/embr-tag-stream',
      provider: 'v8',
    },
  },
}))
