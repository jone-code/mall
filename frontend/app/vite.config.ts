import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const themeScss = path.resolve(rootDir, "src/styles/theme.scss").replace(/\\/g, "/");

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      "@": path.resolve(rootDir, "src"),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "${themeScss}" as *;\n`,
        silenceDeprecations: ["legacy-js-api", "import"],
      },
    },
  },
  server: {
    port: 5174,
    proxy: {
      "/api": {
        target: "http://localhost:8001",
        changeOrigin: true,
      },
      "/files": {
        target: "http://localhost:8001",
        changeOrigin: true,
      },
    },
  },
});
