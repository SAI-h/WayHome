<template>
  <el-container class="home-container">
    <el-aside width="220px" class="home-aside">
      <div class="logo-area">
        <img src="../assets/busSystem.png" alt="WayHome" />
        <span>公交规划</span>
      </div>
      <el-menu
        default-active="1-1"
        :default-openeds="['1']"
        background-color="#2b3a4a"
        text-color="#e8eef3"
        active-text-color="#ffb400"
        @select="handleSelect"
      >
        <el-submenu index="1">
          <template slot="title">
            <i class="el-icon-guide"></i>
            <span>站点 / 线路</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="1-1">站点设置</el-menu-item>
            <el-menu-item index="1-2">线路设置</el-menu-item>
            <el-menu-item index="1-3">班次规划</el-menu-item>
          </el-menu-item-group>
        </el-submenu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="home-header">
        <span class="header-title">公交规划管理平台</span>
        <el-button
          type="warning"
          size="small"
          icon="el-icon-switch-button"
          @click="turnOff"
        >
          退出系统
        </el-button>
      </el-header>

      <el-main class="home-main">
        <MapContainer v-if="selection === '1-1' || selection === '1-2'" />
        <Stations v-if="selection === '1-1'" />
        <BusRoutes v-else-if="selection === '1-2'" />
        <Schedule v-else-if="selection === '1-3'" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { MessageBox } from 'element-ui'
import MapContainer from './MapContainer.vue'
import Stations from './stations.vue'
import BusRoutes from './busRoutes.vue'
import Schedule from './schedule.vue'

export default {
  name: 'home',
  components: {
    MapContainer,
    Stations,
    BusRoutes,
    Schedule
  },
  data() {
    return {
      selection: '1-1',
      city: {
        id: '',
        name: '',
        lngLat: []
      }
    }
  },
  mounted() {
    this.initCity()
  },
  methods: {
    initCity() {
      const jwt = this.$route.params.jwt
      if (!jwt) {
        MessageBox.alert('抱歉，请先进行登录！', '提示信息')
        this.$router.replace('/login')
        return
      }

      const body = this.parseJwt(jwt)
      if (sessionStorage.getItem('city') === null) {
        this.city = {
          id: body.cityID,
          name: body.cityName,
          lngLat: [body.cityLng, body.cityLat]
        }
        if (this.city.name) {
          sessionStorage.setItem('city', JSON.stringify(this.city))
        }
      }
    },
    parseJwt(token) {
      const base64Url = token.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
      return JSON.parse(jsonPayload)
    },
    handleSelect(index) {
      this.selection = index
    },
    turnOff() {
      MessageBox.confirm('确认退出系统吗？', '退出系统', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.clear()
        sessionStorage.clear()
        this.$router.replace('/login')
      })
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.home-aside {
  background-color: #2b3a4a;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  color: #ffb400;
  font-size: 18px;
  font-weight: 700;
}

.logo-area img {
  height: 40px;
}

.home-aside .el-menu {
  border-right: none;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.home-main {
  background: #f5f7fa;
  padding: 20px;
}

@media (max-width: 768px) {
  .home-aside {
    width: 160px !important;
  }

  .header-title {
    font-size: 14px;
  }
}
</style>
