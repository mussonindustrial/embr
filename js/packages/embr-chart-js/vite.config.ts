import { defineConfig } from 'vite'
import { resolve } from 'path'

const packageName = 'embr-chart-js'

export default defineConfig({
    build: {
        outDir: './dist',
        lib: {
            entry: resolve(__dirname, 'src/client.ts'),
            fileName: (_, entryName) => {
                return `${packageName}-${entryName}.js`
            },
            name: 'EmbrChartJs',
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
                    '@inductiveautomation/perspective-client':
                        'PerspectiveClient',
                },
            },
        },
    },
})
