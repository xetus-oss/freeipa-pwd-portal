import Vue from 'vue'

import axios from 'axios';
import VueAxios from 'vue-axios';
import VueRecaptcha from 'vue-recaptcha';

import { Modal } from 'bootstrap-vue/es/components';

import router from './router'
import App from './App.vue'
import NewPasswordInput from './components/NewPasswordInput.vue'
import PwdPortalForm from './components/PwdPortalForm.vue';
import './styles/styles.scss'

const appConfig = {
  recaptchaPublicKey: UiConfig.RECAPTCHA_PUBLIC_KEY,
  recaptchaEnabled: UiConfig.RECAPTCHA_ENABLED,
  apiBase: UiConfig.API_BASE
}

Vue.use({ install: (Vue) => Vue.prototype.$appConfig = appConfig });
axios.defaults.baseURL = appConfig.apiBase
axios.defaults.withCredentials = true;

Vue.use(VueAxios, axios);
Vue.use(Modal);
Vue.component('new-password-input', NewPasswordInput)
Vue.component('pwd-portal-form', PwdPortalForm);
Vue.component('vue-recaptcha', VueRecaptcha);
if (process.env.NODE_ENV == 'production') {
  Vue.config.productionTip = true
} else {
  Vue.config.devtools = true;
}

/* eslint-disable no-new */
const app = new Vue({
  el: '#app',
  router,
  template: '<App/>',
  components: {
    App,
  }
})
