package cn.qianyekeji.ruiji.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author liangshuai
 * @date 2023/2/23
 */
@EnableScheduling
@Component
public class QuitzTask {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 48 23 * * ?")
    public void executeTask() {
        // 执行要定时执行的任务
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String time = currentDateTime.format(formatter);

        String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
        Map<String, String> chatRecord = new HashMap<>();
        chatRecord.put("body", "大家好,我是汤姆");
        chatRecord.put("name", "聪明的汤姆");
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
        redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时


    }
}
