<template>
    <div>
        <el-radio-group v-model="searchInfo.choice" size="small" style="margin-left: -70%; margin-bottom: 20px;" fill="orange">
            <el-radio-button label="minTimeCost">最短时间</el-radio-button>
            <el-radio-button label="minTransfer">最少换乘</el-radio-button>
        </el-radio-group>
        <el-form :model="searchInfo" ref="ruleForm">
            <el-form-item label="出发站点">
                <el-select v-model="searchInfo.sStation" filterable placeholder="请选择/输入" @change="selectedItem" value-key="staID">
                    <el-option
                        v-for="item in stations"
                        :key="item.staID"
                        :label="item.staName"
                        :value="item">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="到达站点">
                <el-select v-model="searchInfo.eStation" filterable placeholder="请选择/输入" @change="selectedItem" value-key="staID">
                    <el-option
                        v-for="item in stations"
                        :key="item.staID"
                        :label="item.staName"
                        :value="item">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="出发时间">
                <el-date-picker class="dtPicker"
                v-model="searchInfo.startTime"
                type="datetime"
                placeholder="选择日期时间"
                default-time="12:00:00">
                </el-date-picker>
            </el-form-item>
            <el-button type="warning" @click="swapStation" style="float:right; margin-top: -160px;"
            :disabled="searchInfo.sStation === '' || searchInfo.eStation === ''">交换站点</el-button>
            <!-- <el-button type="warning" @click="onSubmit"
            :disabled="searchInfo.sStation === '' || searchInfo.eStation === ''">交换站点</el-button> -->
        </el-form>
        <span v-if="this.searchInfo.sStation === '' || this.searchInfo.eStation === ''" class="NoSearch">
            请键入您想查询的路线的对应站点、时间！
        </span>
        <span v-else-if="this.searchInfo.sStation === this.searchInfo.eStation" class="NoSearch">
            请保证输入的两个站点不相同！
        </span>
        <span v-else-if="showInfo.length === 0" class="NoSearch">该时段尚无两站点之间的通行数据！</span>
        <el-collapse v-model="activeNames" @change="handleChange" accordion v-else>
            <el-collapse-item  v-for="(plan, index) in showInfo" :key="index" :name="index">
                <template slot="title">
                    <span v-if="searchInfo.choice === 'minTimeCost'" style="color: blue; font-size: 13px;">
                        推荐方案
                        全长{{ Math.round(plan.realDistance / 1000) }}公里
                        (预计{{ Math.round(plan.distance / 60) }}分钟)
                    </span>
                    <span v-else  style="color: blue; font-size: 13px;">
                        推荐方案{{ index + 1 }}
                        全长{{ Math.round(plan.realDistance / 1000) }}公里
                        ({{ plan.distance === 0 ? '直达' : '换乘' + plan.distance + '次' }})
                    </span>
                </template>
                <div v-for="(stations, index) in plan.scheme" :key="index">
                    <h3 style="margin-left: 10px; margin-bottom: 20px; color: blueviolet;">步骤{{ index+1 }}: 乘坐线路{{ stations[0].routeName }}({{ stations[0].staName }}=>{{ stations[stations.length - 1].staName }})</h3>
                    <!-- <el-timeline>
                        <el-timeline-item
                        v-for="(station, index) in stations"
                            :key="index"
                            color="#0bbd87"
                            >
                            {{station.staName}}
                        </el-timeline-item>
                    </el-timeline> -->

                    <el-steps direction="vertical" :active="stations.length" finish-status="finish" space="60px">
                        <el-step 
                            v-for="(station, index) in stations"
                            :key="index"
                            :title="station.staName">
                        </el-step>
                    </el-steps>
                </div>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
import axios from 'axios'
import { MessageBox } from 'element-ui'
const ERROR = 404;
const SUCCESS = 200;
const busSpeed = 6.3; // (m/s)
const infinity = Number.POSITIVE_INFINITY;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name: "calPath",
    data() {
        return {
            stations: [],
            searchInfo: {
                choice: 'minTimeCost',
                sStation: '',
                eStation: '',
                startTime: new Date() // 出发时间
            },
            graphInfo: {},
            lenSet: false,
            showInfo: [],
            activeNames: ''
        }
    },
    mounted() {
        this.getStations();
        // this.getAllBusRoutes();
    },
    methods: {
        handleChange(index){
            if(index !== '') {
                let routeDetail = [];
                let arr = this.showInfo[index].routeDetail;
                let avgLng = 0, avgLat = 0, count = 0;
                for(let part of arr) {
                    console.log(part);
                    for(let point of part) {
                        point.pointLng = point.point.x;
                        point.pointLat = point.point.y;
                        avgLng += point.pointLng;
                        avgLat += point.pointLat;
                        count ++;
                    }
                }
                avgLng /= count;
                avgLat /= count;
                this.$bus.$emit("getCenter", [avgLng, avgLat]);
                for(let i = 0; i < arr.length; i ++) {
                    routeDetail.push(arr[i]);
                }
                this.$bus.$emit("drawPaths", routeDetail);
            }
        },
        getStations() {// 获取所有的站点数据
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            }
            axios.get(`${BASE}/station`, {params: args})
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.stations = res.data.data;
                    }
                    else {
                        MessageBox.alert(`初始化失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            )
            .catch(
                error => {
                    MessageBox.alert(`初始化失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        selectedItem() { // 多选框强制渲染
            this.$forceUpdate();
        },
        swapStation() { // 交换出发站和到达站
            let tmp = this.searchInfo.sStation;
            this.searchInfo.sStation = this.searchInfo.eStation;
            this.searchInfo.eStation = tmp;
        },
        getAxiosMinTimeCost(startStation, endStation, searchTime) { 
            axios.get(`${BASE}/calPath`, {params:{
                startStaID: startStation.staID,
                endStaID: endStation.staID,
                searchTime: searchTime,
                scheme: 'minTimeCost',
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            }}).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.showInfo = res.data.data;
                    }
                    else {
                        MessageBox.alert(`查询失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            ).catch(
                error => {
                    MessageBox.alert(`查询失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        getAxiosMinTransfer(startStation, endStation, searchTime) {
            axios.get(`${BASE}/calPath`, {params:{
                startStaID: startStation.staID,
                endStaID: endStation.staID,
                searchTime: searchTime,
                scheme: 'minTransfer',
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            }}).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.showInfo = res.data.data;
                        console.log(this.showInfo);
                    }
                    else {
                        MessageBox.alert(`查询失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            ).catch(
                error => {
                    MessageBox.alert(`查询失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        getMinTransfer(startStation, endStation, serachTime) { // 寻找最少换乘次数的路径
            let scheduleList = this.findRunBus(serachTime);
            let graph = {};
            let routeNameSet = [];
            for(let i = 0; i < scheduleList.length; i ++) {
                if(routeNameSet.indexOf(scheduleList[i].routeName) === -1) {
                    routeNameSet.push(scheduleList[i].routeName);
                }
            }
            let points = [];
            let pToR = new Map();
            for(let i = 0; i < routeNameSet.length; i ++) {
                let path = this.graphInfo[routeNameSet[i]].path;
                for(let j = 0; j < path.length; j ++) {
                    if(path[j].staID !== null) {
                        points.push({
                            staID: path[j].staID,
                            staName: path[j].staName,
                            routeName: routeNameSet[i]
                        })
                        let obj = {
                            staID: path[j].staID,
                            staName: path[j].staName,
                        };
                        if(!pToR.has(JSON.stringify(obj))) {
                            pToR.set(JSON.stringify(obj), new Set());
                        }
                        pToR.get(JSON.stringify(obj)).add(routeNameSet[i]);
                    }
                }
            }
            let kvMap = new Map();
            for(let i = 0; i < points.length; i ++) {
                kvMap.set(JSON.stringify(points[i]), i);
            }
            for(let i = 0; i < points.length; i ++) {
                graph[i] = [];
            }
            // console.log(pToR);
            // 建图
            for(let i = 0; i < scheduleList.length; i ++) {
                // 方向相同,建立正向边;否则建立反向边
                let path = this.graphInfo[scheduleList[i].routeName].path.slice();
                if(scheduleList[i].schedule.sStation !== path[0].staName) {
                    path = this.reverseArray(path);
                }
                let stationPII = [];
                for(let j = 0, cnt = 0; j < path.length; j ++) { // cnt用于记录站点是否成对
                    if(path[j].staID !== null) { // 是站点
                        stationPII.push(path[j]);
                        if(stationPII.length == 2) {
                            // 建立由first指向second长度为distance的边
                            let first = {
                                staID: stationPII[0].staID,
                                staName: stationPII[0].staName,
                                routeName: scheduleList[i].routeName
                            }
                            let second = {
                                staID: stationPII[1].staID,
                                staName: stationPII[1].staName,
                                routeName: scheduleList[i].routeName
                            }
                            let firstIndex = kvMap.get(JSON.stringify(first));
                            let secondIndex = kvMap.get(JSON.stringify(second));
                            graph[firstIndex].push({
                                ver: secondIndex,
                                cost: 0
                            })
                            // 重置
                            stationPII = [path[j]];
                        }
                    }
                }
            }
            // 换乘建边边权为1
            for(let [key, value] of pToR) {
                let station = JSON.parse(key);
                for(let routeName1 of value) {
                    for(let routeName2 of value) {
                        if(routeName1 === routeName2) continue;
                        let first = {
                            staID: station.staID,
                            staName: station.staName,
                            routeName: routeName1
                        }
                        let second = {
                            staID: station.staID,
                            staName: station.staName,
                            routeName: routeName2
                        }
                        let firstIndex = kvMap.get(JSON.stringify(first));
                        let secondIndex = kvMap.get(JSON.stringify(second));
                        graph[firstIndex].push({
                            ver: secondIndex,
                            cost: 1
                        })
                    }
                }
            }

            let startChoice = []; // 可能作为起点的点集
            let endChoice = []; // 可能作为终点的点集
            for(let j = 0; j < points.length; j ++) {
                if(startStation.staID === points[j].staID) {
                    startChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        routeName: points[j].routeName
                    });
                }
                else if(endStation.staID === points[j].staID) {
                    endChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        routeName: points[j].routeName
                    });
                }
            }

            this.showInfo = [];
            if(startChoice.length && endChoice.length) {
                let ans = null;
                let choice = []; // choice中存放了多个方案
                for(let i = 0; i < startChoice.length; i ++)
                    for(let j = 0; j < endChoice.length; j ++) {
                        let start = kvMap.get(JSON.stringify(startChoice[i]));
                        let end = kvMap.get(JSON.stringify(endChoice[j]));
                        let res = this.dijkstra(graph, start, end, points.length);
                        if(ans === null || ans.distance > res.distance) {
                            ans = res;
                            choice = [res];
                        }
                        else if(ans.distance === res.distance) {
                            choice.push(res);
                        }
                    }
                    console.log(choice);

                    // 将choice中的方案按线路进行划分
                    for(let i = 0; i < choice.length; i ++) {
                        let routeDetail = []; // 详细路径,包括控制点
                        let plan = choice[i].scheme;
                        let stations = []; // 部分路径
                        let path = []; // 整体路径
                        for(let j = 0; j < plan.length; j ++) {
                            stations.push({
                                staID: points[plan[j]].staID,
                                staName: points[plan[j]].staName,
                                routeName: points[plan[j]].routeName
                            });
                            if(j === plan.length - 1 || points[plan[j]].routeName != points[plan[j + 1]].routeName) { // 处于最末尾,或下一个站点和自己不是同一路线(结束/换乘)
                                path.push(stations);
                                routeDetail.push(this.findAllPoints(stations));
                                stations = [];
                            }
                        }
                        if(path.length) {
                            this.showInfo.push({
                                distance: choice[0].distance,
                                scheme: path,
                                routeDetail: routeDetail
                            });
                        }
                    }
                    // console.log(this.showInfo);
            }

        },
        onSubmit() {
            if(this.lenSet === false) {
                this.setTotalLen();
                this.lenSet = true;
            }
            this.getMinTimeCost(this.searchInfo.sStation, this.searchInfo.eStation, new Date(2023, 4, 12, 16, 0, 0));
            // this.findRunBus(new Date());
        },
        handlerTimeFormatter(elTime) { // 对el时间格式的处理
            let time = elTime.toISOString()
            return {
                year: parseInt(time.substr(0, 4)),
                month: parseInt(time.substr(5, 2)) - 1, // 0序
                day: parseInt(time.substr(8, 2)),
                hour: (parseInt(time.substr(11, 2)) + 8) % 24, // 东八区
                minute: parseInt(time.substr(14, 2)),
                second: parseInt(time.substr(17, 2))
            }
        },
    },
    watch: {
        'searchInfo': {
            deep: true,
            handler() {
                if(this.searchInfo.sStation !== '' && this.searchInfo.eStation !== '') {
                    let timeFormat = this.handlerTimeFormatter(this.searchInfo.startTime);
                    let dateTime = new Date(timeFormat.year, timeFormat.month, timeFormat.day,
                    timeFormat.hour, timeFormat.minute, timeFormat.second);
                    if(this.searchInfo.choice === 'minTimeCost') {
                        if(this.searchInfo.sStation !== '' && this.searchInfo.sStation !== null
                            && this.searchInfo.sStation !== undefined
                            && this.searchInfo.eStation !== '' && this.searchInfo.eStation !== null
                            && this.searchInfo.eStation !== undefined
                            && this.searchInfo.startTime !== '' && this.searchInfo.startTime !== null
                            && this.searchInfo.startTime !== undefined
                        ) {
                            this.getAxiosMinTimeCost(this.searchInfo.sStation, this.searchInfo.eStation, dateTime);
                        }
                    }
                    else {
                        if(this.searchInfo.sStation !== '' && this.searchInfo.sStation !== null
                            && this.searchInfo.sStation !== undefined
                            && this.searchInfo.eStation !== '' && this.searchInfo.eStation !== null
                            && this.searchInfo.eStation !== undefined
                            && this.searchInfo.startTime !== '' && this.searchInfo.startTime !== null
                            && this.searchInfo.startTime !== undefined
                        ) {
                            this.getAxiosMinTransfer(this.searchInfo.sStation, this.searchInfo.eStation, dateTime);
                        }
                        
                    }
                }
            }
        }
    }
}
</script>

<style scoped>
    .el-form >>> .el-form-item__label {
        font-size: 13px;
        font-weight: 600;
        /* color: rgb(240, 165, 15) */
    }

    .el-select {
        margin-left: -150px;
        margin-top: -10%;
        /* margin-bottom: 25px; */
    }

    .el-select >>> .el-input__inner {
        font-size: 13px;
        height: 30px;
    }

    .dtPicker {
        margin-left: -70px;
    }

    .NoSearch {
        font-size: 16px;
        font-weight: 500;
        color: goldenrod;
    }
</style>