import { defineConfig } from "vite";
import { resolve } from "path";

export default defineConfig({
  build: {

    outDir: "./dist",
    lib: {
      entry: resolve(__dirname, 'src/client.ts'),
      fileName: "example-perspective-component",
      name: "ExamplePerspectiveComponent"
    },
    rollupOptions: {
      external: ['react', 'react-dom', '@inductiveautomation/perspective-client'],
      output: {
        globals: {
          react: 'React',
          '@inductiveautomation/perspective-client': 'PerspectiveClient'
        }
      }
    }
  }
})