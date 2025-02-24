<template>
    <div class="formPage">
        <el-row>
            <el-col :span="21">
                <h4>新增站点信息表</h4>
            </el-col>
            <el-col :span="3">
                <el-button size="small" @click="comeBack">返回</el-button>
            </el-col>
        </el-row>
        <br>
        <el-form 
        v-loading="LnglatFindLoading" element-loading-text="请等待，正在获取当前地址的地理坐标..."
        label-position="left" label-width="80px" :model="stationInfo" status-icon :rules="rules" ref="stationInfo">
            <el-form-item label="公交站点名" prop="stationName">
                <el-input placeholder="请输入站点名称" v-model.trim="stationInfo.stationName" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="地址信息" prop="stationAddress">
                <el-input placeholder="请点击地图或输入地址信息" @blur="getLnglat" v-model.trim="stationInfo.stationAddress" autocomplete="off"></el-input>
            </el-form-item>
            <el-row>
                <el-col :span="12">
                    <el-form-item class="lnglat">
                        <el-input v-model="lngToString" :disabled="true"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item class="lnglat">
                        <el-input v-model="latToString" :disabled="true"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="备注信息">
                <el-input placeholder="请填写必要的备注信息" type="textarea" :rows="5" v-model="stationInfo.remarks"></el-input>
            </el-form-item>
            <el-button type="warning" plain class="submitButton" @click="newStation('stationInfo')">提交</el-button>
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
    name:'newStation',
    data() {
        let validateStationName = (rule, value, callback) => {
            if(value === '') {
                return callback(new Error('请输入站点名！'));
            }
            else callback(); // 该检验函数须设置else的情况
        };
        let validateStationAddress = (rule, value, callback) => {
            if(value === '') {
                return callback(new Error('请点击地图获取或直接输入站点地址！'))
            }
            else callback(); // 该检验函数须设置else的情况
        }
        return {
            stationInfo: {
                stationName: "",
                stationAddress: "",
                stationPosition:{
                    lng: "",
                    lat: ""
                },
                remarks: '',
            },
            LnglatFindLoading: false, // 通过模糊地址获取经纬度的loading
            rules: {
                stationName: [
                    {validator: validateStationName, trigger: 'blur'}
                ],
                stationAddress: [
                    {validator: validateStationAddress, trigger: 'blur'}
                ]
            }
        }
    },
    mounted() { 
        // 绑定全局事件
        this.$bus.$on("getLocation", this.getLocation);
        this.$bus.$emit("setChoice", 1); // 向地图组件表明自己想要设置绘点即可，以及获取具体位置信息
    },
    methods: {
        getLocation(location) { // 根据用户在地图上点击的位置获取地址信息(包含经纬度,从地图组件传送过来)
            this.stationInfo.stationAddress = location.address;
            this.stationInfo.stationPosition.lng = location.lng;
            this.stationInfo.stationPosition.lat = location.lat;
        },
        getLnglat() { // 根据用户输入的地址信息得到该地址的经纬度信息
            if(this.stationInfo.stationAddress !== "") {
                this.LnglatFindLoading = true;
                AMap.plugin("AMap.Geocoder", () =>  {
                    new AMap.Geocoder({ 
                        city: JSON.parse(sessionStorage.getItem('city')).name
                    }).getLocation(
                        this.stationInfo.stationAddress, (status, result) => {
                            if(status === "complete" && result.info === 'OK') {
                                let lng = result.geocodes[0].location.lng;
                                let lat = result.geocodes[0].location.lat;
                                this.stationInfo.stationPosition.lng = lng;
                                this.stationInfo.stationPosition.lat = lat;
                                // 向地图组件传递信息,使得其重新设置中心点
                                this.$bus.$emit("getCenter", [lng, lat]); 
                                this.LnglatFindLoading = false;
                            }
                            else {
                                MessageBox.alert("请求地址访问失败！", "地图数据处理信息", {
                                    distinguishCancelAndClose: true,
                                });
                            }
                        }
                    )
                })
            }
            else {
                this.stationInfo.stationPosition.lng = "";
                this.stationInfo.stationPosition.lat = "";
            }
        },
        newStation(formName) { // 向服务器提交新的站点信息表单
            this.$refs[formName].validate((valid) => {
                if (valid) { // 如果通过检验,更新数据库
                    MessageBox.confirm("是否确认提交新公交站点的相关信息", "提示信息", {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                    }).then(() => {
                        let now = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
                        let args = {
                            staName: this.stationInfo.stationName,
                            staAddress: this.stationInfo.stationAddress,
                            staLng: this.stationInfo.stationPosition.lng,
                            staLat: this.stationInfo.stationPosition.lat,
                            remarks: this.stationInfo.remarks,
                            // newTime: now,
                            editTime: now,
                            cityID: JSON.parse(sessionStorage.getItem('city')).id
                        };
                        // console.log(args);
                        axios.post(`${BASE}/station`, args, 
                        {
                            headers: {
                                'Authorization': localStorage.getItem('jwt')
                            }
                        }).then(
                            res => {
                                if(res.data.code == SUCCESS) {
                                    MessageBox.alert("新建公交站点成功！", "提示信息");
                                    // 清空
                                    this.stationInfo.stationName = "";
                                    this.stationInfo.stationAddress = "";
                                    this.stationInfo.stationPosition.lng = "";
                                    this.stationInfo.stationPosition.lat = "";
                                    this.stationInfo.remarks = "";
                                    this.$bus.$emit("loadTable");
                                }
                                else if(res.data.code === EXPIRE) {
                                    MessageBox.alert("用户登录已过期！", "提示信息");
                                    localStorage.clear();
                                    sessionStorage.clear();
                                    this.$router.replace('/login');
                                }
                                else {
                                    MessageBox.alert(`新建公交站点失败！\n错误信息为:${res.data.error}`, "提示信息");
                                }
                            }
                        ).catch(error => {
                            MessageBox.alert(`新建公交站点失败！\n错误信息为:${error}`, "提示信息");
                        })
                    })
                }
                else {
                    MessageBox.alert(`请详尽您的填充信息！`, "提示信息");
                }
            });
        },
        comeBack() { // 返回上一级
            if(this.stationInfo.stationName !== '' || this.stationInfo.stationAddress !== '' || this.stationInfo.remarks !== '') {
                MessageBox.confirm("您还有未编辑完成的站点信息,是否退出？", "提示信息", {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                }).then(
                    () => {
                        this.$bus.$emit("changeChoice", 'showInfo');
                    }
                )
            }
            else this.$bus.$emit("changeChoice", 'showInfo');
        }
    },
    beforeDestroy() {
        // 解绑全局事件
        this.$bus.$off("getLocation");
    },
    computed: { // 计算属性
        lngToString() {
            let lng = this.stationInfo.stationPosition.lng;
            if(lng === "") return "等待接收坐标值";
            else return this.stationInfo.stationPosition.lng + "E";
        },
        latToString() {
            let lat = this.stationInfo.stationPosition.lat;
            if(lat === "") return "等待接收坐标值";
            else return this.stationInfo.stationPosition.lat + "N";
        }
    }
}
</script>

<style scoped>
    .formPage {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 8px; 
        width: 100%;
        height: 90%;
        padding: 20px;
    }
    .el-input {
        width: 100%;
        height: 40px;
    }
    .el-form >>> .el-form-item__label {
        font-size: 10px;
        font-weight: 600;
        /* color: rgb(240, 165, 15) */
    }

    .el-input >>> .el-input__inner {
        font-size: 10px;
        height: 85%;
    }
    .timeInput{
        margin-left: -46%;
        width: 250px;
    }
    .submitButton {
        width: 100%;
    }

    .lnglat {
        margin-left: -100px;
    }
</style>