import { defineConfig } from "vite";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import vue from "@vitejs/plugin-vue";
import legacy from "@vitejs/plugin-legacy";
import { existsSync, readFileSync } from "fs";
import { resolve } from "path";

const keyPath = resolve(process.cwd(), "ssl/key.pem");
const certPath = resolve(process.cwd(), "ssl/cert.pem");
const hasLocalHttpsCert = existsSync(keyPath) && existsSync(certPath);

// https://vite.dev/config/
export default defineConfig({
  build: {
    sourcemap: true,
  },
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
    legacy({
      targets: ["Chrome >= 71", "Android >= 9"],
    }),
  ],
  server: {
    host:
      process.env.VITE_HOST ??
      (hasLocalHttpsCert ? "local.wzpmc.cn" : "0.0.0.0"),
    port: 5174,
    proxy: {
      "/api": {
        target: "http://127.0.0.1:10883",
        changeOrigin: true,
        secure: false,
        ssl: false,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
    https: hasLocalHttpsCert
      ? {
          key: readFileSync(keyPath),
          cert: readFileSync(certPath),
        }
      : undefined,
  },
});
