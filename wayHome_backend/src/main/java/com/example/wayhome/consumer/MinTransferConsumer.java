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
 * 最少换乘路径规划任务消费者。
 *
 * @author WayHome
 */
@Slf4j
@Component
@RabbitListener(queues = "minTransfer.queue")
public class MinTransferConsumer {

    private final PathCalculateService pathCalculateService;

    private final RedisTemplate<String, Object> redisTemplate;

    public MinTransferConsumer(PathCalculateService pathCalculateService, RedisTemplate<String, Object> redisTemplate) {
        this.pathCalculateService = pathCalculateService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 处理最少换乘规划任务，并将结果写入缓存。
     *
     * @param request 任务请求
     */
    @RabbitHandler
    public void process(PathCalRequest request) {
        String resultKey = RedisKeyConstant.PATH_CAL_RESULT_PREFIX + request.getTaskId();
        try {
            List<SolutionVO<Integer>> result = pathCalculateService.minTransfer(
                    request.getStartStaID(), request.getEndStaID(), request.getSearchTime(), request.getCityID());
            redisTemplate.opsForValue().set(resultKey, result, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("最少换乘路径规划失败, taskId={}", request.getTaskId(), e);
            redisTemplate.opsForValue().set(resultKey, "ERROR: " + e.getMessage(), 1, TimeUnit.MINUTES);
        }
    }
}
