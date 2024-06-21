import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig(({ mode }) => ({
    build: {
        outDir: './dist',
        lib: {
            entry: resolve(__dirname, 'src/index.ts'),
            name: 'EmbrTagStream',
            formats: ['umd', 'es'],
        },
    },
    test: {
        fileParallelism: mode !== 'benchmark',
    },
}))
