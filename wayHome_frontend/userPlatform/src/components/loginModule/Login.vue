<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-title">
        <i class="el-icon-position"></i>
        <span>便民公共交通平台</span>
      </div>
      <p class="login-subtitle">WayHome · 您的出行好帮手</p>

      <el-form label-position="top" @submit.native.prevent>
        <el-form-item label="城市检索">
          <el-select
            v-model="cityChoice"
            filterable
            placeholder="请选择 / 输入城市"
            class="city-select"
          >
            <el-option
              v-for="city in cityList"
              :key="city.cityID"
              :label="city.cityName"
              :value="city"
            />
          </el-select>
        </el-form-item>
        <el-button
          class="login-button"
          type="warning"
          :disabled="!cityChoice"
          @click="login"
        >
          即刻启程
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
import request from '../../utils/request'
import { MessageBox } from 'element-ui'
import config from '../../config'

export default {
  name: 'login',
  data() {
    return {
      cityList: [],
      cityChoice: null
    }
  },
  mounted() {
    this.getCityList()
  },
  methods: {
    async getCityList() {
      try {
        const res = await request.get('/city')
        if (res.data.code === config.successCode) {
          this.cityList = res.data.data
          this.cityChoice = this.cityList[0] || null
        } else {
          MessageBox.alert(`初始化失败！\n错误信息为:${res.data.message}`, '提示信息')
        }
      } catch (error) {
        MessageBox.alert(`初始化失败！\n错误信息为:${error.message}`, '提示信息')
      }
    },
    login() {
      if (!this.cityChoice) {
        MessageBox.alert('请先选择城市！', '提示信息')
        return
      }
      this.$router.replace({
        name: 'passengerHome',
        params: { city: this.cityChoice }
      })
    }
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #2b5876 0%, #4e4376 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 44px 36px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.24);
}

.login-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #2b5876;
}

.login-subtitle {
  margin: 8px 0 28px;
  color: #999;
  font-size: 14px;
  letter-spacing: 1px;
}

.city-select {
  width: 100%;
}

.login-button {
  width: 100%;
  margin-top: 8px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 8px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 20px;
  }

  .login-title {
    font-size: 20px;
  }
}
</style>
