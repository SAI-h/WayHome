<template>
    <div>
        <div id="stations">
            <new-station v-if="choice==='newStation'"></new-station>
        </div>
        <div v-if="choice==='showInfo'">
            <div class="tablePage" v-if="!isEdit">
                <el-button type="success" id="newBt" size="small" @click="changeChoice('newStation')">新建</el-button>
                <h4>公交站点汇总表</h4>
                <el-table
                :data="tableData.filter(data => !search || data.staName.includes(search))"
                highlight-current-row
                @current-change="handleCurrentChange"
                style="width: 100%" max-height="400px">
                    <el-table-column label="更新时间" prop="editTime" :formatter="timestampToTime"></el-table-column>
                    <el-table-column label="站点名称" prop="staName">
                        <!-- eslint-disable-next-line -->
                        <template slot-scope="scope">
                            <el-popover trigger="hover" placement="top">
                            <p>站点名称: {{ scope.row.staName }}</p>
                            <p>站点地址: {{ scope.row.staAddress }}</p>
                            <p>站点备注: {{ scope.row.remarks }}</p>
                            <div slot="reference" class="name-wrapper">
                                <el-tag size="medium">{{ scope.row.staName }}</el-tag>
                            </div>
                            </el-popover>
                        </template>
                    </el-table-column>
                    <el-table-column align="right">
                        <!-- eslint-disable-next-line -->
                        <template slot="header" slot-scope="scope">
                            <el-input v-model="search" placeholder="输入站点名搜索"/>
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
                <el-button @click="goBack" style="margin-right: -450px;" size="small" type="warning" plain circle><i class="el-icon-close"></i></el-button>
                <h3>{{ stopName }}站·站点信息表</h3>
                <el-form :model="infoShow" :rules="rules" ref="infoShow" label-width="80px">
                    <el-form-item label="站点编号">
                        <el-input v-model="IDformatter" :disabled="true"></el-input>
                    </el-form-item>
                    <el-form-item label="站点名称" prop="staName">
                        <el-input v-model="infoShow.staName"></el-input>
                    </el-form-item>
                    <el-form-item label="站点地址" prop="staAddress">
                        <el-input @blur="getLnglat" v-model="infoShow.staAddress"></el-input>
                    </el-form-item>
                    <el-row>
                        <el-col :span="12">
                            <el-form-item class="lnglat" label="东经">
                                <el-input v-model="infoShow.staLng" :disabled="true"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item class="lnglat" label="北纬">
                                <el-input v-model="infoShow.staLat" :disabled="true"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="创建时间">
                        <el-input v-model="TimeFormatter" :disabled="true"></el-input>
                    </el-form-item>
                    <el-form-item label="站点备注">
                        <el-input type="textarea" :rows="3" v-model="infoShow.remarks"></el-input>    
                    </el-form-item>
                    <el-form-item>
                        <el-button type="warning" plain style="margin-right: 10%" @click="updateStation('infoShow', infoShow)">完成更新</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
import newStation from './newStation.vue'
import moment from 'moment'
import axios from 'axios'
import { MessageBox } from 'element-ui';

const ERROR = 404;
const SUCCESS = 200;
const EXPIRE = 702;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

// const jwt = localStorage.getItem('jwt');
// axios.defaults.headers.common['Authorization'] = jwt;

export default {
    name:'stations',
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
            infoShow: {
                staName: '',
                staAddress: '',
                pointID: '',
                staLng: '',
                staLat: ''
            },
            backInfo: '', // 备份数据
            stopName: '',
            rules: {
                staName: [
                    { validator: checkVal, trigger: 'blur' }
                ],
                staAddress: [
                    { validator: checkVal, trigger: 'blur' }
                ]
            }
        }
    },
    components:{
        newStation
    },
    mounted() {
        this.$bus.$on("changeChoice", this.changeChoice);
        this.$bus.$on("loadTable", this.loadTable);
        this.$bus.$on("getLocation", this.getLocation);

        setTimeout(this.loadTable(), 10);
    },
    methods: {
        changeChoice(tag) {
            this.choice = tag;
        },
        loadTable() { // 加载表格
            axios.get(`${BASE}/station`, 
                {
                    params: {
                        cityID: JSON.parse(sessionStorage.getItem('city')).id
                    },
                    headers: {
                        Authorization: localStorage.getItem('jwt')
                    }
                }
            ).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.tableData = res.data.data;
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
        handleCurrentChange(currentRow) { // 当选中的行发生变更时,将当前行的地址信息(经纬度)在地图上展示
            let Lnglat = [currentRow.staLng, currentRow.staLat];
            this.$bus.$emit("getCenter", Lnglat);
        },
        handleEdit(index, stationInfo) { // 对站点编辑事件进行处理
            this.$bus.$emit("setChoice", 1); // 向地图组件表明自己想要设置绘点即可，以及获取具体位置信息
            this.infoShow = stationInfo;
            this.stopName = stationInfo.staName;
            this.isEdit = true;
        },
        handleDelete(index, stationInfo) { // 删除选中的站点
            MessageBox.confirm("是否确定要删除该站点？", "提示信息",  {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
            }).then(() => {
                let args = {
                    staID: stationInfo.staID,
                    pointID: stationInfo.pointID
                }
                // 先判断当前站点是否和线路进行绑定,如果有,则拒绝删除
                // 这里先不做,后面补上！！！
                // 20250222 没事数据库里做了

                // 不存在绑定关系,则直接删除
                axios.delete(`${BASE}/station`, {
                    params: args,
                    headers: {
                        Authorization: localStorage.getItem('jwt')
                    }
                })
                .then(
                    res => {
                        console.log(args);
                        if(res.data.code === SUCCESS) {
                            MessageBox.alert("站点删除成功！", "提示信息");
                            this.loadTable(); // 重新加载表单数据
                            // this.stationInfo = {};
                        }
                        else if(res.data.code === EXPIRE) {
                            MessageBox.alert("用户登录已过期！", "提示信息");
                            localStorage.clear();
                            sessionStorage.clear();
                            this.$router.replace('/login');
                        }
                        else {
                            MessageBox.alert(`站点删除操作失败！\n 请检查该站点是否绑定线路！ \n错误信息为:${res.data.message}`, "提示信息");
                        } 
                    }
                )
                .catch(
                    error => {
                        MessageBox.alert(`站点删除操作失败！\n错误信息为:${error}`, "提示信息");
                    }
                )
                
                // axios.post(`${BASE}/getPathbyStation`, args)
                // .then(
                //     res => {
                //         if(res.data.status === SUCCESS) {
                //             let paths = res.data.msg;
                //             if(paths.length === 0) { // 没有和当前站点关联的公交路线直接删除即可
                //                 axios.post(`${BASE}/delStation`, args)
                //                 .then(
                //                     res => {
                //                         if(res.data.status === SUCCESS) {
                //                             MessageBox.alert("站点删除成功！", "提示信息");
                //                             this.loadTable(); // 重新加载表单数据
                //                         }
                //                         else {
                //                             MessageBox.alert(`站点删除操作失败！\n错误信息为:${res.data.error}`, "提示信息");
                //                         }
                //                     }
                //                 )
                //             }
                //             else { // 否则,拒绝删除
                //                 let errMsg = "";
                //                 for(let i = 0; i < paths.length; i ++) {
                //                     errMsg += paths[i].busNum;
                //                     if(i != paths.length - 1) errMsg += '、';
                //                 }
                //                 MessageBox.alert(`因为存在和当前站点相关联的路线：${errMsg}，删除操作失败！请将站点解绑后重试！`, "提示信息");    
                //             }
                //         }
                //         else {
                //             MessageBox.alert(`站点删除操作失败！\n错误信息为:${res.data.error}`, "提示信息");
                //         }
                //     }
                // )
                // .catch(
                //     error => {
                //         MessageBox.alert(`站点删除操作失败！\n错误信息为:${error}`, "提示信息");
                //     }
                // )
            })
        },
        goBack() { // 退出站点编辑界面
            MessageBox.confirm("您还有未编辑完成的站点信息,是否退出？", "提示信息", {
                confirmButtonText: '确定',
                cancelButtonText: '取消'
            }).then(
                () => {
                    this.isEdit = false; // 重置为非编辑状态
                    this.loadTable(); // 重载表单
                }
            )
        },
        updateStation(formName, infoShow) { // 提交更新信息
            this.$refs[formName].validate((valid) => {
                if (valid) { // 如果通过检验,更新数据库
                    MessageBox.confirm("是否更新公交站点的信息", "提示信息", {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                    }).then(() => {
                        // console.log(this.tableData);
                        let args = {
                            staID: infoShow.staID,
                            staName: infoShow.staName,
                            staAddress: infoShow.staAddress,
                            pointID: infoShow.pointID,
                            staLng: infoShow.staLng,
                            staLat: infoShow.staLat,
                            remarks: infoShow.remarks,
                            editTime: moment(new Date()).format('YYYY-MM-DD HH:mm:ss'),
                            cityID: JSON.parse(sessionStorage.getItem('city')).id
                        };
                        axios.patch(`${BASE}/station`, args, {
                            headers: {
                                Authorization: localStorage.getItem('jwt')
                            }
                        }).then(
                            res => {
                                if(res.data.code === SUCCESS) {
                                    MessageBox.alert("更新公交站点成功！", "提示信息");
                                    this.isEdit = false; // 重置为非编辑状态
                                    this.loadTable(); // 重载表单
                                }
                                else if(res.data.code === EXPIRE) {
                                    MessageBox.alert("用户登录已过期！", "提示信息");
                                    localStorage.clear();
                                    sessionStorage.clear();
                                    this.$router.replace('/login');
                                }
                                else {
                                    MessageBox.alert(`更新公交站点失败！\n错误信息为:${res.data.error}`, "提示信息");
                                }
                            }
                        ).catch(error => {
                            MessageBox.alert(`更新公交站点失败！\n错误信息为:${error}`, "提示信息");
                        })
                    })
                }
                else {
                    MessageBox.alert(`请详尽您的填充信息！`, "提示信息");
                }
            });
        },
        getLnglat() {
            if(this.infoShow.staAddress !== "") {
                AMap.plugin("AMap.Geocoder", () => {
                    new AMap.Geocoder({ 
                        city: JSON.parse(sessionStorage.getItem('city')).name
                    }).getLocation(   
                        this.infoShow.staAddress, (status, result) => {
                            console.log(this.infoShow);
                            if(status === "complete" && result.info === 'OK') {
                                let lng = result.geocodes[0].location.lng;
                                let lat = result.geocodes[0].location.lat;
                                this.infoShow.staLng = lng;
                                this.infoShow.staLat = lat;
                                // 向地图组件传递信息,使得其重新设置中心点
                                this.$bus.$emit("getCenter", [lng, lat]); 
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
                this.infoShow.staLng = "";
                this.infoShow.staLat = "";
            }
        },
        getLocation(location) { // 根据用户在地图上点击的位置获取地址信息(包含经纬度,从地图组件传送过来)
            this.infoShow.staAddress = location.address;
            this.infoShow.staLng = location.lng;
            this.infoShow.staLat = location.lat;
        }
    },
    beforeDestroy() {
        this.$bus.$off("changeChoice");
        this.$bus.$off("loadTable");
        this.$bus.$off("getLocation");
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
    #stations {
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
        height: 530px;
        padding: 3px;
        margin-top:-23px;
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