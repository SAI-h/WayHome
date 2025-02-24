<template>
    <div>
        <el-row>
            <el-col :span="20">
                <h4 style="margin-left: 70px;">{{ schedule.routeName }}班次绑定</h4>
            </el-col>
            <el-col :span="4">
                <el-button type="warning" plain size="small" @click="comeback">返回</el-button>
            </el-col>
        </el-row>
        <el-form :model="schedule" :rules="rules" ref="ruleForm">
            <el-form-item label="班次方向">
                <el-input size="small" v-model="schedule.startStation" :disabled="true"></el-input>
                <el-input size="small" v-model="schedule.endStation" :disabled="true"></el-input>
                <el-button size="small" type="warning" style="margin-left: 10px;" plain @click="swapDirection">交换</el-button>
            </el-form-item>
            <el-form-item label="路线详情">
                <h5>
                    {{ routeGetter }}
                </h5>
            </el-form-item>
            <el-form-item label="路线长度">
                <h5 style="float: left; color: blue;">
                    {{ schedule.totalLen }}米
                </h5>
            </el-form-item>
            <el-row :gutter="24">
                <el-col :span="12">
                    <el-form-item label="始发" prop="startTime">
                        <el-time-picker placeholder="选择时间" @change="handlerTimeChange" v-model="schedule.startTime" size="small" style="width: 70%;"></el-time-picker>
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="末班" prop="endTime">
                        <el-time-picker placeholder="选择时间" @change="handlerTimeChange" v-model="schedule.endTime" size="small" style="width: 70%;"></el-time-picker>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="发车间隔" prop="deInterval">
                <el-input v-model.number="schedule.deInterval" size="small" style="margin-left: -170px;" placeholder="请输入发车时间间隔" @blur="handlerTimeChange"></el-input> 分钟
            </el-form-item>
            <el-form-item label="运行时间" prop="type">
                <el-checkbox-group v-model="schedule.type">
                    <el-checkbox label="周一" name="type"></el-checkbox>
                    <el-checkbox label="周二" name="type"></el-checkbox>
                    <el-checkbox label="周三" name="type"></el-checkbox>
                    <el-checkbox label="周四" name="type"></el-checkbox>
                    <el-checkbox label="周五" name="type"></el-checkbox>
                    <el-checkbox label="周六" name="type"></el-checkbox>
                    <el-checkbox label="周日" name="type"></el-checkbox>
                </el-checkbox-group>
            </el-form-item>
            <el-form-item label="备注">
                <el-input type="textarea" style="width: 90%" v-model="schedule.remarks"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="warning" style="width:100%" @click="submitForm('ruleForm')">提交</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script>
import { MessageBox } from 'element-ui'
import axios from 'axios'
import moment from 'moment'

const ERROR = 404;
const SUCCESS = 200;
const FAILURE = 202;
const EXPIRE = 702;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

// const jwt = localStorage.getItem('jwt');
// axios.defaults.headers.common['Authorization'] = jwt;

export default {
    name: "newSchedule",
    props: {
        pathInfo: Object
    },
    data() {
        return {
            schedule: {
                routeID: this.pathInfo.routeID,
                routeName: this.pathInfo.routeName,
                startStation: this.pathInfo.path[0].staName,
                startID: this.pathInfo.path[0].staID,
                endStation: this.pathInfo.path[this.pathInfo.path.length - 1].staName,
                endID: this.pathInfo.path[this.pathInfo.path.length - 1].staID,
                path: this.pathInfo.path,
                startTime: '',
                endTime: '',
                deInterval: '',
                type: [],
                remarks: '',
                totalLen: 0,
                direction: true
            },
            rules: {
                type: [
                    { type: 'array', required: true, message: '请至少选择一个活动性质', trigger: 'change' }
                ],
                startTime: [
                    { type: 'date', required: true, message: '请选择首班车发车时间', trigger: 'change' }
                ],
                endTime: [
                    { type: 'date', required: true, message: '请选择末班车发车时间', trigger: 'change' }
                ],
                deInterval: [
                    { required: true, message: '该项不能为空'},
                    { type: 'number', message: '时间间隔必须为数字值'}
                ],
                checkedWorkday:[
                    { type: 'array', required: true, message: '请至少选择一项', trigger: 'change' }
                ]
            }
        }
    },
    mounted() {
        this.schedule.totalLen = Math.round(this.getTotalLength(this.schedule.path));
        // this.schedule.totalLen = 0;
    },
    methods: {
        swapDirection() {
            let tmp = this.schedule.startStation;
            this.schedule.startStation = this.schedule.endStation;
            this.schedule.endStation = tmp;

            tmp = this.schedule.startID;
            this.schedule.startID = this.schedule.endID;
            this.schedule.endID = tmp;

            if(this.schedule.direction === true) this.schedule.direction = false;
            else this.schedule.direction = true;
        },
        comeback() { // 返回至上一层
            MessageBox.confirm("是否返回至上层界面？", "提示信息", {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
            }).then(
                () => {
                    this.$bus.$emit("changeStatus", 'scheduleShow');
                }
            )
        },
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if(valid) {
                    MessageBox.confirm("是否提交当前的公交班次信息", "提示信息", {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                    })
                    .then(() => {
                        let sT = this.handlerTimeFormatter(this.schedule.startTime);
                        let eT = {
                            hh: this.schedule.endTime.getHours(),
                            mm: this.schedule.endTime.getMinutes(),
                            ss: this.schedule.endTime.getSeconds()
                        }

                        let workdays = new Map([
                            ["周一", false],
                            ["周二", false],
                            ["周三", false],
                            ["周四", false],
                            ["周五", false],
                            ["周六", false],
                            ["周日", false]
                        ]);

                        for(let i = 0; i < this.schedule.type.length; i++) {
                            workdays.set(this.schedule.type[i], true);
                        }

                        const workdaysObj = Object.fromEntries(workdays);

                        // console.log(workdays);

                        let args = {
                            routeID: this.schedule.routeID,
                            // routeName: this.schedule.routeName,
                            // startStation: this.schedule.startID,
                            // endStation: this.schedule.endID,
                            // cityID: JSON.parse(sessionStorage.getItem('city')).id,
                            direction: this.schedule.direction,
                            deInterval: this.schedule.deInterval,
                            startTime: moment(new Date(1900, 1, 1, sT.hh, sT.mm, sT.ss)).format('HH:mm'),
                            endTime: moment(new Date(1900, 1, 1, eT.hh, eT.mm, eT.ss)).format('HH:mm'),
                            remarks: this.schedule.remarks,
                            editTime: moment(new Date()).format('YYYY-MM-DD HH:mm:ss'),
                            workdays: JSON.stringify(workdaysObj)
                        }

                        // console.log(this.schedule.type);

                        axios.post(`${BASE}/schedule`, args, {
                            headers: {
                                'Authorization': localStorage.getItem('jwt')
                            }
                        })
                        .then(
                            res => {
                                if(res.data.code === SUCCESS) {
                                    MessageBox.alert("班次绑定成功！", "提示信息");
                                    this.$bus.$emit("changeStatus", 'scheduleShow');
                                }
                                else if(res.data.code === EXPIRE) {
                                    MessageBox.alert("用户登录已过期！", "提示信息");
                                    localStorage.clear();
                                    sessionStorage.clear();
                                    this.$router.replace('/login');
                                }
                                else {
                                    MessageBox.alert(`班次绑定失败！\n错误信息为:${res.data.message}`, "提示信息");
                                }
                            }
                        )
                        .catch(
                            error => {
                                MessageBox.alert(`班次绑定失败！\n错误信息为:${error}`, "提示信息");
                            }
                        )
                    })
                } 
                else {
                    MessageBox.alert("请详实您的填充信息", "提示信息");
                }
            });
        },
        handlerTimeFormatter(elTime) { // 对el时间格式的处理
            return {
                hh: (parseInt(elTime.toISOString().substr(11, 2)) + 8) % 24, // 东八区
                mm: parseInt(elTime.toISOString().substr(14, 2)),
                ss: parseInt(elTime.toISOString().substr(17, 2))
            }
        },
        handlerTimeChange() { // 监测时间设置是否合法
            let sTime = this.schedule.startTime;
            let eTime = this.schedule.endTime;
            let sT = this.handlerTimeFormatter(sTime);
            let eT = this.handlerTimeFormatter(eTime);
            let sTotal = 0, eTotal = 0;
            if(sTime !== '' && eTime !== '') {
                sTotal = sT.hh * 3600 + sT.mm * 60 + sT.ss;
                eTotal = eT.hh * 3600 + eT.mm * 60 + eT.ss;
                if(sTotal > eTotal) {
                    MessageBox.alert("应保证末班车发车时间晚于或等于首班车发车时间！请重新设置！", "提示信息");
                    this.schedule.endTime = sTime;
                }
            }
            let interval = this.schedule.deInterval;
            if(sTime !== '' && eTime !== '' && interval !== '' && interval !== 0) {
                if(eTotal > sTotal && Math.floor((eTotal - sTotal) / 60) % interval !== 0) { // 末班车和首班车不是每隔一个时间间隔一班车,则对末班车进行修正
                    let last = Math.floor(Math.floor((eTotal - sTotal) / 60) / interval) * interval;
                    let min = (sT.mm + last) % 60;
                    let hou = sT.hh + Math.floor((sT.mm + last) / 60);
                    this.schedule.endTime = new Date(1900, 1, 1, hou, min, sT.ss);
                    MessageBox.alert("根据发车间隔,已对末班车时间进行修正！您可继续手动修正！", "提示信息");
                }
            }
        },
        getTotalLength(points) { // 获取公交路线的总长度
            let totalLen = 0;
            for(let i = 0; i < points.length - 1; i ++) {
                let first = new AMap.LngLat(points[i].pointLng, points[i].pointLat);
                let second = new AMap.LngLat(points[i + 1].pointLng, points[i + 1].pointLat);
                totalLen += first.distance(second);
            }
            return totalLen;
        }
    },
    computed: {
        routeGetter() {
            let res = "";
            if(this.schedule.startStation === this.schedule.path[0].staName) {
                for(let i = 0; i < this.schedule.path.length; i ++) {
                    if(this.schedule.path[i].staName === null) continue;
                    res += this.schedule.path[i].staName;
                    if(i !== this.schedule.path.length - 1) res += ' - ';
                }
            }
            else {
                for(let i = this.schedule.path.length - 1; i >= 0; i --) {
                    if(this.schedule.path[i].staName === null) continue;
                    res += this.schedule.path[i].staName;
                    if(i) res += ' - ';
                }
            }
            return res;
        }
    }
}
</script>

<style scoped>
    .el-form >>> .el-form-item__label {
        font-size: 10px;
        font-weight: 600;
        /* color: rgb(240, 165, 15) */
    }

    .el-checkbox >>> .el-checkbox__label {
        font-size: 10px;
        font-weight: 600;
    }

</style>