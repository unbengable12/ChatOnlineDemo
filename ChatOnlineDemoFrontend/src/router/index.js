import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "@/net";
import {ElMessage} from "element-plus";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'welcome',
      component: () => import("@/views/WelcomeView.vue"),
      children: [
        {
          path: '',
          name: "welcome-login",
          component: () => import("@/views/welcome/LoginPage.vue")
        },
        {
          path: 'register',
          name: "welcome-register",
          component: () => import("@/views/welcome/RegisterPage.vue")
        },
        {
          path: 'reset',
          name: 'welcome-reset',
          component: () => import("@/views/welcome/ResetPage.vue")
        }
      ]
    },
    {
      path: '/index',
      name: 'index',
      component: () => import("@/views/IndexView.vue"),
      children: [
        {
          path: '',
          name: "index-home",
          component: () => import("@/views/index/HomePage.vue")
        },
        {
          path: '/about',
          name: 'index-about',
          component: () => import("@/views/index/AboutPage.vue")
        },
        {
          path: '/not-found',
          name: 'index-not-found',
          component: () => import("@/views/result/NotFoundPage.vue")
        },
        {
          path: "/error",
          name: "index-error",
          component: () => import("@/views/result/ErrorPage.vue")
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next)=>{
  const isUnauthorized = unauthorized();
  const routeExists = router.getRoutes().some(route => route.path === to.path);
  if (!routeExists) {
    next("/not-found")
    return;
  }
  if (to.name.startsWith("welcome-") && !isUnauthorized) {
    next("/index")
  } else if (to.name.startsWith("index-") && isUnauthorized) {
    next("/")
  } else {
    next()
  }
})
export default router;