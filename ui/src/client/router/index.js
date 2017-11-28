import Vue from 'vue';
import Router from 'vue-router'

import Home from '../components/Home.vue'
import PasswordChange from '../components/PasswordChange.vue'
import PasswordReset from '../components/PasswordReset.vue'
import PasswordResetRequest from '../components/PasswordResetRequest.vue'
import NotFound from '../components/404.vue'

Vue.use(Router)

export default new Router({
  routes: [
    { path: '/', name: 'Home', component: Home },
    { path: '/change', name: 'Change', component: PasswordChange },
    { path: '/reset', name: 'ResetRequest', component: PasswordResetRequest },
    { path: '/reset/:id', name: 'Reset', component: PasswordReset },
    { path: '', name: 'NotFound', component: NotFound }
  ],
  linkActiveClass: "active"
})