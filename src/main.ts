import {createApp} from 'vue'
import {createPinia} from 'pinia'
import ui from '@nuxt/ui/vue-plugin'
import App from '@/App.vue'
import '@/style/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(ui)

app.mount('#app')
