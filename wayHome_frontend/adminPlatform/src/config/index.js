/**
 * 全局配置
 * 统一维护后端接口地址，避免在每个组件中硬编码。
 */
const config = {
  // 后端服务基础地址
  baseUrl: process.env.VUE_APP_BASE_URL || 'http://localhost:3000',
  // 成功状态码
  successCode: 200,
  // 登录过期状态码
  expireCode: 702
}

export default config
