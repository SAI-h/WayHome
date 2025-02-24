<template>
    <div class="tablePage">
        <h4>公交线路/班次汇总表</h4>
        <el-table
        :data="tableData.filter(data => !search || data.routeName.includes(search))"
        highlight-current-row
        @current-change="handleCurrentChange"
        style="width: 100%" max-height="400px">
            <el-table-column label="线路名" prop="routeName">
                <!-- eslint-disable-next-line -->
                <template slot-scope="scope">
                    <div slot="reference" class="name-wrapper">
                        <el-tag size="medium">{{ scope.row.routeName }}</el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="端点1">
                <!-- eslint-disable-next-line -->
                <template slot-scope="scope">
                    <div slot="reference" class="name-wrapper">
                        <el-tag size="medium" type="warning">{{ scope.row.path[0].staName }}</el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="端点2">
                <!-- eslint-disable-next-line -->
                <template slot-scope="scope">
                    <div slot="reference" class="name-wrapper">
                        <el-tag size="medium" type="warning">{{ scope.row.path[scope.row.path.length - 1].staName }}</el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column align="right">
                <!-- eslint-disable-next-line -->
                <template slot="header" slot-scope="scope">
                    <el-input v-model="search" style="margin-left: -15px;" placeholder="输入线路名搜索"/>
                </template>
                <template slot-scope="scope">
                    <div slot="reference" class="name-wrapper">
                        <el-tag size="medium" type="success">全程共{{ scope.row.stationCnt }}站</el-tag>
                    </div>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
import moment from 'moment'
import axios from 'axios'
import { MessageBox } from 'element-ui'

const ERROR = 404;
const SUCCESS = 200;
const EXPIRE = 702;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

// const jwt = localStorage.getItem('jwt');
// axios.defaults.headers.common['Authorization'] = jwt;

export default {
    name: "scheduleLeft",
    data() {
        return {
            tableData: [], // 展示的数据
            search: ''
        }
    },
    mounted() {
        this.loadTable();
    },
    methods: {
        loadTable() { // 加载表格
            this.tableData = [];
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).id
            };
            axios.get(`${BASE}/route`, {
                params:args,
                headers: {
                    'Authorization': localStorage.getItem('jwt')
                }
            }).then(
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
                                    hoverShow: ' | ',
                                    stationCnt: 0
                                });
                                // console.log(path.points);
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
                                        // path.hoverShow += point.staName + " | ";
                                        // path.stationCnt++;
                                        paths.get(path.routeName).hoverShow += point.staName + " | ";
                                        paths.get(path.routeName).stationCnt++;
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
                                hoverShow: value.hoverShow,
                                stationCnt: value.stationCnt
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
        timestampToTime(row) { // 时间格式化
            return moment(row.editTime).format('YYYY-MM-DD HH:mm:ss')
        },
        handleCurrentChange(pathInfo) {
            this.$bus.$emit("scheduleShow", pathInfo);
        }
    }
}
</script>

<style scoped>
    .tablePage {
        height: 100%;
        overflow-y:auto;
        overflow-x:auto; 
    }

</style>