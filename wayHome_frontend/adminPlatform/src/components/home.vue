<template>
    <el-container style="height: 608px; border: 1px solid #eee;">
        <el-aside width="200px" style="background-color: #545c64">
            <el-menu  
                default-active="1-1"
                :default-openeds="['1', '2', '3']"
                background-color="#545c64"
                text-color="wheat"
                active-text-color="darkorange" 
                @select="handleSelect">
                <el-submenu index="1">
                    <template slot="title"><h4>公交规划</h4></template>
                    <el-menu-item-group title="站点/线路">
                        <el-menu-item index="1-1">站点设置</el-menu-item>
                        <el-menu-item index="1-2">线路设置</el-menu-item>
                        <el-menu-item index="1-3">班次规划</el-menu-item>
                    </el-menu-item-group>
                </el-submenu>

            </el-menu>
        </el-aside>
        
        <el-container>
            <el-header style="text-align: center; font-size: 12px; background-color: antiquewhite;">
                <img style="height: 60px; margin-left: 100px;" src="../assets/busSystem.png"/>
                <el-button style="float: right;" type="warning" icon="el-icon-thumb" @click="turnOff" size="small">抵达终点站</el-button>
            </el-header>
            
            <el-main style="padding: 20px">
                <!-- 向子组件传值 -->
                <MapContainer v-if="selection === '1-1' || selection === '1-2'" :city="city"/>
                <stations v-if="selection === '1-1'"/>
                <bus-routes v-else-if="selection === '1-2'"/>
                <Schedule v-else-if="selection === '1-3'"></Schedule>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
import { MessageBox } from 'element-ui'
import axios from 'axios'
import MapContainer from './MapContainer.vue'
import Stations from './stations.vue'
import BusRoutes from './busRoutes.vue'
import Schedule from './schedule.vue'
const ERROR = 404;
const SUCCESS = 200;
const EXPIRE = 702;
// const BASE = "http://REPLACE_ME_WITH_YOUR_SERVER_HOST:3000";
const BASE = "http://localhost:3000";

// const jwt = localStorage.getItem('jwt');
// axios.defaults.headers.common['Authorization'] = jwt;

export default {
    name: 'home',
    data() {
        return {
            selection: '',
            city: {
                id: '',
                name: '',
                lngLat:[]
            }
        }
    },
    components:{
        MapContainer,
        Stations,
        BusRoutes,
        Schedule,
    },
    mounted() {
        this.initCity();
    },
    methods:{
        initCity() { 
            if(this.$route.params === undefined && JSON.parse(sessionStorage.getItem('admin')) === null) {
                console.log(this.$route.params);
                MessageBox.alert("抱歉，请先进行登录！", "提示信息");
                this.$router.replace('/login'); 
            }
            else {
                let jwt = this.$route.params.jwt;
                const base64Url = jwt.split('.')[1]; 
                const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                let body = JSON.parse(jsonPayload);

                if(sessionStorage.getItem('admin') === null) {
                    sessionStorage.setItem('admin', JSON.stringify({
                        adminID: body.adminID,
                    }));
                }

                if(sessionStorage.getItem('city') === null) {
                    this.city.id = body.cityID;
                    this.city.name = body.cityName;
                    this.city.lngLat = [body.cityLng, body.cityLat];
                    if(this.city.name !== '') {
                        sessionStorage.setItem('city', JSON.stringify(this.city));
                        this.selection = '1-1';
                    }
                }
            }
        },
        handleSelect(index) { // 处理选择到的任务项
            this.selection = index;
        },
        turnOff() {
            MessageBox.confirm("拜拜ヾ(•ω•`)o！！", "退出系统", 
                {
                    confirmButtonText: '再见ヾ(￣▽￣)',
                    cancelButtonText: '好奇点到了，我再瞅瞅🤦‍♂️'
                }
            )
            .then(
                () => {
                    localStorage.clear();
                    sessionStorage.clear();
                    this.$router.replace('/login');
                }
            )
        }
    }
}
</script>

<style>
    .el-header {
        background-color: #B3C0D1;
        color: #333;
        line-height: 60px;
    }
</style>