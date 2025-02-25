<template>
    <!-- <v-scale-screen style="background-color: darkorange;"> -->
    <div id="back">
        <div id="login">
            <br> <br>
            <div id="lert">
                <i class="el-icon-position"></i>
                <span> 便民公共交通平台</span>
            </div>
            <el-form ref="form" :model="loginForm" label-width="80px" id="loginForm" @submit.native.prevent>
                <el-form-item label="城市检索">
                    <el-select v-model="this.loginForm.cityChoice" filterable placeholder="请选择/输入城市" @change="selectedItem">
                        <el-option
                            v-for="city in cityList"
                            :key="city.cityID"
                            :label="city.cityName"
                            :value="city">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-button class="DAButton" @click="login" type="warning" :disabled="this.loginForm.cityChoice==='' || this.loginForm.cityChoice === null">即刻启程</el-button>
            </el-form>
        </div>
    </div>
    <!-- </v-scale-screen> -->
</template>

<script>
import axios from 'axios'
import { MessageBox } from 'element-ui'
const SUCCESS = 200;
const FAILURE = 202;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

const crypto = require('crypto');  //引入crypto模块

export default {
    name: 'login',
    data() {
        return {
            cityList: [],
            loginForm: {
                // account: '',
                // password: ''
                cityChoice: null,
            },
            // loginForm: {
            //     account: '',
            //     password: ''
            // }
        }
    },
    mounted() {
        this.getCityList();
    },
    methods:{
        selectedItem() {
            this.$forceUpdate();
        },
        getCityList() {
            axios.get(`${BASE}/city`)
            .then(
                res => {
                    if(res.data.code === SUCCESS) {
                        this.cityList = res.data.data;
                        this.loginForm.cityChoice = this.cityList[0];
                    } else {
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
        async login() {

            this.$router.replace({
                name: 'passengerHome',
                params: {
                    city: this.loginForm.cityChoice
                }
            })

            // axios.get(`${BASE}/city`)
            // .then(
            //     res => {
            //         if(res.data.code === SUCCESS) {
            //             this.cityList = res.data.data;
            //         } else {
            //             MessageBox.alert(`初始化失败！\n错误信息为:${res.data.message}`, "提示信息");
            //         }
            //     }
            // )
            // .catch(
            //     error => {
            //         MessageBox.alert(`初始化失败！\n错误信息为:${error}`, "提示信息");
            //     }
            // )

        //     const md5 = crypto.createHash('md5'); 
        //     let digest = await md5.update(this.loginForm.password, 'utf8').digest('hex');

        //     let loginTag = false;
        //     await axios.post(`${BASE}/login`, {
        //         account: "Dri" + this.loginForm.account,
        //         password: digest,
        //         role: 'driver'
        //     })
        //     .then(
        //         res => {
        //             if(res.data.status === SUCCESS) {
        //                 loginTag = true;
        //                 this.$router.replace({
        //                     name: 'driverHome',
        //                     params: {
        //                         account: 'Dri' + this.loginForm.account,
        //                         role: 'driver'
        //                     }
        //                 })
        //             }
        //         }
        //     )
        //     .catch(
        //         error => {
        //             MessageBox.alert(`登录失败！\n错误信息为:${error}`, "提示信息");
        //         }
        //     )

        //    if(!loginTag) {
        //         await axios.post(`${BASE}/login`, {
        //             account:  this.loginForm.account,
        //             password: digest,
        //             role: 'user'
        //         })
        //         .then(
        //             res => {
        //                 if(res.data.status === SUCCESS) {
        //                     loginTag = true;
        //                     this.$router.replace({
        //                         name: 'passengerHome',
        //                         params: {
        //                             account: this.loginForm.account,
        //                             role: 'user'
        //                         }
        //                     })
        //                 }
        //             }
        //         )
        //         .catch(
        //             error => {
        //                 MessageBox.alert(`登录失败！\n错误信息为:${error}`, "提示信息");
        //             }
        //         )
        //    }

        //     if(!loginTag) {
        //         MessageBox.alert("登录失败！请检查账号密码是否正确！", "提示信息");
        //     }
        }
    }
}
</script>

<style scoped>
    #lert{
        font-size: 17px;
        font-weight: bold;
        color: rgb(89, 61, 9)
    }

    #back {
        background: url(../../assets/wallhaven-mdegzm.jpg) no-repeat center center fixed;
        width: 100%;
        height: 100%;
    }

    #login {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 20px; 
        background: rgb(242, 244, 245, 0.20);
        backdrop-filter: blur(15px);
        position: relative;
        width: 40%;
        height: 60%;
        left: 30%;
        top: 15%;
        /* display: flex; */
        flex-direction: column;
        border: 1px solid #fff;
        border-bottom: 1px solid rgba(255, 255, 255, 0.5);
        border-right: 1px solid rgba(255, 255, 255, 0.5);
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.1);
        /* gap: 30px; */
    }

    #loginForm >>> .el-form-item__label {
        font-weight: bold;
        font-family: "fantasy";
        color:rgb(80, 47, 4);
        font-size: 13px;
    }

    .el-form {
        margin-top: 30px;
    }

    .el-input {
        width: 350px;
    }

    .el-input >>> .el-input__inner {
        font-size: 13px;
        font-weight: 500;
    }

    .DAButton {
        width: 45%;
        font-size: 13px
    }
</style>