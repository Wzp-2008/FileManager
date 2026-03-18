import {defineConfig} from "vite";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import {ElementPlusResolver} from "unplugin-vue-components/resolvers";
import vue from "@vitejs/plugin-vue";
import legacy from "@vitejs/plugin-legacy";
import {readFileSync} from "fs";

// https://vite.dev/config/
export default defineConfig({
    build: {
        sourcemap: true,
    },
    plugins: [
        vue(),
        AutoImport({resolvers: [ElementPlusResolver()]}),
        Components({resolvers: [ElementPlusResolver()]}),
        legacy({
            targets: ["Chrome >= 71", "Android >= 9"],
        }),
    ],
    server: {
        host: "local.wzpmc.cn",
        proxy: {
            "/api": {
                target: "http://127.0.0.1:8080/",
                changeOrigin: true,
                secure: false,
                ssl: false,
                // rewrite: (path) => path.replace(/^\/api/, '')
            },
        },
        https: {
            key: readFileSync("ssl/key.pem"),
            cert: readFileSync("ssl/cert.pem"),
        },
    },
});
