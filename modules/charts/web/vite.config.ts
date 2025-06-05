import { defineConfig } from 'vite'
import { resolve } from 'path'

const packageName = 'embr-charts'

export default defineConfig(({ mode }) => ({
  build: {
    outDir: './dist',
    lib: {
      entry: resolve(__dirname, 'src/client.ts'),
      fileName: (_, entryName) => {
        return `${packageName}-${entryName}.js`
      },
      name: 'EmbrCharts',
      formats: ['umd'],
    },
    rollupOptions: {
      external: [
        'react',
        'react-dom',
        '@inductiveautomation/perspective-client',
        'moment',
      ],
      output: {
        globals: {
          react: 'React',
          'react-dom': 'ReactDOM',
          moment: 'moment',
          '@inductiveautomation/perspective-client': 'PerspectiveClient',
        },
      },
    },
  },
  define: {
    'process.env': {},
  },
  test: {
    passWithNoTests: true,
    fileParallelism: mode !== 'benchmark',
    globals: true,
    environment: 'node',
    include: [
      'src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
      '__tests__/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
    ],
    reporters: ['default'],
    coverage: {
      reportsDirectory: '../coverage/js/chart-js',
      provider: 'v8',
    },
  },
}))
