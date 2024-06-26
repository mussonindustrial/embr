import { defineConfig, Options } from 'tsup'

export default defineConfig((options: Options) => ({
    entry: ['src/index.ts'],
    format: ['esm', 'cjs'],
    moduleResolution: 'node',
    clean: true,
    minify: true,
    ...options,
}))
