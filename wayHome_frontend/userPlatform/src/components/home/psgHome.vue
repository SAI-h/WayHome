<template>
    <el-container style="height: 608px; border: 1px solid #eee;">
        <el-aside width="16%" style="background-color: whitesmoke">
            <el-menu  
                default-active="1-1"
                :default-openeds="['1', '2', '3']"
                background-color="white"
                text-color="grey"
                active-text-color="darkorange" 
                @select="handleSelect">
                <el-submenu index="1">
                    <template slot="title"><h4>公交查询</h4></template>
                    <el-menu-item-group>
                        <el-menu-item index="1-1">班次查询</el-menu-item>
                        <el-menu-item index="1-2">站点查询</el-menu-item>
                        <el-menu-item index="1-3">乘车规划</el-menu-item>
                    </el-menu-item-group>
                </el-submenu>
            </el-menu>
        </el-aside>
        
        <el-container>
            <el-header style="text-align: center; font-size: 12px; background-color: antiquewhite;">
                <img style="height: 60px; margin-left: 100px" src="../../assets/busSystem.png"/>
                <el-button style="float: right;" type="warning" icon="el-icon-thumb" @click="turnOff" size="small">抵达终点站</el-button>
            </el-header>
            
            <el-main style="padding: 20px">
                <MapContainer v-if="selection === '1-1' || selection === '1-2' || selection === '1-3'"></MapContainer>
                <scheduleSearch class="leftModule" v-if="selection === '1-1'"></scheduleSearch>
                <stationFindRoute class="leftModule" v-else-if="selection === '1-2'"></stationFindRoute>
                <calPath class="leftModule" v-else-if="selection === '1-3'"></calPath>
                <personal v-else-if="selection === '2-1'"></personal>
                <busCard v-else-if="selection === '2-2'"></busCard>
                <NoticePreview v-else-if="selection === '3-1'"></NoticePreview>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
import MapContainer from '../../MapContainer/MapContainer.vue'
import calPath from '../mainFunctions/calPath.vue'
import scheduleSearch from '../mainFunctions/scheduleSearch.vue'
import stationFindRoute from '../mainFunctions/stationFindRoute.vue'

import { MessageBox } from 'element-ui'
import axios from 'axios'
const ERROR = 404;
const SUCCESS = 200;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name: 'psgHome',
    data() {
        return {
            selection: '',
        }
    },
    components:{
        MapContainer,
        calPath,
        scheduleSearch,
        stationFindRoute,
        // personal,
        // busCard,
        // NoticeLoad
        // NoticePreview
    },
    mounted() {
        this.setStorage(); // 设置临时会话存储
    },
    methods:{
        handleSelect(index) { // 处理选择到的任务项
            this.selection = index;
        },
        setStorage() {
            let cityData = this.$route.params.city;
            let citySave = {
                cityID: cityData.cityID,
                lngLat: [cityData.cityLng, cityData.cityLat],
                name: cityData.cityName
            }
            sessionStorage.setItem('city', JSON.stringify(citySave));
            this.selection = '1-1';
        },
        turnOff() {
            MessageBox.confirm("乘客您好，已抵达终点站，请带好您的随身物品准备下车！祝您之后的旅途像今日这班车一样平安顺遂！", "退出系统", 
                {
                    confirmButtonText: '起身下车🙋‍♀️',
                    cancelButtonText: '坐过站了，再坐回去😭'
                }
            )
            .then(
                () => {
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

    .leftModule {
        padding: 25px;
        width: 46%;
        height: 90%;
        float: left;
        overflow-y:auto;
        overflow-x:auto; 
    }
</style>