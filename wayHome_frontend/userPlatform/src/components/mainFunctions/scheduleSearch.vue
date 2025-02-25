<template>
    <div>
        <el-form>
            <el-form-item label="线路检索">
                <el-select v-model="routeNameChoice" filterable placeholder="请选择/输入想要检索的公交线路" @change="selectedItem">
                    <el-option
                        v-for="route in routesName"
                        :key="route"
                        :label="route"
                        :value="route">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-collapse>
                <el-collapse-item>
                    <template slot="title">
                        <span style="color: blue; font-size: 13px;">快速检索</span>
                    </template>
                    <div v-for="(value,index) of typeDiffer" :key="index">        
                        <el-form-item :label="value[0].substr(0, 1).toUpperCase()">
                            <el-button v-for="(choice, index) in value" :key="index" type="text" @click="routeNameSelected(choice)">{{ choice }}</el-button>
                        </el-form-item>
                    </div>
                </el-collapse-item>
            </el-collapse>
        </el-form>
        <br><br>
        <el-empty v-if="this.routeNameChoice === ''" class="NoSearch" :image-size="100">
                请键入您想查询的公交路线号！
        </el-empty>
        <el-card v-else class="box-card">
            <div slot="header" class="clearfix">
                <span style="color: rgb(15, 123, 123); font-size: 14px;">{{ routeNameChoice }}号线路班次安排</span>
            </div>
            <el-form>
                <el-form-item label="线路安排">
                    <span style="font-size: 10px; color: gray;">{{ path }}</span>
                </el-form-item>
                <el-form-item label="发车时间">
                    <el-radio-group v-model="workday" size="small">
                        <el-radio-button label="0">周一</el-radio-button>
                        <el-radio-button label="1">周二</el-radio-button>
                        <el-radio-button label="2">周三</el-radio-button>
                        <el-radio-button label="3">周四</el-radio-button>
                        <el-radio-button label="4">周五</el-radio-button>
                        <el-radio-button label="5">周六</el-radio-button>
                        <el-radio-button label="6">周日</el-radio-button>
                    </el-radio-group>
                
                    <h6>{{ schedule.dir_0.startStation }}至{{ schedule.dir_0.endStation }}方向</h6>
                    <span style="color: goldenrod;" v-if="!schedule.dir_0.sendTime[workday].length">
                        抱歉，当天该方向车辆未运行！
                    </span>
                    <span style="font-size: 10px;" v-else>
                        {{ getSendTime(schedule.dir_0.sendTime[workday]) }}
                    </span>
                    <h6>{{ schedule.dir_1.startStation }}至{{ schedule.dir_1.endStation }}方向</h6>
                    <span style="color: goldenrod;" v-if="!schedule.dir_1.sendTime[workday].length">
                        抱歉，当天该方向车辆未运行！
                    </span>
                    <span style="font-size: 10px;" v-else>
                        {{ getSendTime(schedule.dir_1.sendTime[workday]) }}
                    </span>
                </el-form-item>
                <el-form-item label="备注1">
                    <h6 style="float: left; color: blueviolet;">
                        {{ schedule.dir_0.startStation }} - {{ schedule.dir_0.endStation }}方向：
                        {{ schedule.dir_0.remarks !== '' ? schedule.dir_0.remarks : '没有填写备注信息' }}
                    </h6>
                </el-form-item>
                    
                <el-form-item label="备注2">
                    <h6 style="float: left; color: blueviolet;">
                        {{ schedule.dir_1.startStation }} - {{ schedule.dir_1.endStation }}方向：
                        {{ schedule.dir_1.remarks !== '' ? schedule.dir_1.remarks : '没有填写备注信息'}}
                    </h6>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script>
import axios from 'axios'
import { MessageBox } from 'element-ui'

const ERROR = 404;
const SUCCESS = 200;
const WEEK_DAYS = {
    '周一': 0,
    '周二': 1,
    '周三': 2,
    '周四': 3,
    '周五': 4,
    '周六': 5,
    '周日': 6
};
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name: "scheduleSearch",
    data() {
        return {
            routesName: [],
            typeDiffer: [],
            routeNameChoice: '',
            startStationTag: '', // 标记起点
            endStationTag: '', // 标记终点
            path: '',
            schedule: {
                dir_0: {
                    startStation: '',
                    endStation: '',
                    sendTime: [[], [], [], [], [], [], []],
                    remarks: ' '
                },
                dir_1: {
                    startStation: '',
                    endStation: '',
                    sendTime: [[], [], [], [], [], [], []],
                    remarks: ' '
                }
            },
            workday: 0,
        }
    },
    mounted() {
        this.initRoutes();
    },
    methods: {
        initRoutes() { // 获取所有的公交线路编号
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID,
                lazyLoad: true
            }
            axios.get(`${BASE}/route`, {params:args})
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.routesName = res.data.data;
                        this.setTypeDiffer();
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
        selectedItem() {
            this.$forceUpdate();
        },
        setTypeDiffer() {
            let save = {};
            for(let route of this.routesName) {
                let Char = route.substr(0, 1).toUpperCase();
                if(!save[Char]) save[Char] = [];
                save[Char].push(route);
            }
            let keys = Object.keys(save).sort();
            for(let key of keys) this.typeDiffer.push(save[key]);
        },
        routeNameSelected(choice) {
            this.routeNameChoice = choice;
        },
        async getrouteNameInfo() { // 获取当前需要查询的线路信息
            await axios.get(`${BASE}/route`, {
                    params:{ // 用于绘图
                        routeName: this.routeNameChoice, 
                        cityID: JSON.parse(sessionStorage.getItem('city')).cityID
                    }
                }
            )
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.path = '';
                        let info = res.data.data[0].points;
                        let points = [];
                        let avgLng = 0, avgLat = 0;
                        this.startStationTag = '';
                        this.endStationTag = '';
                        for(let point of info) {
                            points.push({
                                isStation: point.staName !== null,
                                LngLat: [point.pointLng, point.pointLat],
                                title: point.staName === null ? '' : point.staName
                            })
                            avgLng += point.pointLng;
                            avgLat += point.pointLat;
                            if(point.staName !== null) {
                                if(this.startStationTag === '') this.startStationTag = point.staName;
                                this.endStationTag = point.staName;
                                this.path += point.staName + '-';
                            }
                        }
                        this.path = this.path.substr(0, this.path.length - 1);
                        avgLng /= points.length;
                        avgLat /= points.length;
                        this.$bus.$emit("getCenter", [avgLng, avgLat]);
                        this.$bus.$emit("drawPath", points);
                    }
                    else {
                        MessageBox.alert(`线路查询失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            )
            .catch(
                error => {
                    MessageBox.alert(`线路查询失败！\n错误信息为:${error}`, "提示信息");
                }
            )

            await axios.get(`${BASE}/schedule`,{params:{
                routeName: this.routeNameChoice, 
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            },withCredentials: true} // 用于界面发车时间呈现
            )
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        let info = res.data.data;
                        this.schedule.dir_0.startStation = this.startStationTag;
                        this.schedule.dir_0.endStation = this.endStationTag;
                        this.schedule.dir_1.startStation = this.endStationTag;
                        this.schedule.dir_1.endStation = this.startStationTag;
                        for(let i = 0; i < 7; i ++) {
                            this.schedule.dir_0.sendTime[i] = [];
                            this.schedule.dir_1.sendTime[i] = [];
                        }
                        this.schedule.dir_0.remarks = ' ';
                        this.schedule.dir_1.remarks = ' ';
                        for(let sch of info) {
                            let set = 0; // 放置位置
                            if(sch.direction === true) set = 1;
                            let startTime = this.getIntTime(sch.startTime);
                            let endTime = this.getIntTime(sch.endTime);
                            let deInterval = sch.deInterval;
                            let workdays_record = JSON.parse(sch.workdays);
                            let workdays = [];
                            let r = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
                            for(let i = 0; i < 7; i ++) {
                                if(workdays_record[r[i]] === true) workdays.push(r[i]);
                            }
                            if(!set) {
                                this.schedule.dir_0.remarks += sch.remarks + ' '; 
                            }
                            else {
                                this.schedule.dir_1.remarks += sch.remarks + ' '; 
                            }
                            if(deInterval === 0) {
                                for(let day of workdays)
                                    if(!set)
                                        this.schedule.dir_0.sendTime[WEEK_DAYS[day]].push(this.getStringTime(startTime));
                                    else
                                        this.schedule.dir_1.sendTime[WEEK_DAYS[day]].push(this.getStringTime(startTime));
                            }
                            else {
                                for(let i = startTime; i <= endTime; i += deInterval) {
                                    if(!set) {
                                        for(let day of workdays)
                                            this.schedule.dir_0.sendTime[WEEK_DAYS[day]].push(this.getStringTime(i));
                                    }
                                    else {
                                        for(let day of workdays)
                                            this.schedule.dir_1.sendTime[WEEK_DAYS[day]].push(this.getStringTime(i));
                                    }
                                }
                            }
                        }
                        for(let i = 0; i < 7; i ++) {
                            this.schedule.dir_0.sendTime[i].sort(function(a, b) {
                                if(a < b) return -1;
                                else if(a > b) return 1;
                                else return 0;
                            })
                            this.schedule.dir_1.sendTime[i].sort(function(a, b) {
                                if(a < b) return -1;
                                else if(a > b) return 1;
                                else return 0;
                            })
                        }
                    }
                    else {
                        // console.log('error');
                        MessageBox.alert(`线路查询失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            )
            .catch(
                error => {
                    // console.log(212131);
                    MessageBox.alert(`线路查询失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        getIntTime(time) { // 获取传入时间的整数格式
            let hh = parseInt(time.substr(0, 2));
            let mm = parseInt(time.substr(3, 2));
            return hh * 60 + mm;
        },
        getStringTime(time) { // 获取传入时间的字符串表达形式
            let mm = '' + time % 60;
            let hh = '' + Math.floor(time / 60);
            if(hh.length === 1) hh = '0' + hh;
            if(mm.length === 1) mm = '0' + mm;
            return hh + ':' + mm;
        }
    },
    computed: {
        getSendTime() {
            return timeInfo => {
                let res = '';
                for(let time of timeInfo) res += time + ' ';
                return res;
            }
        }
    },
    watch: {
        routeNameChoice() {
            this.getrouteNameInfo();
        }
    }
}
</script>

<style scoped>
    .el-select {
        margin-left: -200px;
    }
    .el-select >>> .el-input__inner {
        width: 220px;
        font-size: 13px;
        height: 40px;
    }
    .el-form >>> .el-form-item__label {
        font-size: 13px;
        font-weight: 600;
        color: gray
    }

    .NoSearch {
        font-size: 16px;
        font-weight: 500;
        color: goldenrod;
    }
</style>