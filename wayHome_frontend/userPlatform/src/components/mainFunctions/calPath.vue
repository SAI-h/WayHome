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
const TASK_RECEIVE = 801;
const PROCESSING = 802;
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
                    for(let point of part) {
                        point.pointLng = point.point.pointLng;
                        point.pointLat = point.point.pointLat;
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
                    if(res.data.code === TASK_RECEIVE) {
                        const taskID = res.data.data;

                        const checkResult = () => {
                            axios.get(`${BASE}/calPath/result`, { params: { taskID: taskID } })
                                .then(resultRes => {
                                    if (resultRes.data.code === SUCCESS) {
                                        this.showInfo = resultRes.data.data;  // 展示计算结果
                                    } else if (resultRes.data.code === PROCESSING) {
                                        // 如果计算中，继续轮询
                                        setTimeout(checkResult, 100);  // 每N毫秒轮询一次
                                    } else {
                                        MessageBox.alert(`查询失败！\n错误信息为:${resultRes.data.message}`, "提示信息");
                                    }
                                })
                                .catch(error => {
                                    MessageBox.alert(`查询失败！\n错误信息为:${error}`, "提示信息");
                                });
                        };

                        // 初始启动轮询
                        checkResult();
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
            }}).then( res => {
                if(res.data.code === TASK_RECEIVE) {
                        const taskID = res.data.data;

                        const checkResult = () => {
                            axios.get(`${BASE}/calPath/result`, { params: { taskID: taskID } })
                                .then(resultRes => {
                                    if (resultRes.data.code === SUCCESS) {
                                        this.showInfo = resultRes.data.data;  // 展示计算结果
                                    } else if (resultRes.data.code === PROCESSING) {
                                        // 如果计算中，继续轮询
                                        setTimeout(checkResult, 100);  // 每N毫秒轮询一次
                                    } else {
                                        MessageBox.alert(`查询失败！\n错误信息为:${resultRes.data.message}`, "提示信息");
                                    }
                                })
                                .catch(error => {
                                    MessageBox.alert(`查询失败！\n错误信息为:${error}`, "提示信息");
                                });
                        };

                        // 初始启动轮询
                        checkResult();
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