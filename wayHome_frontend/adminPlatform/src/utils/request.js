/**
 * 统一 Axios 请求实例
 * 自动携带 JWT、统一处理响应与错误。
 */
import axios from 'axios'
import { MessageBox } from 'element-ui'
import config from '../config'

const request = axios.create({
  baseURL: config.baseUrl,
  timeout: 30000
})

// 请求拦截器：自动附加 JWT 令牌
request.interceptors.request.use(
  (req) => {
    const token = localStorage.getItem('jwt')
    if (token) {
      req.headers.Authorization = token
    }
    return req
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理过期与错误
request.interceptors.response.use(
  (res) => {
    if (res.data && res.data.code === config.expireCode) {
      MessageBox.alert('用户登录已过期！', '提示信息')
      localStorage.clear()
      sessionStorage.clear()
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }
    return res
  },
  (error) => {
    MessageBox.alert(`请求失败！\n错误信息为:${error.message}`, '提示信息')
    return Promise.reject(error)
  }
)

export default request
