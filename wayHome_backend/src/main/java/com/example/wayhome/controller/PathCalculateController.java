package com.example.wayhome.controller;

import com.example.wayhome.constant.RabbitMqConstant;
import com.example.wayhome.constant.RedisKeyConstant;
import com.example.wayhome.dto.PathCalRequest;
import com.example.wayhome.utils.Result;
import com.example.wayhome.utils.ResultCodeEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 路径规划接口，采用异步任务模式：提交任务后轮询查询结果。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/calPath")
public class PathCalculateController {

    /** 最短时间方案标识 */
    private static final String SCHEME_MIN_TIME = "minTimeCost";

    /** 最少换乘方案标识 */
    private static final String SCHEME_MIN_TRANSFER = "minTransfer";

    private final RabbitTemplate rabbitTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    public PathCalculateController(RabbitTemplate rabbitTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 提交路径规划任务。
     *
     * @param startStaId 起始站点 ID
     * @param endStaId   终点站点 ID
     * @param searchTime 出发时间
     * @param scheme     规划方案
     * @param cityId     城市 ID
     * @return 任务 ID
     */
    @GetMapping
    public Result<String> submitPathCalculation(@NotNull @RequestParam("startStaID") Long startStaId,
                                                @NotNull @RequestParam("endStaID") Long endStaId,
                                                @NotNull @RequestParam("searchTime")
                                                @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
                                                LocalDateTime searchTime,
                                                @RequestParam(value = "scheme", defaultValue = SCHEME_MIN_TIME) String scheme,
                                                @NotNull @RequestParam(value = "cityID") Integer cityId) {
        String taskId = UUID.randomUUID().toString();
        PathCalRequest request = new PathCalRequest(taskId, startStaId, endStaId, searchTime, scheme, cityId);

        String routingKey = SCHEME_MIN_TRANSFER.equals(scheme)
                ? RabbitMqConstant.MIN_TRANSFER_ROUTING_KEY
                : RabbitMqConstant.MIN_TIME_ROUTING_KEY;
        rabbitTemplate.convertAndSend(RabbitMqConstant.PATH_CAL_EXCHANGE, routingKey, request);
        return Result.build(taskId, ResultCodeEnum.TASK_RECEIVE);
    }

    /**
     * 查询路径规划结果。
     *
     * @param taskId 任务 ID
     * @return 规划结果或处理中状态
     */
    @GetMapping("/result")
    public Result<?> getPathCalculationResult(@NotNull @RequestParam("taskID") String taskId) {
        Object result = redisTemplate.opsForValue().get(RedisKeyConstant.PATH_CAL_RESULT_PREFIX + taskId);
        if (result == null) {
            return Result.build(null, ResultCodeEnum.PROCESSING);
        }
        return Result.ok(result);
    }
}
