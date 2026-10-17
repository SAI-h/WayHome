package com.example.wayhome.constant;

/**
 * RabbitMQ 相关常量定义，统一维护交换机、队列与路由键，避免魔法值散落在代码中。
 *
 * @author WayHome
 */
public final class RabbitMqConstant {

    /** 路径规划任务交换机名称 */
    public static final String PATH_CAL_EXCHANGE = "path.cal.exchange";

    /** 最短时间方案队列名称 */
    public static final String MIN_TIME_QUEUE = "minTime.queue";

    /** 最少换乘方案队列名称 */
    public static final String MIN_TRANSFER_QUEUE = "minTransfer.queue";

    /** 最短时间方案路由键 */
    public static final String MIN_TIME_ROUTING_KEY = "path.cal.minTime";

    /** 最少换乘方案路由键 */
    public static final String MIN_TRANSFER_ROUTING_KEY = "path.cal.minTransfer";

    private RabbitMqConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
