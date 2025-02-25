import VueRouter from "vue-router"

import login from '../components/loginModule/Login'
import psgHome from '../components/home/psgHome'

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
            path: '/passengerHome',
            name: 'passengerHome',
            component: psgHome
        },
    ]
})