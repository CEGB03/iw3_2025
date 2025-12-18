import { createRouter, createWebHistory } from 'vue-router'
import Login from '../pages/Login.vue'
import Orders from '../pages/Orders.vue'
import OrderDetail from '../pages/OrderDetail.vue'

const routes = [
  { path: '/', redirect: '/orders' },
  { path: '/login', component: Login },
  { path: '/orders', component: Orders, meta: { requiresAuth: true } },
  { path: '/orders/:id', component: OrderDetail, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
