/**
 * 统一 Axios 请求实例
 * 统一处理响应与错误。
 */
import axios from 'axios'
import { MessageBox } from 'element-ui'
import config from '../config'

const request = axios.create({
  baseURL: config.baseUrl,
  timeout: 30000
})

request.interceptors.response.use(
  (res) => res,
  (error) => {
    MessageBox.alert(`请求失败！\n错误信息为:${error.message}`, '提示信息')
    return Promise.reject(error)
  }
)

export default request
