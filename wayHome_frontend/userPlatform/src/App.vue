<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
    margin: 0;
  }
  body {
    height: 100%;
  }
  .amap-logo{
    display: none;
    opacity:0 !important;
  }
  .amap-copyright {
      opacity:0;
  }
  #app {
    height: 607px;
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    /* margin-top: -40px; */
  }
</style>
