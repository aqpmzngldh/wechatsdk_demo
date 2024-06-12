package cn.qianyekeji.wechatsdk.service.impl;

import cn.qianyekeji.wechatsdk.common.R;
import cn.qianyekeji.wechatsdk.service.CountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CountServiceImpl implements CountService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void addCount(String roomId, String talker) {
        System.out.println("当前群聊的房间名是" + roomId);
        System.out.println("当前发消息的人是" + talker);
        String key = "a_jifen_count_" + roomId + "_" + talker;
        Long count = redisTemplate.opsForValue().increment(key, 0);
        if (count != null && count >= 10) {
            return;
        }
        Double score = redisTemplate.opsForZSet().score("a_jifen_"+roomId, talker);
        if (score == null) {
            redisTemplate.opsForZSet().add("a_jifen_"+roomId, talker, 1.0);
        } else {
            Double newScore = score + 1.0;
            redisTemplate.opsForZSet().add("a_jifen_"+roomId, talker, newScore);
        }
        LocalDate today = LocalDate.now();
        long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long remainingTime = endOfDay - System.currentTimeMillis();
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, remainingTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public HashMap selectCount(String roomId) {
        System.out.println("活跃度查询");
        String key = "a_jifen_"+roomId;
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> set = zSetOps.reverseRange(key, 0, 14);
        LinkedHashMap<Object, Object> objectObjectHashMap = new LinkedHashMap<>();
        for (String element : set) {
            long score = zSetOps.score(key, element).longValue();
            objectObjectHashMap.put(element,score);

        }
        return  objectObjectHashMap;
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    public void remove() {
        Set<String> keys = redisTemplate.keys("a_jifen_*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
