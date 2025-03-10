<template>
    <div>
        <scheduleLeft style="float: left;" class="schArea"></scheduleLeft>
        <scheduleRight style="float: right;" class="schArea"></scheduleRight>
    </div>
</template>

<script>
import scheduleLeft from './scheduleLeft.vue'
import scheduleRight from './scheduleRight.vue'

export default {
    name: "schedule",
    components: {
        scheduleLeft,
        scheduleRight
    }
}
</script>

<style scoped>
    .schArea {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
        border-radius: 8px; 
        width: 45%;
        height: 460px;
        padding: 20px;
        overflow-y:auto;
        overflow-x:auto; 
    }
</style>