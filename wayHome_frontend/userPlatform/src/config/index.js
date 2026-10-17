/**
 * 全局配置
 * 统一维护后端接口地址，避免在每个组件中硬编码。
 */
const config = {
  baseUrl: process.env.VUE_APP_BASE_URL || 'http://localhost:3000',
  successCode: 200,
  expireCode: 702
}

export default config
