package com.example.wayhome.consumer;

import com.example.wayhome.dto.PathCalRequest;
import com.example.wayhome.service.PathCalculateService;
import com.example.wayhome.vo.SolutionVO;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(queues = "minTime.queue")
public class MinTimeConsumer {

    @Autowired
    private PathCalculateService pathCalculateService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitHandler
    public void process(PathCalRequest request) {
        String taskId = request.getTaskId();

        try {
            // 计算最短时间
            List<SolutionVO<Double>> result = pathCalculateService.minTimeCostCal(
                request.getStartStaID(), request.getEndStaID(), request.getSearchTime(), request.getCityID()
            );

            // 存入 Redis，1 分钟过期
            redisTemplate.opsForValue().set("path_cal_result:" + taskId, result, 1, TimeUnit.MINUTES);

        } catch (Exception e) {
            redisTemplate.opsForValue().set("path_cal_result:" + taskId, "ERROR: " + e.getMessage(), 1, TimeUnit.MINUTES);
        }
    }
}
