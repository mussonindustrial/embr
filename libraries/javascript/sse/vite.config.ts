import { defineConfig } from 'vite'
import { resolve } from 'path'

const packageName = 'sse'

export default defineConfig(({ mode }) => ({
  build: {
    outDir: './dist',
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      name: 'EmbrSSE',
      formats: ['umd', 'es'],
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
      reportsDirectory: '../coverage/js/sse',
      provider: 'v8',
    },
  },
}))
