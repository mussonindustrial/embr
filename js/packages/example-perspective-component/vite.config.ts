import { defineConfig } from "vite";
import { resolve } from "path";

const packageName = "example-perspective-component";

export default defineConfig({
  build: {
    outDir: "./dist",
    lib: {
      entry: resolve(__dirname, "src/client.ts"),
      fileName: (_, entryName) => {
        return `${packageName}-${entryName}.js`;
      },
      name: "ExamplePerspectiveComponent",
      formats: ["umd"],
    },
    rollupOptions: {
      external: [
        "react",
        "react-dom",
        "@inductiveautomation/perspective-client",
      ],
      output: {
        globals: {
          react: "React",
          "react-dom": "ReactDOM",
          "@inductiveautomation/perspective-client": "PerspectiveClient",
        },
      },
    },
  },
});
