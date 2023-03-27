import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import {ElementPlusResolver} from "unplugin-vue-components/resolvers";
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import IconResolver from 'unplugin-icons/dist/resolver'
import Icons from 'unplugin-icons/dist/vite'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
          resolvers: [
            ElementPlusResolver(),
            IconResolver({
                prefix: "Icon"
            })
          ],
        }),
        Components({
          resolvers: [
            IconResolver({
                enabledCollections: ['ep']
            }),
            ElementPlusResolver()
          ],
        }),
        Icons({
            autoInstall: true
        })
    ],
    server: {
        host: "0.0.0.0",
        port: 5173
    }
})
