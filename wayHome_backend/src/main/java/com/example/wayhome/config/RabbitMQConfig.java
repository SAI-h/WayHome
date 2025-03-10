package com.example.wayhome.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1️⃣ 定义 Direct Exchange
    @Bean
    public DirectExchange pathCalExchange() {
        return new DirectExchange("path.cal.exchange");
    }

    // 2️⃣ 定义两个队列
    @Bean
    public Queue minTimeQueue() {
        return new Queue("minTime.queue", true);
    }

    @Bean
    public Queue minTransferQueue() {
        return new Queue("minTransfer.queue", true);
    }

    // 3️⃣ 绑定队列到交换机
    @Bean
    public Binding bindingMinTime(DirectExchange pathCalExchange, Queue minTimeQueue) {
        return BindingBuilder.bind(minTimeQueue).to(pathCalExchange).with("path.cal.minTime");
    }

    @Bean
    public Binding bindingMinTransfer(DirectExchange pathCalExchange, Queue minTransferQueue) {
        return BindingBuilder.bind(minTransferQueue).to(pathCalExchange).with("path.cal.minTransfer");
    }
}
