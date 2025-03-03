<template>
    <div>
        <el-form>
            <el-form-item label="站点检索">
                <el-select v-model="stationChoice" filterable placeholder="请选择/输入想要检索的公交站点" value-key="staID" @change="selectedItem">
                    <el-option
                        v-for="station in stations"
                        :key="station.staID"
                        :label="station.staName"
                        :value="station">
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <el-empty v-if="this.stationChoice === ''" class="NoSearch">
                请键入您想查询的公交站点！
        </el-empty>
        <el-card v-else>
            <div slot="header" class="clearfix">
                <span style="color: rgb(15, 123, 123); font-size: 14px;">{{ stationChoice.staName }}站点详情</span>
            </div>
            <el-form>
                <el-form-item label="站点位置">
                    <span style="font-size: 10px; color: gray;">{{ stationChoice.staAddress }}</span>
                </el-form-item>
                <el-form-item label="经过路线"></el-form-item>
                <el-collapse v-model="activeName" accordion @change="routeSelected">
                    <el-collapse-item v-for="(value, index) of routes" :key="index" :name="index">
                        <template slot="title">
                            <span style="color: blue; font-size: 13px;">
                                路线{{ index+1 }}： {{ value.routeName }}
                            </span>
                        </template>
                        <el-steps :space="60" :active="stationsShow.length" direction="vertical">
                            <el-step v-for="(station, index) in stationsShow" 
                            :key="index" :title="station.staName"
                            :description="station.staName === stationChoice.staName ? '该站点为检索站点' : ''">
                            </el-step>
                        </el-steps>
                    </el-collapse-item>
                </el-collapse>
                <el-form-item label="备注">
                    <h6 style="float: left; color: blueviolet;">
                        {{ stationChoice.remarks !== '' ? stationChoice.remarks : '没有填写备注信息' }}
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
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name: "stationFindRoute",
    data() {
        return {
            stations: [],
            stationChoice: '',
            routes: [],
            activeName: '',
            stationsShow: [],
        }
    },
    mounted() {
        this.initStations();
    },
    methods: {
        selectedItem() {
            this.$forceUpdate();
            this.$bus.$emit("getCenter", [this.stationChoice.staLng, this.stationChoice.staLat]);
            this.getRoutes();
        },
        initStations() {
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
        async routeSelected(index) {
            this.stationsShow = [];                
            if (this.routes[index] && this.routes[index].points) {
                let points = this.routes[index].points;
                let mapShow = [];
                let avgLng = 0, avgLat = 0;
                for (let point of points) {
                    if (point.staName !== null) {
                        this.stationsShow.push(point);
                    }
                    mapShow.push({
                        isStation: point.staName !== null,
                        LngLat: [point.pointLng, point.pointLat],
                        title: point.staName === null ? '' : point.staName
                    });
                    avgLng += point.pointLng;
                    avgLat += point.pointLat;
                }
                avgLng /= points.length;
                avgLat /= points.length;
                this.$bus.$emit("getCenter", [avgLng, avgLat]);
                this.$bus.$emit("drawPath", mapShow);
            } else {
                console.warn('Selected route does not have points:', this.routes[index]);
            }
        },
        getRoutes() { // 获取经过该站点的路径
            

            axios.get(`${BASE}/route/${this.stationChoice.staID}`)
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.routes = res.data.data;
                        // console.log(this.routes);
                        // console.log(res.data.data);
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

            // axios.get('${BASE}/{}', {
            //     staID: this.stationChoice.staID,
            //     cityID: JSON.parse(sessionStorage.getItem('city')).cityID

            // this.routes = [];
            // axios.post(`${BASE}/getPathbyStation`, {
            //     staID: this.stationChoice.staID,
            //     cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            // })
            // .then(
            //     res => {
            //         if(res.data.status === SUCCESS) {
            //             this.routes = res.data.msg;
            //         }
            //         else {
            //             MessageBox.alert(`初始化失败！\n错误信息为:${res.data.error}`, "提示信息");
            //         }
            //     }
            // )
            // .catch(
            //     error => {
            //         MessageBox.alert(`初始化失败！\n错误信息为:${error}`, "提示信息");
            //     }
            // )
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