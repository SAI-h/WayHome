<template>
   <div class="drawStation" 
        v-loading="addressFindLoading" element-loading-text="请等待，正在获取当前地理位置信息..." 
   >
      <div id="draw-station-container"></div>
    </div>
</template>

<script>
import { MessageBox } from 'element-ui'
import AMapLoader from '@amap/amap-jsapi-loader'
// 设置安全密钥
window._AMapSecurityConfig = {
    securityJsCode: 'e22cf69805cbd7f769fc1fbbc73eb1d5'
}
export default {
    name: 'MapContainer',
    // 直接存储于SessionStorage即可获取
    // props: ['city'], // 由父组件传递而来，由管理员管辖的城市确定
    data() {
        return {
            addressFindLoading: false, // 点击地图获取地理信息的loading
            choice: 0,
            map: null, // 地图实例对象
            mkerlist: [], // maker(标记)
            path: [], // 绘图路径点
            polyline: [] // 绘图路径线
        }
    },
    mounted() {
        this.initMap(); // 初始化地图

        // 绑定全局事件
        this.$bus.$on("getCenter", this.getCenter);
        this.$bus.$on("drawPath", this.drawPath);
        this.$bus.$on("setChoice", this.setChoice);
    },
    beforeDestroy() {
        // 解绑全局事件
        this.$bus.$off("getCenter");
        this.$bus.$off("drawPath");
        this.$bus.$off("setChoice");
    },
    methods: {
        initMap() { // 初始化地图
             // 如果传输过来的数据不为空,可以更新城市sessionStorage值(防止刷新后城市信息消失)
            // if(this.city.name !== '') 
            //     sessionStorage.setItem('city', JSON.stringify(this.city));
            AMapLoader.load({
                key: '05894a2741ccb3765ca597c3b2e908f0',
                version: '2.0', 
                plugins: ['AMap.Scale', 'AMap.PlaceSearch', 'AMap.AutoComplete'] // 插件
            }).then(AMap => {
                this.map = new AMap.Map("draw-station-container", {
                    viewMode: '2D', // 是否为2D地图模式
                    zoom: 10, // 初始化地图级别
                    center: JSON.parse(sessionStorage.getItem('city')).lngLat // 初始化地图中心点位置
                });
                this.mkerlist.push(new AMap.Marker({
                    position: JSON.parse(sessionStorage.getItem('city')).lngLat,
                    title: JSON.parse(sessionStorage.getItem('city')).name
                }))
                this.map.add(this.mkerlist);
                // 给地图绑定点击事件
                this.map.on('click', this.clickMap);
            }).catch(error => {
                console.log(error);
                MessageBox.alert(error, "地图加载错误信息", {
                    distinguishCancelAndClose: true,
                });
            })
        },
        setChoice(choice) {
            this.choice = choice;
        }, 
        clickMap(point) { // 地图点击事件
            if(this.choice === 1) this.addressFindLoading = true;
            // 从地图上逐个挪除点标记,并奖mkerlist置为空
            for(let mker of this.mkerlist) this.map.remove(mker); 
            this.mkerlist = [];
            // 移除所有轨迹
            this.map.remove(this.polyline);
            this.path = [];

            // 获取点击点的经纬度位置
            let curlngLat = [point.lnglat.getLng(), point.lnglat.getLat()]; 

            // 将当前点的mker存入mkerlist数组，然后放入map中
            this.mkerlist.push(new AMap.Marker({
                position: curlngLat,
                title: JSON.parse(sessionStorage.getItem('city')).name
            }));
            this.map.add(this.mkerlist);

            /*----以下为组件之间的传值处理---*/

            // 根据经纬度信息获取地址信息
            let location = {
                address: '',
                lng: curlngLat[0],
                lat: curlngLat[1]
            }
            if(this.choice === 0) this.$bus.$emit("getLocation_2", location);
            else {
                this.map.plugin(['AMap.Geocoder'], () => {
                    new AMap.Geocoder({ 
                        city: JSON.parse(sessionStorage.getItem('city')).name
                    }).getAddress(
                        curlngLat, (status, result) => {
                            if(status === "complete" && result.info === "OK") {
                                location.address = result.regeocode.formattedAddress;
                                // 向兄弟组件"newStation/newPath"传值(当前位置的经纬度信息)
                                this.$bus.$emit("getLocation", location);
                                this.addressFindLoading = false;
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
        },
        getCenter(LngLat) { // 根据经纬度信息重绘地图中心点
            for(let mker of this.mkerlist) {
                this.map.remove(mker);
            }
            this.mkerlist = [];
            this.map.remove(this.polyline);
            this.path = [];

            this.map.setCenter(LngLat);
            this.mkerlist.push(
                new AMap.Marker({
                    position: LngLat,
                    title: JSON.parse(sessionStorage.getItem('city')).name
                })
            )
            this.map.add(this.mkerlist);
        },
        drawPath(points) { // 绘制公交路线
            // 移除maker
            for(let mker of this.mkerlist) {
                this.map.remove(mker);
            }
            this.mkerlist = [];
            // 移除所有轨迹
            this.map.remove(this.polyline);
            this.path = [];

            // 从points中筛选出站点和路径上的点
            for(let point of points) {
                if(point.isStation === true) { // 是站点
                    this.mkerlist.push(
                        new AMap.Marker({
                            position: point.LngLat,
                            title: point.title
                        })
                    )
                }
                this.path.push(new AMap.LngLat(point.LngLat[0], point.LngLat[1]));
            }
            if(this.path.length !== 0) {
                this.polyline = new AMap.Polyline({
                    path: this.path,  
                    borderWeight: 1, // 线条宽度，默认为 1
                    strokeColor: 'blue', // 线条颜色
                    lineJoin: 'round' // 折线拐点连接处样式
                });
                this.map.add(this.polyline);
            }
            this.map.add(this.mkerlist);
        }
    }
}
</script>

<style>
    .drawStation {
        vertical-align: baseline;
        /* float: inline-end; */
        /* display:inline-block; */
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 8px; 
        width: 46%;
        height: 95%;
        margin: auto;
        margin-left: 54%;
    }
    
    .drawStation #draw-station-container {
        border-radius: 20px; 
        padding: 0px;
        /* margin: 20px 0 20px 0; */
        margin: 20px auto;
        width: 100%;
        height: 100%;
        /* flex: right; */
    }
    
    .el-input {
        width: 150px;
        margin: 10px 0 0 10px;
        z-index: 5;
    }
</style>