<template>
  <div>
    <div v-if="!pathInfo.path.length">
        <el-empty :image-size="250" description="请点击左侧路线获取对应班次数据"></el-empty>
    </div>
    <div v-else-if="!schedule.length && status === 'scheduleShow'">
        <el-empty :image-size="250" description="当前线路暂无绑定的班次"></el-empty>
        <el-button type="warning" plain size="small" @click="newSchedule">立即绑定</el-button>
    </div>
    <div v-else-if="status === 'scheduleShow'">
        <el-row>
            <el-col :span="20">
                <h4 style="margin-left: 70px;">{{ pathInfo.routeName }}班次安排表</h4>
            </el-col>
            <el-col :span="4">
                <el-button type="warning" plain size="small" @click="newSchedule">班次绑定</el-button>
            </el-col>
        </el-row>
        <el-card v-for="sch in schedule" :key="sch.routeID">
            <div slot="header" class="clearfix">
                <span style="margin-left: -40%;">更新时间: {{ TimeFormatter(sch.editTime) }}</span>
                <el-button style="float: right; padding: 3px 0" type="danger" @click="delSchedule(sch)">弃用</el-button>
                <el-button style="float: right; padding: 3px 0" type="primary" @click="scheduleUpdate(sch)">编辑</el-button>
            </div>
            <div>
                <h6 style="float: left;">方向</h6>
                <el-tag type="warning">{{ sch.sStation }}</el-tag>
                <i class="el-icon-right"></i>
                <el-tag type="warning">{{ sch.eStation }}</el-tag>
            </div>
            <br>
            <div>
                <h6 style="float: left;">首班车发车时间</h6>
                <span style="margin-left: -200px;">{{ sch.startTime }}</span>
                <br>
                <h6 style="float: left;">末班车发车时间</h6>
                <span style="margin-left: -200px;">{{ sch.endTime }}</span>
                <br>
                <h6 style="float: left;">发车间隔</h6>
                <span style="margin-left: -175px;">{{ sch.deInterval }}分钟</span>
                <br>
                <h6 style="float: left;">运行时间</h6>
                <span>{{ workdaysFormatter(sch.workdays) }}</span>
            </div>
        </el-card>
    </div>
    <div v-else-if="status === 'newSchedule'">
        <newSchedule :pathInfo="pathInfo"></newSchedule>
    </div>
    <div v-else>
        <scheduleUpdate :pathInfo="updateInfo"></scheduleUpdate>
    </div>
  </div>
</template>

<script>
import newSchedule from './newSchedule.vue'
import scheduleUpdate from './scheduleUpdate.vue'
import axios from 'axios'
import { MessageBox } from 'element-ui'
import moment from 'moment'

const ERROR = 404;
const SUCCESS = 200;
const FAILURE = 202;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name: "scheduleRight",
    components: {
        newSchedule, scheduleUpdate
    },
    data() {
        return {
            pathInfo: {
                routeName: '',
                path: [],
            },
            status: 'scheduleShow',
            schedule: [],
            updateInfo: []
        }
    },
    mounted() {
        this.$bus.$on("scheduleShow", this.scheduleShow);
        this.$bus.$on("changeStatus", this.changeStatus);
    },
    methods: {
        scheduleShow(pathInfo) {
            this.pathInfo.routeName = pathInfo.routeName;
            this.pathInfo.routeID = pathInfo.routeID;
            this.pathInfo.path = pathInfo.path;
            // console.log("--------------------");
            // console.log(this.pathInfo);
            // console.log("--------------------");
            // let args = {
            //     routeID: ,
            //     // cityID: JSON.parse(sessionStorage.getItem('city')).id
            // }
            axios.get(`${BASE}/schedule/${this.pathInfo.routeID}`)
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.schedule = res.data.data;
                        // console.log(this.schedule);
                        this.schedule.forEach(item => {
                            // console.log(item);
                            if(item.direction === true) {
                                item.sStation = this.pathInfo.path[0].staName;
                                item.eStation = this.pathInfo.path[this.pathInfo.path.length - 1].staName;
                            }
                            else {
                                item.sStation = this.pathInfo.path[this.pathInfo.path.length - 1].staName;
                                item.eStation = this.pathInfo.path[0].staName;
                            }
                        })
                    }
                    else {
                        MessageBox.alert(`班次获取失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            )
            .catch(
                error => {
                    MessageBox.alert(`班次获取失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        newSchedule() {
            this.status = "newSchedule";
        },
        changeStatus(status) {
            this.status = status;
            this.scheduleShow(this.pathInfo);
        },
        delSchedule(schedule) {
            MessageBox.confirm("是否弃用该公交班次信息", "提示信息", {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
            })
            .then(() => {
                axios.delete(`${BASE}/schedule/${schedule.scheduleID}`)
                .then(
                    res => {
                        if(res.data.code === SUCCESS) {
                            MessageBox.alert("班次弃用成功", "提示信息");
                            this.scheduleShow(this.pathInfo);
                        }
                        else {
                            MessageBox.alert(`班次弃用失败！\n错误信息为:${res.data.message}`, "提示信息");
                        }
                    }
                )
                .catch(
                    error => {
                        MessageBox.alert(`班次弃用失败！\n错误信息为:${error}`, "提示信息");
                    }
                )
            })
        },
        scheduleUpdate(schedule) {
            this.status = 'scheduleUpdate';
            this.updateInfo = {
                scheduleID: schedule.scheduleID,
                routeName: this.pathInfo.routeName,
                path: this.pathInfo.path,
                schedule: schedule
            }
            let workdays = JSON.parse(this.updateInfo.schedule.workdays)
            let key_arr = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
            let save = [];
            for(let i = 0; i < key_arr.length; i ++) {
                if(workdays[key_arr[i]]) {
                    save.push(key_arr[i]);
                }
            }
            this.updateInfo.schedule.workdays = save;
        }
    },
    beforeDestroy() {
        this.$bus.$off("scheduleShow");
        this.$bus.$off("changeStatus");
    },
    computed: {
        TimeFormatter() { // 时间格式化
            return time => {
                return moment(time).format('YYYY年MM月DD日 HH:mm:ss');
            }
        },
        workdaysFormatter() { // 运行时间格式化
            return dat => {
                // console.log(dat);
                let workdays = JSON.parse(dat);
                // console.log(workdays);
                let res = "";
                let key_arr = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
                for(let i = 0; i < key_arr.length; i ++) {
                    if(workdays[key_arr[i]]) {
                        res += key_arr[i] + " ";
                    }
                }
                return res;
            }
        }
    }
}
</script>

<style scoped>
    span {
        font-size: 13px; 
        color: grey;
    }
</style>