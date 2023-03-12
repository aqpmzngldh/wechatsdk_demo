package cn.qianyekeji.ruiji.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class SimpleRedisLock {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public SimpleRedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new StringRedisSerializer());

        // 初始化模板
        this.redisTemplate.afterPropertiesSet();

        //加载 Lua 脚本,把017889abcceefmpx,03/11开头的key里面03/11变成03/12
        this.redisTemplate.execute((RedisCallback<Void>) connection -> {
            String script = "local keys = redis.call('KEYS', '017889abcceefmpx,03/11*')\n" +
                    "            for i, key in ipairs(keys) do\n" +
                    "                local newkey = string.gsub(key, '03/11', '03/12')\n" +
                    "            redis.call('RENAME', key, newkey)\n" +
                    "            redis.call('PERSIST', newkey)\n" +
                    "            end";
            connection.eval(script.getBytes(), ReturnType.STATUS, 0, new byte[0]);
            return null;
        });
    }
}
