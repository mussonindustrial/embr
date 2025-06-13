import { defineConfig } from 'vite'
import { resolve } from 'path'
import { viteSingleFile } from 'vite-plugin-singlefile'

export default defineConfig(({ mode }) => ({
  build: {
    rollupOptions: {
      input: {
        editor: resolve(__dirname, 'editor.html'),
      },
    },
    external: [],
    target: 'esnext',
  },
  plugins: [viteSingleFile()],
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
  define: {
    'process.env.NODE_ENV': JSON.stringify(mode),
  },
}))
