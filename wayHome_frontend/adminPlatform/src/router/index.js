import VueRouter from "vue-router"

import login from "../components/login"
import home from "../components/home"

export default new VueRouter ({
    mode: 'history',
    routes: [
        {
            path: '/',
            redirect: '/login' // 重定向
        },
        {
            path: '/login',
            name: 'login',
            component: login
        },
        {
            path: '/home',
            name: 'home',
            component: home
        }
    ]
})