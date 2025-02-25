// const port = 8080; // 设置端口
module.exports = {
    // devServer: {
    //     host: '49.235.138.213'
    // },
    devServer: {
      port: 8081, // 端口号
    },
    publicPath: './',
    outputDir: 'dist', // build时构建文件的目录 构建时传入 --no-clean 可关闭该行为
    assetsDir: 'assets', // build时放置生成的静态资源 (js、css、img、fonts) 的 (相对于 outputDir 的) 目录
    filenameHashing: true, // 默认在生成的静态资源文件名中包含hash以控制缓存
    runtimeCompiler: false, // 是否使用包含运行时编译器的 Vue 构建版本
    productionSourceMap: true, // 如果你不需要生产环境的 source map，可以将其设置为 false 以加速生产环境构建
    lintOnSave: false // 关闭eslint检查
}