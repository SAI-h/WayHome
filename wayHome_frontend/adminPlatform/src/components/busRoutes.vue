<template>
    <div>
        <div id="routes">
            <new-path v-if="choice==='newPath'"></new-path>
        </div>
        <div v-if="choice==='showInfo'">
            <div class="tablePage" v-if="!isEdit">
                <el-button type="success" id="newBt" size="small" @click="changeChoice('newPath')">新建</el-button>
                <h4>公交线路汇总表</h4>
                <el-table
                :data="tableData.filter(data => !search || data.routeName.includes(search))"
                highlight-current-row
                @current-change="handleCurrentChange"
                style="width: 100%" max-height="400px">
                    <el-table-column label="更新时间" prop="editTime" :formatter="timestampToTime"></el-table-column>
                    <el-table-column label="线路名" prop="routeName">
                        <!-- eslint-disable-next-line -->
                        <template slot-scope="scope">
                            <el-popover trigger="hover" placement="top">
                            <p>线路详情: {{ scope.row.hoverShow }}</p>
                            <div slot="reference" class="name-wrapper">
                                <el-tag size="medium">{{ scope.row.routeName }}</el-tag>
                            </div>
                            </el-popover>
                        </template>
                    </el-table-column>
                    <el-table-column align="right">
                        <!-- eslint-disable-next-line -->
                        <template slot="header" slot-scope="scope">
                            <el-input v-model="search" placeholder="输入线路名搜索"/>
                        </template>
                        <template slot-scope="scope">
                            <el-button type="primary" size="small"
                            @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                            <el-button type="danger" size="small"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="editPage" v-else>
                <edit-route :path="editRoute"></edit-route>
            </div>
        </div>
    </div>
</template>

<script>
import newPath from './newPath.vue'
import editRoute from './editRoute.vue'
import moment from 'moment'
import axios from 'axios'
import { MessageBox } from 'element-ui';

const ERROR = 404;
const SUCCESS = 200;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

export default {
    name:'busRoutes',
    data() {
        let checkVal = (rule, value, callback) => {
            if(value === "") return callback(new Error('该内容不能为空！'));
            else return callback();
        };
        return {
            choice: 'showInfo',
            tableData: [], // 展示的数据
            search: '',
            isEdit: false,
            editRoute: ''
        }
    },
    components:{
        newPath, editRoute
    },
    mounted() {
        this.$bus.$on("changeChoice", this.changeChoice);
        this.$bus.$on("loadTable", this.loadTable);
        this.$bus.$on("changeEdit", this.changeEdit);
        this.loadTable();
    },
    methods: {
        changeChoice(tag) {
            this.loadTable();
            this.choice = tag;
        },
        loadTable() { // 加载表格
            this.tableData = [];
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).id
            };
            axios.get(`${BASE}/route`, {params:args}).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        let msg = res.data.data;
                        // console.log(msg);
                        let paths = new Map();

                        for(let path of msg) {
                            if(!paths.has(path.routeName)) {
                                paths.set(path.routeName, {
                                    routeID: path.routeID,
                                    cityID: path.cityID,
                                    remarks: path.remarks,
                                    editTime: path.editTime,
                                    points: [],
                                    hoverShow: ' | '
                                });
                                for(let point of path.points) {
                                    paths.get(path.routeName).points.push(
                                        {
                                            staID: point.staID,
                                            pointID: point.pointID,
                                            staName: point.staName,
                                            pointLng: point.pointLng,
                                            pointLat: point.pointLat,
                                            staAddress: point.staAddress
                                        }
                                    )
                                    if(point.staName !== null) {
                                        paths.get(path.routeName).hoverShow += point.staName + " | ";
                                    }
                                }
                            }
                        }

                        for(let [key, value] of paths) {
                            this.tableData.push({
                                routeID: value.routeID,
                                routeName: key,
                                cityID: value.cityID,
                                remarks: value.remarks,
                                editTime: value.editTime,
                                path: value.points,
                                hoverShow: value.hoverShow
                            })
                        }
                        this.tableData.sort((path_1, path_2) => { // 按照路线更新时间进行排序
                            if(path_1.editTime < path_2.editTime) {
                                return 1;
                            }
                            else if(path_1.editTime > path_2.editTime) {
                                return -1;
                            }
                            else return 0;
                        })
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
        timestampToTime(row) { // 时间格式化
            return moment(row.editTime).format('YYYY-MM-DD HH:mm:ss');
        },
        handleCurrentChange(currentRow) { // 当选中的行发生变更时,将当前行的公交线路绘制在地图上展示
            let points = [];
            let center = [0, 0]; // 绘图展示的中心点设置为所有点经纬度的平均值点
            for(let point of currentRow.path) {
                if(point.staID !== null) {
                    points.push({
                        isStation: true,
                        LngLat: [point.pointLng, point.pointLat],
                        title: point.staName
                    })
                }
                else {
                    points.push({
                        isStation: false,
                        LngLat: [point.pointLng, point.pointLat],
                    })
                }
                center[0] += point.pointLng;
                center[1] += point.pointLat;
            }
            center[0] /= currentRow.path.length;
            center[1] /= currentRow.path.length;
            this.$bus.$emit("getCenter", center);
            this.$bus.$emit("drawPath", points);
        },
        handleEdit(index, routeInfo) { // 对站点编辑事件进行处理
            this.editRoute = routeInfo;
            this.isEdit = true;
        },
        handleDelete(index, routeInfo) {
            MessageBox.confirm("是否确定要删除该公交线路？", "提示信息",  {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
            })
            .then(
                () => {
                    axios.delete(`${BASE}/route/${routeInfo.routeID}`)
                    .then(res => {
                        if(res.data.code === SUCCESS) {
                            MessageBox.alert("路线删除成功！", "提示信息");
                            this.loadTable();
                        }
                        else {
                            MessageBox.alert(`删除公交路线失败！\n错误信息为:${res.data.error}`, "提示信息");
                        }
                    })

                    // let delInfo = {
                    //     cityID: JSON.parse(sessionStorage.getItem('city')).id,
                    //     routeName: routeInfo.routeName
                    // }

                    // axios.post(`${BASE}/countSchedule`, delInfo)
                    // .then(
                    //     res => {
                    //         if(res.data.status === SUCCESS) {
                    //             if(res.data.msg[0].count === 0) { // 和班次绑定数目为0则可以进行删除
                    //                 axios.delete(`${BASE}/route`, delInfo)
                    //                 .then(res => { 
                    //                     if(res.data.code === SUCCESS) {
                    //                         MessageBox.alert("路线删除成功！", "提示信息");
                    //                         this.loadTable();
                    //                     }
                    //                     else {
                    //                         MessageBox.alert(`删除公交路线失败！\n错误信息为:${res.data.error}`, "提示信息");
                    //                     }
                    //                 })
                    //                 .catch(error => {
                    //                     MessageBox.alert(`删除公交路线失败！\n错误信息为:${error}`, "提示信息");
                    //                 })
                    //             }
                    //             else { // 拒绝删除
                    //                 MessageBox.alert(`存在${res.data.msg[0].count}条班次绑定信息！请弃用解绑后重试！\n`, "提示信息");
                    //             }
                    //         }
                    //         else {
                    //             MessageBox.alert(`删除公交路线失败！\n错误信息为:${res.data.error}`, "提示信息");
                    //         }
                    //     }
                    // )
                    // .catch(
                    //     error => {
                    //         MessageBox.alert(`删除公交路线失败！\n错误信息为:${error}`, "提示信息");
                    //     }
                    // )
                }
            )
        },
        changeEdit() { // 更改编辑状态
            this.loadTable();
            this.isEdit = false;
        }
    },
    beforeDestroy() {
        this.$bus.$off("changeChoice");
        this.$bus.$off("loadTable");
        this.$bus.$off("changeEdit");
    },
    computed: {
        IDformatter() { // 格式化站点ID号
            let tmp = this.infoShow.staID + "";
            while(tmp.length < 6) {
                tmp = "0" + tmp;
            }
            return tmp;
        },
        TimeFormatter() { // 时间格式化
            return moment(this.infoShow.setTime).format('YYYY-MM-DD HH:mm:ss');
        }
    }
}
</script>

<style scoped>
    #routes {
        vertical-align: baseline;
        width: 45%;
        height: 100%;
        margin-top: -490px;
        padding: 10px;
    }

    #newBt {
        margin-left: -90%;
    }

    .tablePage {
        border-radius: 8px; 
        width: 47%;
        height: 90%;
        padding: 20px;
        margin-top: -3%;
        overflow-y:auto;
        overflow-x:auto; 
    }

    .editPage {
        border-radius: 8px; 
        width: 50%;
        /* height: 530px; */
        padding: 3px;
        margin-top:-15px;
        overflow-y:auto;
        overflow-x:auto; 
    }

    .el-input {
        width: 90%;
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
</style>