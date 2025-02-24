<template>
  <div class="formPage">
        <el-row>
            <el-col :span="21">
                <h4 style="margin-left: 70px;">{{ path.routeName }}线路信息表</h4>
            </el-col>
            <el-col :span="3">
                <el-button type="warning" plain size="small" @click="comeBack">返回</el-button>
            </el-col>
        </el-row>
        <el-form :model="dynamicValidateForm" ref="dynamicValidateForm" label-width="100px" class="demo-dynamic">
        <el-form-item
            v-for="(point, index) in dynamicValidateForm.points"
            :label="labelFormatter(index)"
            :key="point.key"
            :prop="'points.' + index + '.pointLng'"
            :rules="{ required: true, message: '站点/控制点不能为空', trigger: 'blur' }">
            <br>

            <el-row :gutter="30" v-if="dynamicValidateForm.points[index].staID===-1">
                <el-col :span="5">
                    <el-form-item class="lnglat">
                        <el-input v-model="dynamicValidateForm.points[index].pointLng" :disabled="true" placeholder="请点击地图"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="5">
                    <el-form-item class="lnglat">
                        <el-input v-model="dynamicValidateForm.points[index].pointLat" :disabled="true" placeholder="请点击地图"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="14">
                    <el-button-group style="margin-left: -48px;">
                        <el-button type="warning" plain @click.prevent="removePoint(point)" size="small" icon="el-icon-delete" :disabled="deleteJudge(point)"></el-button>
                        <el-button type="warning" plain @click="addPoint(point, 0)" size="small" :disabled="stationBan">新增站点</el-button>
                        <el-button type="warning" plain @click="addPoint(point, -1)" size="small" :disabled="controlBan || index===dynamicValidateForm.points.length-1">新增控制点</el-button>
                    </el-button-group>
                </el-col>
            </el-row>

            <el-row :gutter="24" v-else>
                <el-col :span="4">
                    <!-- 要保证v-model的值不存在公共区域 -->
                    <el-select v-model="dynamicValidateForm.points[index]"
                        filterable placeholder="请选择/输入" 
                        ref="myselected"
                        @change="change"
                        value-key="staID">
                        <el-option
                            v-for="item in stationList"
                            :key="item.staID"
                            :label="item.staName"
                            :value="item">
                        </el-option>
                    </el-select>
                </el-col>
                <el-col :span="20">
                    <el-button-group class="spanJudge">
                        <el-button type="warning" plain @click.prevent="removePoint(point)" size="small" icon="el-icon-delete" :disabled="deleteJudge(point)"></el-button>
                        <el-button type="warning" plain @click="addPoint(point, 0)" size="small" :disabled="stationBan">新增站点</el-button>
                        <el-button type="warning" plain @click="addPoint(point, -1)" size="small" :disabled="controlBan || index===dynamicValidateForm.points.length-1">新增控制点</el-button>
                    </el-button-group>
                </el-col>
            </el-row>
        </el-form-item>
        <el-form-item>
            <el-button style="width: 100%; margin-left: -80px;" type="warning" plain size="small" @click="submitForm('dynamicValidateForm')">更新路线信息</el-button>
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
const CONTROL_POINT = -1;
const STATION_POINT = 0;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

// const jwt = localStorage.getItem('jwt');
// axios.defaults.headers.common['Authorization'] = jwt;

export default {
    name: "editRoute",
    props: {
        path: Object
    },
    data() {
        return {
            stationList: [],
            dynamicValidateForm: {
                points: [],
                routeName: ''
            },
            controlID: -1, // 需要去设置的下一个控制点的索引号
            stationBan: false, // 是否禁止设置站点
        }
    },
    mounted() {
        this.$bus.$on("getLocation_2", this.getLocation_2);
        this.$bus.$emit("setChoice", 0); // 向地图组件表明自己想要设置绘点即可，无需获取具体位置信息

        this.loadPoints();
        this.getStations();
    },
    methods: {
        loadPoints() { // 载入用于表单的points数组
            this.dynamicValidateForm.routeID = this.path.routeID;
            this.dynamicValidateForm.routeName = this.path.routeName;
            let arr = this.path.path;
            let save = this.dynamicValidateForm.points;
            for(let point of arr) {
                if(point.staID === null) { // 控制点
                    save.push({
                        staID: CONTROL_POINT,
                        pointID: point.pointID,
                        pointLng: point.pointLng,
                        pointLat: point.pointLat,
                        // key: Date.now()
                    })
                }
                else {
                    save.push({
                        staID: point.staID,
                        staName: point.staName,
                        pointID: point.pointID,
                        pointLng: point.pointLng,
                        pointLat: point.pointLat,
                        // key: Date.now()
                    })
                }
            }
        },
        getStations() { // 获取将当前城市的索引公交站点
            // let args = {
            //     cityID: JSON.parse(sessionStorage.getItem('city')).id
            // };
            axios.get(`${BASE}/station`, {
                params: {
                    cityID: JSON.parse(sessionStorage.getItem('city')).id
                }, 
                headers: {
                    Authorization: localStorage.getItem('jwt')
                }
            }).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.stationList = res.data.data;
                    }
                    else if(res.data.code === EXPIRE) {
                            MessageBox.alert("用户登录已过期！", "提示信息");
                            localStorage.clear();
                            sessionStorage.clear();
                            this.$router.replace('/login');
                        }
                    else {
                        MessageBox.alert(`表单初始化失败！\n错误信息为:${res.data.message}`, "提示信息");
                    }
                }
            ).catch(
                error => {
                    MessageBox.alert(`表单初始化失败！\n错误信息为:${error}`, "提示信息");
                }
            )
        },
        comeBack() { // 返回至上一层
            MessageBox.confirm("是否返回至上层界面？", "提示信息", {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
            }).then(
                () => {
                    this.$bus.$emit("changeEdit");
                }
            )
        },
        submitForm(formName) { // 表单提交
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    MessageBox.confirm("是否更新当前公交路线的相关信息", "提示信息", {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                    }).then(() => { // 因为线路之间站点的有序性,因此采用先删除再插入的方式进行更新

                        // cur_time = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');

                        let points = this.dynamicValidateForm.points;
                        for(let i = 0; i < points.length; i ++) {
                            if(points[i].staID === CONTROL_POINT) {
                                points[i].staID = null;
                                points[i].editTime = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
                            }
                        }
                        
                        let args = {
                            routeID: this.dynamicValidateForm.routeID,
                            routeName: this.dynamicValidateForm.routeName,
                            points: points,
                            editTime: moment(new Date()).format('YYYY-MM-DD HH:mm:ss'),
                            cityID: JSON.parse(sessionStorage.getItem('city')).id
                        };


                    
                        axios.patch(`${BASE}/route`, args, {
                            headers: {
                                'Authorization': localStorage.getItem('jwt')  // 在请求头中加上 Authorization 字段
                            }
                        }).then(
                            res => {
                                if(res.data.code == SUCCESS) {
                                    MessageBox.alert("更新公交路线成功！", "提示信息");
                                    this.$bus.$emit("changeEdit");
                                }
                                else if(res.data.code === EXPIRE) {
                                    MessageBox.alert("用户登录已过期！", "提示信息");
                                    localStorage.clear();
                                    sessionStorage.clear();
                                    this.$router.replace('/login');
                                }
                                else {
                                    MessageBox.alert(`更新公交路线失败！\n错误信息为:${res.data.message}`, "提示信息");
                                }
                            }
                        ).catch(error => {
                            MessageBox.alert(`更新公交路线失败！\n错误信息为:${error}`, "提示信息");
                        })

                    

                        // axios.patch(`${BASE}/updatePath`, {
                        //     routeID: 
                        // }) {

                        // }

                        // let cityID = JSON.parse(sessionStorage.getItem('city')).id;

                        // // 1.先将原先该条线路的相关信息删除
                        // let delInfo = {
                        //     cityID: cityID,
                        //     routeName: this.dynamicValidateForm.routeName
                        // };

                        // console.log(this.dynamicValidateForm);

                        // axios.post(`${BASE}/delPath`, delInfo)
                        // .then(() => {
                        //     // 2.再将最新的线路信息插入关系表中
                        //     let args = [];
                        //     let arr = this.dynamicValidateForm.points;
                        //     let bus = this.dynamicValidateForm.routeName;
                        //     let now = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
                        //     for(let point of arr) {
                        //         if(point.staID === CONTROL_POINT) {
                        //             // routeName, staID, isStation, pointLng, pointLat, editTime, cityID
                        //             args.push([bus, null, 0, point.pointLng, point.pointLat, now, cityID]);
                        //         }
                        //         else {
                        //             // routeName, staID, isStation, pointLng, pointLat, editTime, cityID
                        //             args.push([bus, point.staID, 1, point.pointLng, point.pointLat, now, cityID]);
                        //         }
                        //     }
                        //     axios.post(`${BASE}/newPath`, args).then(
                        //         res => {
                        //             if(res.data.status == SUCCESS) {
                        //                 MessageBox.alert("更新公交路线成功！", "提示信息");
                        //                 this.$bus.$emit("changeEdit");
                        //             }
                        //             else {
                        //                 MessageBox.alert(`更新公交路线失败！\n错误信息为:${res.data.error}`, "提示信息");
                        //             }
                        //         }
                        //     ).catch(error => {
                        //         MessageBox.alert(`更新公交路线失败！\n错误信息为:${error}`, "提示信息");
                        //     })
                        // })
                        // .catch(error => {
                        //     MessageBox.alert(`更新公交路线失败！\n错误信息为:${error}`, "提示信息");
                        // });
                    }
                )
                } 
                else {
                    MessageBox.alert(`请详尽您的填充信息！`, "提示信息");
                }
            });
        },
        removePoint(item) { // 移除选中的行
            let index = this.dynamicValidateForm.points.indexOf(item);
            if (index !== -1) {
                this.dynamicValidateForm.points.splice(index, 1);
            }
        },
        addPoint(item, type) { // 向当前站点后新增站点或控制点
            let index = this.dynamicValidateForm.points.indexOf(item);
            let addItem;
            console.log(item);
            if(type === STATION_POINT) {
                addItem = {
                    staID: STATION_POINT,
                    key: Date.now()
                }
            }
            else {
                addItem = {
                    staID: CONTROL_POINT,
                    pointLng: '',
                    pointLat: '',
                    key: Date.now()
                }
                this.controlID = index + 1;
            }
            let arr = this.dynamicValidateForm.points;
            arr.push(addItem);
            for(let i = arr.length - 1; i > index + 1; i --) {
                arr[i] = arr[i - 1];
            }
            arr[index + 1] = addItem;
        },
        change() { // 多选框选中
            this.$forceUpdate(); // 强制渲染
            this.stationBan = false;
            let arr = this.dynamicValidateForm.points;
            let lastID = -1;
            for(let i = 0; i < arr.length; i ++) {
                if(arr[i].staID !== CONTROL_POINT) {
                    if(lastID !== -1) {
                        if(arr[i].staID === lastID) { // 当前站点和上一个站点出现重复
                            MessageBox.alert("请保证设置的两个相邻站点不重复！", "提示信息");
                            this.stationBan = true; // 设置了两个重复的站点
                            break;
                        }
                    }
                    lastID = arr[i].staID;
                }
            }
        },
        getLocation_2(location) { // 获取到来自地图对应位置的坐标值,并将其存入对应points的index索引下
            if(this.controlID !== -1) {
                this.dynamicValidateForm.points[this.controlID].pointLng = location.lng;
                this.dynamicValidateForm.points[this.controlID].pointLat = location.lat;
            }
        },
        labelFormatter(index) { // 对标签的格式化输出
            let arr = this.dynamicValidateForm.points;
            if(arr[index].staID === CONTROL_POINT) return "控制点";
            else {
                let cnt = 0;
                for(let i = 0; i <= index; i ++) cnt += arr[i].staID !== CONTROL_POINT;
                return "站点" + cnt;
            }
        },
        deleteJudge(point) { // 判断当前结点是否不能进行删除操作
            if(point.staID === CONTROL_POINT) return false; // 控制点可以直接删除
            else { // 对于站点的情况
                let arr = this.dynamicValidateForm.points;
                let index = arr.indexOf(point);
                if(index === 0 || 
                arr[index - 1].staID === CONTROL_POINT && (index == arr.length - 1 || arr[index + 1].staID === CONTROL_POINT)
                ) return true; // 当前站点是第一个站点或者其上一个点是控制点且删除当前站点后控制点无法被两个站点包裹时,无法被删除
            }
            return false;
        }
    },
    beforeDestroy() {
        this.$bus.$off("getLocation_2");
    },
    computed: { // 计算属性
        controlBan() { // 确保每次仅能对一个控制点进行设置
            let arr = this.dynamicValidateForm.points;
            let cnt = 0;
            for(let i = 0; i < arr.length; i ++) cnt += arr[i].staID === CONTROL_POINT && arr[i].pointLng === '';
            return cnt === 1;
        }
    },
    watch: {
        'dynamicValidateForm.points': { // 若points发生变更则重新渲染地图
            deep: true,
            handler() {
                let drawPoints = []; // 绘图数组
                let pArr = this.dynamicValidateForm.points;
                // console.log(pArr);
                for(let point of pArr) {
                    if(point.staID === CONTROL_POINT) { // 若是控制点
                        if(point.pointLng !== '' && point.pointLat !== '') { // 该控制点已经完成了设置
                            drawPoints.push(
                                {
                                    isStation: false, // 标志为控制点
                                    LngLat: [point.pointLng, point.pointLat]
                                }
                            );
                        }
                    }
                    else if(point.staID !== STATION_POINT){ // 若是已经完成设置的站点(等于STATION_POINT表示当前站点未完成设置)
                        drawPoints.push(
                            {
                                isStation: true, // 标志为站点
                                title: point.staName,
                                LngLat: [point.pointLng, point.pointLat]
                            }
                        );
                    }
                }
                this.$bus.$emit("drawPath", drawPoints);
            }
        }
    }
}
</script>

<style scoped>
    .formPage {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 8px; 
        width: 475px;
        height: 445px;
        padding: 18px;
        overflow-y:auto;
        /* overflow-x:auto;  */
    }

    .el-select {
        margin-top: -10px;
        margin-left: -140px;
        width: 50%;
        height: 10px;
    }

    .el-input {
        margin-left: -100px;
        width: 100px;
        height: 40px;
    }
    .el-form >>> .el-form-item__label {
        font-size: 10px;
        font-weight: 600;
        /* color: rgb(240, 165, 15) */
    }

    .el-input >>> .el-input__inner {
        font-size: 10px;
        height: 30px;
    }

    .el-select >>> .el-input__inner {
        font-size: 10px;
        height: 30px;
    }

    /* .el-select >>> .el-input__inner {
        font-size: 15px;
        height: 100%;
    } */

    .timeInput{
        margin-left: -46%;
        width: 250px;
    }
    .submitButton {
        width: 100%;
    }

    .lnglat {
        margin-top: -10px;
        width: 70px;
    }

    .spanJudge {
        margin-left: 60px;
    }
</style>