package com.example.wayhome.consumer;

import com.example.wayhome.constant.RedisKeyConstant;
import com.example.wayhome.dto.PathCalRequest;
import com.example.wayhome.service.PathCalculateService;
import com.example.wayhome.vo.SolutionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 最短时间路径规划任务消费者。
 *
 * @author WayHome
 */
@Slf4j
@Component
@RabbitListener(queues = "minTime.queue")
public class MinTimeConsumer {

    private final PathCalculateService pathCalculateService;

    private final RedisTemplate<String, Object> redisTemplate;

    public MinTimeConsumer(PathCalculateService pathCalculateService, RedisTemplate<String, Object> redisTemplate) {
        this.pathCalculateService = pathCalculateService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 处理最短时间规划任务，并将结果写入缓存。
     *
     * @param request 任务请求
     */
    @RabbitHandler
    public void process(PathCalRequest request) {
        String resultKey = RedisKeyConstant.PATH_CAL_RESULT_PREFIX + request.getTaskId();
        try {
            List<SolutionVO<Double>> result = pathCalculateService.minTimeCostCal(
                    request.getStartStaID(), request.getEndStaID(), request.getSearchTime(), request.getCityID());
            redisTemplate.opsForValue().set(resultKey, result, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("最短时间路径规划失败, taskId={}", request.getTaskId(), e);
            redisTemplate.opsForValue().set(resultKey, "ERROR: " + e.getMessage(), 1, TimeUnit.MINUTES);
        }
    }
}
