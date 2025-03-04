package com.example.wayhome.controller;

import com.example.wayhome.dto.PathCalRequest;
import com.example.wayhome.utils.Result;
import com.example.wayhome.utils.ResultCodeEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calPath")
public class PathCalculateController {

//    @Autowired
//    private PathCalculateService pathCalculateService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public Result<?> PathCal(@NotNull @RequestParam("startStaID") Long startStaID,
                          @NotNull @RequestParam("endStaID") Long endStaID,
                          @NotNull @RequestParam("searchTime")
                            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") LocalDateTime searchTime,
                          @RequestParam(value = "scheme", defaultValue = "minTimeCost") String scheme,
                          @NotNull @RequestParam(value = "cityID") Integer cityID
                        ) {
        // 生成 taskID
        String taskID = UUID.randomUUID().toString();

        // 构造任务消息
        PathCalRequest pathCalRequest = new PathCalRequest(taskID, startStaID, endStaID, searchTime, scheme, cityID);

        // 确定 Routing Key
        String routingKey = "path.cal.minTime"; // 默认最短时间计算
        if ("minTransfer".equals(scheme)) {
            routingKey = "path.cal.minTransfer"; // 如果是最少换乘
        }


        // 发送任务到 RabbitMQ
        rabbitTemplate.convertAndSend("path.cal.exchange", routingKey, pathCalRequest);

        // 立即返回 taskId，前端可轮询查询结果
        return Result.build(taskID, ResultCodeEnum.TASK_RECEIVE);
    }

    @GetMapping("/result")
    public Result<?> getPathCalResult(@NotNull @RequestParam("taskID") Object taskID) {
        Object result = redisTemplate.opsForValue().get("path_cal_result:" + taskID);
        if (result == null) {
            return Result.build(null, ResultCodeEnum.PROCESSING);
        }
        return Result.ok(result);
    }


}
