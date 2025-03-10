package com.example.wayhome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//@EnableTransactionManagement
public class RedisTemplateConfig {

//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        return Jackson2ObjectMapperBuilder.json()
//                .modulesToInstall(new JavaTimeModule()) // 注册 JavaTimeModule
//                .build();

//    @Bean
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
//        StringRedisTemplate template = new StringRedisTemplate();
//        template.setConnectionFactory(connectionFactory);
////        template.setEnableTransactionSupport(true); // 启用事务支持
//        return template;
//    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value序列化（使用自定义ObjectMapper）
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
//        template.setEnableTransactionSupport(true);
        return template;
    }

}
