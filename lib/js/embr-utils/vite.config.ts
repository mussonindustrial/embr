import { defineConfig } from 'vite'
import { resolve } from 'path'

const packageName = 'embr-utils'

export default defineConfig({
    build: {
        outDir: './dist',
        lib: {
            entry: resolve(__dirname, 'src/index.ts'),
            fileName: (_, entryName) => {
                return `${packageName}-${entryName}.js`
            },
            name: 'embr-utils',
            formats: ['umd'],
        },
    },
})
