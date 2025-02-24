<template>
    <v-scale-screen style="background-color: darkorange;">
        <div id="back">
            <div id="login">
                <br> <br> <br>
                <div id="lert"> 
                    <i class="el-icon-s-tools"></i>
                    <span> 公交规划管理平台（管理员）</span>
                </div>
                <el-form ref="form" :model="loginForm" label-width="100px" id="loginForm" @submit.native.prevent>
                    <el-form-item label="账号">
                        <el-input maxlength="20" v-model.trim="loginForm.account" placeholder="请输入账号"></el-input>
                    </el-form-item>
                    <br>
                    <el-form-item label="密码">
                        <el-input maxlength="20" v-model.trim="loginForm.password" placeholder="请输入密码" show-password></el-input>
                    </el-form-item>
                    <br><br>
                    <el-button id="DAButton" @click="login" type="warning" :disabled="loginForm.account==='' || loginForm.password===''">登录</el-button>
                </el-form>
            </div>
        </div>
    </v-scale-screen>
</template>

<script>
import axios from 'axios'
import { MessageBox } from 'element-ui'
const SUCCESS = 200;
const FAILURE = 501;
// const BASE = "http://49.235.138.213:3000";
const BASE = "http://localhost:3000";

const crypto = require('crypto');  //引入crypto模块

export default {
    name: 'login',
    data() {
        return {
            loginForm: {
                account: '',
                password: ''
            }
        }
    },
    methods:{
        async login() {
            const md5 = crypto.createHash('md5'); 
            let digest = await md5.update(this.loginForm.password, 'utf8').digest('hex');
            // console.log(digest);

            // let args = {
            //     account: this.loginForm.account,
            //     password: digest,
            //     role: 'admin'
            // }

            let args = {
                account: this.loginForm.account,
                password: this.loginForm.password,
                // role: 'admin'
            }
            await axios.post(`${BASE}/admin`, args).then(
                res => {
                    if(res.data.code === SUCCESS) {
                        localStorage.setItem('jwt', res.data.data);                        
                        this.$router.replace({
                            name: 'home',
                            params: {
                                jwt: res.data.data
                            }
                        })
                    }
                    else if(res.data.code === FAILURE) {
                        MessageBox.alert("账号或者密码存在错误！", "提示信息", {
                            distinguishCancelAndClose: true,
                        })
                    }
                    else {
                        MessageBox.alert(res.data.error, "出现错误", {
                            distinguishCancelAndClose: true,
                        })
                    }
                }
            ).catch(error => {
                    MessageBox.alert(error, "出现错误", {
                        distinguishCancelAndClose: true,
                    })
                }
            )
        }
    }
}
</script>

<style scoped>
    #lert{
        font-size: 30px;
        font-weight: bold;
        font-family: "fantasy";
        color: wheat
    }

    #back {
        margin-top: 35px;
        background: url(../assets/wallhaven-wey8zx.jpg);
        width: 100%;
        height: 92%;
    }

    #login {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 20px; 
        background: rgb(242, 244, 245, 0.20);
        backdrop-filter: blur(15px);
        /* background: white; */
        position: relative;
        width: 40%;
        height: 50%;
        left: 30%;
        top: 15%;
        display: flex;
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
        color:antiquewhite;
        font-size: 25px;
    }

    .el-form {
        margin-top: 60px;
    }

    .el-input {
        width: 590px;
    }

    .el-input >>> .el-input__inner {
        font-size: 23px;
        font-weight: 500;
        padding: 0 6px;
        height: 55px;
        /* letter-spacing: 5px; */
    }

    #DAButton {
        width: 90%;
        font-size: 25px
    }
</style>