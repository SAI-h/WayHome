package com.example.wayhome.config;

import com.example.wayhome.constant.RabbitMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 交换机、队列与绑定关系配置。
 *
 * @author WayHome
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 定义路径规划直连交换机。
     *
     * @return 直连交换机
     */
    @Bean
    public DirectExchange pathCalExchange() {
        return new DirectExchange(RabbitMqConstant.PATH_CAL_EXCHANGE);
    }

    /**
     * 定义最短时间方案队列。
     *
     * @return 最短时间方案队列
     */
    @Bean
    public Queue minTimeQueue() {
        return new Queue(RabbitMqConstant.MIN_TIME_QUEUE, true);
    }

    /**
     * 定义最少换乘方案队列。
     *
     * @return 最少换乘方案队列
     */
    @Bean
    public Queue minTransferQueue() {
        return new Queue(RabbitMqConstant.MIN_TRANSFER_QUEUE, true);
    }

    /**
     * 绑定最短时间队列到交换机。
     *
     * @param pathCalExchange 交换机
     * @param minTimeQueue    最短时间队列
     * @return 绑定关系
     */
    @Bean
    public Binding bindingMinTime(DirectExchange pathCalExchange, Queue minTimeQueue) {
        return BindingBuilder.bind(minTimeQueue).to(pathCalExchange)
                .with(RabbitMqConstant.MIN_TIME_ROUTING_KEY);
    }

    /**
     * 绑定最少换乘队列到交换机。
     *
     * @param pathCalExchange   交换机
     * @param minTransferQueue  最少换乘队列
     * @return 绑定关系
     */
    @Bean
    public Binding bindingMinTransfer(DirectExchange pathCalExchange, Queue minTransferQueue) {
        return BindingBuilder.bind(minTransferQueue).to(pathCalExchange)
                .with(RabbitMqConstant.MIN_TRANSFER_ROUTING_KEY);
    }
}
