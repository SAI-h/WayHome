<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-title">
        <i class="el-icon-s-tools"></i>
        <span>公交规划管理平台</span>
      </div>
      <p class="login-subtitle">WayHome · 管理员入口</p>

      <el-form
        ref="loginForm"
        :model="loginForm"
        label-position="top"
        class="login-form"
        @submit.native.prevent
      >
        <el-form-item label="账号">
          <el-input
            v-model.trim="loginForm.account"
            maxlength="20"
            placeholder="请输入账号"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model.trim="loginForm.password"
            maxlength="20"
            placeholder="请输入密码"
            show-password
            prefix-icon="el-icon-lock"
          />
        </el-form-item>
        <el-button
          class="login-button"
          type="warning"
          :loading="loading"
          :disabled="!canSubmit"
          @click="login"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
import request from '../utils/request'
import { MessageBox } from 'element-ui'
import config from '../config'

export default {
  name: 'login',
  data() {
    return {
      loading: false,
      loginForm: {
        account: '',
        password: ''
      }
    }
  },
  computed: {
    canSubmit() {
      return this.loginForm.account !== '' && this.loginForm.password !== ''
    }
  },
  methods: {
    async login() {
      this.loading = true
      try {
        const res = await request.post('/admin', {
          account: this.loginForm.account,
          password: this.loginForm.password
        })
        if (res.data.code === config.successCode) {
          localStorage.setItem('jwt', res.data.data)
          this.$router.replace({
            name: 'home',
            params: { jwt: res.data.data }
          })
        } else {
          MessageBox.alert('账号或者密码存在错误！', '提示信息')
        }
      } catch (error) {
        MessageBox.alert(`登录失败！\n${error.message}`, '提示信息')
      } finally {
        this.loading = false
      }
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
  background: linear-gradient(135deg, #f5a623 0%, #f7c948 40%, #ff8c42 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(10px);
}

.login-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #e8890c;
}

.login-subtitle {
  margin: 8px 0 28px;
  color: #999;
  font-size: 14px;
  letter-spacing: 1px;
}

.login-form >>> .el-form-item__label {
  font-weight: 600;
  color: #555;
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
