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
                        全长{{ Math.round(totalLength(index) / 1000) }}公里
                        (预计{{ Math.round(plan.distance / 60) }}分钟)
                    </span>
                    <span v-else  style="color: blue; font-size: 13px;">
                        推荐方案{{ index + 1 }}
                        全长{{ Math.round(totalLength(index) / 1000) }}公里
                        ({{ plan.distance === 0 ? '直达' : '换乘' + plan.distance + '次' }})
                    </span>
                </template>
                <div v-for="(stations, index) in plan.scheme" :key="index">
                    <h3 style="margin-left: 10px; margin-bottom: 20px; color: blueviolet;">步骤{{ index+1 }}: 乘坐线路{{ stations[0].busNum }}({{ stations[0].staName }}=>{{ stations[stations.length - 1].staName }})</h3>
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
const busSpeed = 6.3; // (m/s)
const infinity = Number.POSITIVE_INFINITY;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

// 优先队列的数据结构表示
class PriorityQueue {
    constructor() {
        this.heap = [];
    }

    size() {
        return this.heap.length;
    }

    isEmpty() {
        return this.heap.length === 0;
    }

    push(val) {
        this.heap.push(val);
        this.upAdjust(this.heap.length - 1);
    }

    pop() {
        const top = this.heap[0];
        if(this.heap.length === 1) {
            this.heap = [];
            return top;
        }
        else {
            this.heap[0] = this.heap[this.heap.length - 1];
            this.heap.pop();
            this.downAdjust(0);
            return top;
        }
    }

    peek() {
        return this.heap[0];
    }

    upAdjust(index) {
        let childIndex = index;
        let parentIndex = Math.floor((childIndex - 1) / 2);
        const temp = this.heap[childIndex];
        while (childIndex > 0 && temp.distance < this.heap[parentIndex].distance) {
            this.heap[childIndex] = this.heap[parentIndex];
            childIndex = parentIndex;
            parentIndex = Math.floor((childIndex - 1) / 2);
        }
        this.heap[childIndex] = temp;
    }

    downAdjust(index) {
        let parentIndex = index;
        let childIndex = parentIndex * 2 + 1;
        const temp = this.heap[parentIndex];
        while (childIndex < this.heap.length) {
            if(childIndex + 1 < this.heap.length && this.heap[childIndex + 1].distance < this.heap[childIndex].distance) {
                childIndex ++;
            }
            if (temp.distance <= this.heap[childIndex].distance) {
                break;
            }
            this.heap[parentIndex] = this.heap[childIndex];
            parentIndex = childIndex;
            childIndex = parentIndex * 2 + 1;
        }
        this.heap[parentIndex] = temp;
    }
}

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
        this.getAllBusRoutes();
    },
    methods: {
        handleChange(index){
            if(index !== '') {
                let routeDetial = [];
                let arr = this.showInfo[index].routeDetial;
                // console.log(arr);
                let avgLng = 0, avgLat = 0, count = 0;
                for(let part of arr) {
                    let cal = part.allPoints;
                    for(let point of cal) {
                        avgLng += point.pointLng;
                        avgLat += point.pointLat;
                        count ++;
                    }
                }
                avgLng /= count;
                avgLat /= count;
                this.$bus.$emit("getCenter", [avgLng, avgLat]);
                for(let i = 0; i < arr.length; i ++) {
                    routeDetial.push(arr[i].allPoints);
                }
                this.$bus.$emit("drawPaths", routeDetial);
            }
        },
        getStations() {// 获取所有的站点数据
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            };
            axios.post(`${BASE}/getStations`, args).then(
                res => {
                    if(res.data.status === SUCCESS) {
                        this.stations = res.data.msg;
                    }
                    else {
                        MessageBox.alert(`站点数据获取失败！\n错误信息为:${res.data.error}`, "提示信息");
                    }
                }
            ).catch(
                error => {
                    MessageBox.alert(`站点数据获取失败！\n错误信息为:${error}`, "提示信息");
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
        dijkstra(graph, start, end, pointNum) { // 最短路算法
            let dist = new Array(pointNum);
            let vis = new Array(pointNum);
            let pre = new Array(pointNum); // 记录前驱
            let minHeap = new PriorityQueue();
            for(let i = 0; i < pointNum; i ++) {
                dist[i] = infinity;
                vis[i] = false;
                pre[i] = -1;
            }
            dist[start] = 0;

            minHeap.push({ver: start, distance: dist[start]});
            while(minHeap.size()) {
                let top = minHeap.peek();
                minHeap.pop();

                let ver = top.ver, distance = top.distance;
                if(vis[ver]) continue;
                vis[ver] = true;
                // console.log(ver);
                for(let i = 0; i < graph[ver].length; i ++) {
                    let nxt = graph[ver][i];
                    if(dist[nxt.ver] > distance + nxt.cost) {
                        pre[nxt.ver] = ver;
                        dist[nxt.ver] = distance + nxt.cost;
                        minHeap.push({ver: nxt.ver, distance:dist[nxt.ver]});
                    }
                }
            }
            let scheme = [];
            let node = end;
            if(dist[end] != infinity) { // 有解
                do {
                    scheme.unshift(node);
                    node = pre[node];
                } while(node != start);
                scheme.unshift(start);
            }
            return {
                scheme: scheme,
                distance: dist[end]
            }
        },
        getNxtVer(path, staID) { // 寻找该路线上staID的下一个点及相关信息
            let stationDistance = [];
            let tag = false;
            let ans = null;
            let distance = 0;
            for(let i = 0; i < path.length; i ++) {
                // console.log(path[i]);
                if(tag) {
                    stationDistance.push(path[i]);
                    if(path[i].staID !== null) {
                        ans = path[i];
                        distance = Math.round(this.getTotalDistance(stationDistance));
                        break;
                    }
                }
                if(path[i].staID === staID) {
                    tag = true;
                    stationDistance.push(path[i]);
                }
            }
            return [ans, distance];
        },
        getMinTimeCost(startStation, endStation, serachTime) { // 寻找最短时间花费的路径
            // console.log(serachTime);
            let scheduleList = this.findRunBus(serachTime);
            let graph = {};

            // 获取在searchTime的时间节点下正在运行的公交路线
            let busNumSet = [];
            for(let i = 0; i < scheduleList.length; i ++) {
                if(busNumSet.indexOf(scheduleList[i].busNum) === -1) {
                    busNumSet.push(scheduleList[i].busNum);
                }
            }

            // 将公交路线上的点标记出来
            let points = []; // 点集(同一位置的点在不同的班次算作不同的点)
            for(let i = 0; i < busNumSet.length; i ++) {
                let path = this.graphInfo[busNumSet[i]].path;
                for(let j = 0; j < path.length; j ++) {
                    if(path[j].staID !== null) {
                        points.push({
                            staID: path[j].staID,
                            staName: path[j].staName,
                            pointLng: path[j].pointLng,
                            pointLat: path[j].pointLat,
                            busNum: busNumSet[i]
                        })
                    }
                }
            }
            
            // 建立点到索引的映射, JS对象是通过引用传递的，而不是通过值传递的
            // 查找一个值和原先建立对象的值一样的对象时，如果使用了不同的对象引用，那么Map将无法找到这个键，返回undefined
            // 为了解决这个问题,可以使用对象的唯一标识符作为键，例如对象的ID或者使用JSON.stringify()将对象转换成字符串作为键
            let kvMap = new Map();
            for(let i = 0; i < points.length; i ++) {
                kvMap.set(JSON.stringify({ 
                            staID: points[i].staID,
                            staName: points[i].staName,
                            busNum: points[i].busNum
                          }), i);
            }

            // 建立邻接表
            for(let i = 0; i < points.length; i ++) {
                graph[i] = [];
            }
            
            // 根据路线的方向建图
            for(let i = 0; i < scheduleList.length; i ++) {
                // 方向相同,建立正向边;否则建立反向边
                let path = this.graphInfo[scheduleList[i].busNum].path.slice();
                if(scheduleList[i].schedule.sStation !== path[0].staName) {
                    path = this.reverseArray(path);
                }
                let stationDistance = []; // 用于测算相邻两个站点之间距离的数组,存放两个相邻站点和站点之间的控制点
                for(let j = 0, cnt = 0; j < path.length; j ++) { // cnt用于记录站点是否成对
                    if(path[j].staID !== null) { // 是站点
                        stationDistance.push(path[j]);
                        cnt ++;
                        if(cnt == 2) { // cnt为2说明出现了成对的站点,在站点之间建边
                            // 建立由first指向second长度为distance的边
                            let first = {
                                staID: stationDistance[0].staID,
                                staName: stationDistance[0].staName,
                                busNum: scheduleList[i].busNum
                            }
                            let second = {
                                staID: stationDistance[stationDistance.length - 1].staID,
                                staName: stationDistance[stationDistance.length - 1].staName,
                                busNum: scheduleList[i].busNum
                            }
                            let distance = this.getTotalDistance(stationDistance);
                            let firstIndex = kvMap.get(JSON.stringify(first));
                            let secondIndex = kvMap.get(JSON.stringify(second));
                            // console.log(firstIndex, secondIndex, stationDistance);
                            // console.log("---------------");
                            // 加边
                            // if(!graph[firstIndex]) {
                            //     graph[firstIndex] = []
                            // }
                            graph[firstIndex].push({
                                ver: secondIndex,
                                cost: distance / busSpeed
                            })
                            // 重置
                            stationDistance = [path[j]];
                            cnt = 1;
                        }
                    }
                    else { // 是控制点
                        stationDistance.push(path[j]);
                    }
                }
            }
            // console.log(graph);
            
            // 同一位置站点换乘需要额外建边
            // 如A是一个站点,1号线路和2号线路均通过该站点,若要从1号线路换乘为2号线路则需要额外的换乘时间
            // 在建图上就表示为点{A,1}向{A,2}建立一条长度为2号线发车时间除以2的边
            // 但事先并没有对不同来向的站点进行再次区分,为避免路线方向带来的影响
            // 采用1号线{A,1}向2号线的A的该方向的下一个点{B,2},连接一条长为(2号线额外换乘时间+A到B站点运行时间的)的边
            
            // 首先处理出每个站点经过哪些路线
            // A: {busNum2, nxtVer, deInterval}
            // (A,busNum1) -> (nxtVer, busNum2)

            // console.log(scheduleList);
            // 判断每条线路是否存在正向方向/负向方向/均存在
            // 若存在正向方向,在对于每个需要换乘的站点需要建立正向方向上的边...
            let busDir = new Map();
            for(let i = 0; i < scheduleList.length; i ++) {
                let busNum = scheduleList[i].busNum;
                if(!busDir.has(busNum)) {
                    busDir.set(busNum, {
                        dir1_Length: -1, // 是否存在正向方向
                        dir2_Length: -1 // 是否存在负向方向
                    })
                }
                let path = this.graphInfo[busNum].path;
                if(scheduleList[i].schedule.sStation === path[0].staName) {
                    busDir.get(busNum).dir1_Length = scheduleList[i].schedule.deInterval; // 存在正向方向,存储班次间隔
                }
                else {
                    busDir.get(busNum).dir2_Length = scheduleList[i].schedule.deInterval; // 存在负向方向,存储班次间隔
                }
            }

            // console.log(scheduleList);
            let pToR = new Map();
            let pSave = new Map();
            for(let i = 0; i < points.length; i ++) {
                let obj = JSON.stringify({
                    staID: points[i].staID,
                    staName: points[i].staName,
                    // busNum: points[i].busNum
                });
                if(!pSave.has(obj)) {
                    pSave.set(obj, new Set());
                }
                pSave.get(obj).add(points[i].busNum);
                if(!pToR.has(obj)) {
                    pToR.set(obj, []);
                }
                let busNum = points[i].busNum;
                if(busDir.get(busNum).dir1_Length !== -1) { // 存在正向方向的边
                    let path = this.graphInfo[busNum].path;
                    if(path[path.length - 1].staID !== points[i].staID) {// 不是当前路线上的最后一个点
                        // 寻找该路线该方向上的下一个点以及相关信息
                        let save = this.getNxtVer(path, points[i].staID);
                        pToR.get(obj).push(
                            {
                                nxtVer: {
                                    staID: save[0].staID,
                                    staName: save[0].staName,
                                    busNum: busNum
                                },
                                cost: busDir.get(busNum).dir1_Length / 2 * 60 + save[1] / busSpeed
                            }
                        )
                    }
                }
                if(busDir.get(busNum).dir2_Length) {
                    let path = this.reverseArray(this.graphInfo[busNum].path);
                    if(path[path.length - 1].staID !== points[i].staID) {// 不是当前路线上的最后一个点
                        // 寻找该路线该方向上的下一个点以及相关信息
                        let save = this.getNxtVer(path, points[i].staID);
                        pToR.get(obj).push(
                            {
                                nxtVer: {
                                    staID: save[0].staID,
                                    staName: save[0].staName,
                                    busNum: busNum
                                },
                                cost: busDir.get(busNum).dir1_Length / 2 * 60 + save[1] / busSpeed
                            }
                        )
                    }
                }
            }

            // 同站换乘建边
            for(let [key, value] of pSave) {
                for(let busNum of value) {
                    let station = JSON.parse(key);
                    let first = {
                        staID: station.staID,
                        staName: station.staName,
                        busNum: busNum
                    }

                    for(let i = 0; i < pToR.get(key).length; i ++) {
                        let nxt = pToR.get(key)[i];
                        if(nxt.nxtVer.busNum != first.busNum) { // 不在同一班次上,需要换乘,建边
                            // console.log(first, nxt.nxtVer);
                            let firstIndex = kvMap.get(JSON.stringify(first));
                            let secondIndex = kvMap.get(JSON.stringify(nxt.nxtVer));
                            let distance = nxt.cost;
                            // 加边
                            // if(!graph[firstIndex]) {
                            //     graph[firstIndex] = []
                            // }
                            graph[firstIndex].push({
                                ver: secondIndex,
                                cost: distance
                            })
                        }
                    }
                }
            }

            // 最短路实现
            let startChoice = []; // 可能作为起点的点集
            let endChoice = []; // 可能作为终点的点集
            for(let j = 0; j < points.length; j ++) {
                if(startStation.staID === points[j].staID) {
                    startChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        busNum: points[j].busNum
                    });
                }
                else if(endStation.staID === points[j].staID) {
                    endChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        busNum: points[j].busNum
                    });
                }
            }

            this.showInfo = [];
            if(startChoice.length && endChoice.length){ // 有通路
                let ans = null;
                for(let i = 0; i < startChoice.length; i ++)
                    for(let j = 0; j < endChoice.length; j ++) {
                        let start = kvMap.get(JSON.stringify(startChoice[i]));
                        let end = kvMap.get(JSON.stringify(endChoice[j]));
                        let res = this.dijkstra(graph, start, end, points.length);
                        if(ans === null || ans.distance > res.distance) ans = res;
                    }
                let stations = [];
                for(let i = 0; i < ans.scheme.length; i ++) {
                    let j = ans.scheme[i];
                    stations.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        busNum: points[j].busNum
                    })
                }
                // console.log(ans);
                // 筛选
                let save = [];
                let plan = [];
                let routeDetial = [];
                for(let i = 0; i < stations.length; i ++) {
                    save.push(stations[i]);
                    if(i === stations.length - 1 || stations[i].busNum != stations[i + 1].busNum) { // 与下一站不属于同一条线路,当前站需要换乘
                        plan.push(save);
                        // console.log(333, save);
                        routeDetial.push(this.findAllPoints(save));
                        // 重置
                        if(i !== stations.length - 1) {
                            save = [{
                                staID: stations[i].staID,
                                staName: stations[i].staName,
                                busNum: stations[i + 1].busNum
                            }];
                        }
                    }
                }
                let couldSet = true;
                for(let p of plan) 
                    if(p.length <= 1) {
                        couldSet = false;
                    }
                if(couldSet) {
                    this.showInfo.push(
                        {
                            scheme: plan,
                            distance: ans.distance,
                            routeDetial: routeDetial
                        }
                    ); // showInfo中可能含有多个plan,每个plan可能需要多次换乘
                }
            }
            // console.log(111, this.showInfo);
        },
        getMinTransfer(startStation, endStation, serachTime) { // 寻找最少换乘次数的路径
            let scheduleList = this.findRunBus(serachTime);
            let graph = {};
            let busNumSet = [];
            for(let i = 0; i < scheduleList.length; i ++) {
                if(busNumSet.indexOf(scheduleList[i].busNum) === -1) {
                    busNumSet.push(scheduleList[i].busNum);
                }
            }
            let points = [];
            let pToR = new Map();
            for(let i = 0; i < busNumSet.length; i ++) {
                let path = this.graphInfo[busNumSet[i]].path;
                for(let j = 0; j < path.length; j ++) {
                    if(path[j].staID !== null) {
                        points.push({
                            staID: path[j].staID,
                            staName: path[j].staName,
                            busNum: busNumSet[i]
                        })
                        let obj = {
                            staID: path[j].staID,
                            staName: path[j].staName,
                        };
                        if(!pToR.has(JSON.stringify(obj))) {
                            pToR.set(JSON.stringify(obj), new Set());
                        }
                        pToR.get(JSON.stringify(obj)).add(busNumSet[i]);
                    }
                }
            }
            let kvMap = new Map();
            for(let i = 0; i < points.length; i ++) {
                kvMap.set(JSON.stringify(points[i]), i);
            }
            for(let i = 0; i < points.length; i ++) {
                graph[i] = [];
            }
            // console.log(pToR);
            // 建图
            for(let i = 0; i < scheduleList.length; i ++) {
                // 方向相同,建立正向边;否则建立反向边
                let path = this.graphInfo[scheduleList[i].busNum].path.slice();
                if(scheduleList[i].schedule.sStation !== path[0].staName) {
                    path = this.reverseArray(path);
                }
                let stationPII = [];
                for(let j = 0, cnt = 0; j < path.length; j ++) { // cnt用于记录站点是否成对
                    if(path[j].staID !== null) { // 是站点
                        stationPII.push(path[j]);
                        if(stationPII.length == 2) {
                            // 建立由first指向second长度为distance的边
                            let first = {
                                staID: stationPII[0].staID,
                                staName: stationPII[0].staName,
                                busNum: scheduleList[i].busNum
                            }
                            let second = {
                                staID: stationPII[1].staID,
                                staName: stationPII[1].staName,
                                busNum: scheduleList[i].busNum
                            }
                            let firstIndex = kvMap.get(JSON.stringify(first));
                            let secondIndex = kvMap.get(JSON.stringify(second));
                            graph[firstIndex].push({
                                ver: secondIndex,
                                cost: 0
                            })
                            // 重置
                            stationPII = [path[j]];
                        }
                    }
                }
            }
            // 换乘建边边权为1
            for(let [key, value] of pToR) {
                let station = JSON.parse(key);
                for(let busNum1 of value) {
                    for(let busNum2 of value) {
                        if(busNum1 === busNum2) continue;
                        let first = {
                            staID: station.staID,
                            staName: station.staName,
                            busNum: busNum1
                        }
                        let second = {
                            staID: station.staID,
                            staName: station.staName,
                            busNum: busNum2
                        }
                        let firstIndex = kvMap.get(JSON.stringify(first));
                        let secondIndex = kvMap.get(JSON.stringify(second));
                        graph[firstIndex].push({
                            ver: secondIndex,
                            cost: 1
                        })
                    }
                }
            }

            let startChoice = []; // 可能作为起点的点集
            let endChoice = []; // 可能作为终点的点集
            for(let j = 0; j < points.length; j ++) {
                if(startStation.staID === points[j].staID) {
                    startChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        busNum: points[j].busNum
                    });
                }
                else if(endStation.staID === points[j].staID) {
                    endChoice.push({
                        staID: points[j].staID,
                        staName: points[j].staName,
                        busNum: points[j].busNum
                    });
                }
            }

            this.showInfo = [];
            if(startChoice.length && endChoice.length) {
                let ans = null;
                let choice = []; // choice中存放了多个方案
                for(let i = 0; i < startChoice.length; i ++)
                    for(let j = 0; j < endChoice.length; j ++) {
                        let start = kvMap.get(JSON.stringify(startChoice[i]));
                        let end = kvMap.get(JSON.stringify(endChoice[j]));
                        let res = this.dijkstra(graph, start, end, points.length);
                        if(ans === null || ans.distance > res.distance) {
                            ans = res;
                            choice = [res];
                        }
                        else if(ans.distance === res.distance) {
                            choice.push(res);
                        }
                    }
                    console.log(choice);

                    // 将choice中的方案按线路进行划分
                    for(let i = 0; i < choice.length; i ++) {
                        let routeDetial = []; // 详细路径,包括控制点
                        let plan = choice[i].scheme;
                        let stations = []; // 部分路径
                        let path = []; // 整体路径
                        for(let j = 0; j < plan.length; j ++) {
                            stations.push({
                                staID: points[plan[j]].staID,
                                staName: points[plan[j]].staName,
                                busNum: points[plan[j]].busNum
                            });
                            if(j === plan.length - 1 || points[plan[j]].busNum != points[plan[j + 1]].busNum) { // 处于最末尾,或下一个站点和自己不是同一路线(结束/换乘)
                                path.push(stations);
                                routeDetial.push(this.findAllPoints(stations));
                                stations = [];
                            }
                        }
                        if(path.length) {
                            this.showInfo.push({
                                distance: choice[0].distance,
                                scheme: path,
                                routeDetial: routeDetial
                            });
                        }
                    }
                    // console.log(this.showInfo);
            }

        },
        findAllPoints(stations) { // 根据所给出站点,给出该公交路线上的这一段上的所有点(站点/控制点)信息
            let busNum = stations[0].busNum;
            let allPoints = [];
            let arr = this.graphInfo[busNum].path.slice();
            let tag = false;
            let toFind = true;
            // 暴力正向搜一遍,反向搜一遍
            for(let i = 0; i < arr.length; i ++) {
                // if(toFind && tag && arr[i].staID !== null && arr[i].staID === stations[1].staID) {
                //     toFind = false;
                // }
                // else {
                //     allPoints = [];
                //     break;
                // }
                if(arr[i].staID === stations[0].staID) tag = true;
                if(tag) allPoints.push(arr[i])
                if(arr[i].staID === stations[stations.length - 1].staID) break;
            }
            // console.log(111, allPoints);
            if(!allPoints.length) {
                allPoints = [];
                arr = this.reverseArray(arr);
                for(let i = 0; i < arr.length; i ++) {
                    if(arr[i].staID === stations[0].staID) tag = true;
                    if(tag) allPoints.push(arr[i])
                    if(arr[i].staID === stations[stations.length - 1].staID) break;
                }
            }
            // console.log(222, allPoints);
            return {
                allPoints: allPoints,
                distance: this.getTotalDistance(allPoints)
            }
        },
        onSubmit() {
            if(this.lenSet === false) {
                this.setTotalLen();
                this.lenSet = true;
            }
            this.getMinTimeCost(this.searchInfo.sStation, this.searchInfo.eStation, new Date(2023, 4, 12, 16, 0, 0));
            // this.findRunBus(new Date());
        },
        async getAllBusRoutes() { // (异步)获取所有的公交信息
            await axios.post(`${BASE}/getPaths`, {
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            })
            .then(
                res => {
                    if(res.data.status === SUCCESS) {
                        // 1.加载路径
                        let info = res.data.msg;
                        for(let item of info) { 
                            if(!this.graphInfo[item.busNum]) {
                                this.graphInfo[item.busNum] = {
                                    totalLen: 0,
                                    path: [],
                                    schedule: []
                                };
                            }
                            this.graphInfo[item.busNum].path.push({
                                pointLng: item.pointLng,
                                pointLat: item.pointLat,
                                staID: item.staID,
                                staName: item.staName
                            });
                        }
                    }
                    else {
                        MessageBox.alert("22");
                    }
                }
            )
            .catch(
                error => {
                    MessageBox.alert("33");
                }
            )

            // 2.获取路线的所有的公交班次
            let args = {
                cityID: JSON.parse(sessionStorage.getItem('city')).cityID
            }
            await axios.post(`${BASE}/getAllSchedule`, args)
            .then(
                res => {
                    if(res.data.status === SUCCESS) {
                        let arr = res.data.msg;
                        for(let i = 0; i < arr.length; i ++) {
                            this.graphInfo[arr[i].busNum].schedule.push({
                                routeID: arr[i].routeID,
                                sStation: arr[i].sStation,
                                eStation: arr[i].eStation,
                                startTime: arr[i].startTime,
                                endTime: arr[i].endTime,
                                deInterval: arr[i].deInterval,
                                workdays: arr[i].workdays
                            })
                        }
                    }
                }
            )
            .catch(
                error => {
                    MessageBox.alert("11");
                }
            )
        },
        findRunBus(time) { // 获取在当前时间下处于运行状态的公交班次
            let days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
            let day = days[time.getDay()];
            let hour = time.getHours();
            let minute = time.getMinutes();
            let second = time.getSeconds();
            let timeCode = hour * 3600 + minute * 60 + second;
            let scheduleList = [];
            for(const key in this.graphInfo) {
                if(this.graphInfo[key].schedule.length) {
                    // 检查星期 
                    let arr = this.graphInfo[key].schedule;
                    for(let i = 0; i < arr.length; i ++) {
                        let workdays = JSON.parse(arr[i].workdays).workdays;
                        if(workdays.indexOf(day) !== -1) { // 检查时间
                            let sT = {
                                hh: parseInt(arr[i].startTime.substr(0, 2)),
                                mm: parseInt(arr[i].startTime.substr(3, 2)),
                                ss: parseInt(arr[i].startTime.substr(6, 2)),
                            }
                            let eT = {
                                hh: parseInt(arr[i].endTime.substr(0, 2)),
                                mm: parseInt(arr[i].endTime.substr(3, 2)),
                                ss: parseInt(arr[i].endTime.substr(6, 2)),
                            }
                            let sTcode = sT.hh * 3600 + sT.mm * 60 + sT.ss;
                            let eTcode = eT.hh * 3600 + eT.mm * 60 + eT.ss + this.graphInfo[key].totalLen / busSpeed;
                            if(sTcode < timeCode && eTcode > timeCode) { // 在公交的运行范围内
                                scheduleList.push({
                                    busNum: key,
                                    schedule: arr[i]
                                })
                            }
                        }
                    }
                }
            }
            return scheduleList;
        },
        getDistance(pointA, pointB) { // 获取两点之间的距离
            let first = new AMap.LngLat(pointA.pointLng, pointA.pointLat);
            let second = new AMap.LngLat(pointB.pointLng, pointB.pointLat);
            return first.distance(second);
        },
        getTotalDistance(points) { // 获取整段路径的长度
            let res = 0;
            for(let i = 0; i < points.length - 1; i ++) res += this.getDistance(points[i], points[i + 1]);
            return res;
        },
        setTotalLen() {
            for(const key in this.graphInfo) {
                this.graphInfo[key].totalLen = Math.round(this.getTotalDistance(this.graphInfo[key].path));
            }
        },
        reverseArray(arr) { // 数组反转
            return arr.slice().reverse();
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
    computed: {
        totalLength() {
            return index => {
                let res = 0;
                let arr = this.showInfo[index].routeDetial;
                for(let i = 0; i < arr.length; i ++) res += arr[i].distance;
                return res;
            }
        }
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
                        this.getMinTimeCost(this.searchInfo.sStation, this.searchInfo.eStation, dateTime);
                    }
                    else {
                        this.getMinTransfer(this.searchInfo.sStation, this.searchInfo.eStation, dateTime);
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