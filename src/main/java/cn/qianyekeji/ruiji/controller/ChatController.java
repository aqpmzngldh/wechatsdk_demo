package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.date.DateTime;
import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.entity.Chat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping
    public R<String> save(String time, String body) {
//        log.info("传递的数据分别是{}和{}",time,body );
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        time=currentDateTime.format(formatter);

        String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
        Map<String, String> chatRecord = new HashMap<>();
        chatRecord.put("body", body);
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
        redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时
        return R.success("聊天记录已成功保存");
    }
    @GetMapping
    public R<List<Chat>> list() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        String key = today + "*";
//        String key = "2023/2/16" + "*";
        String key = DateTimeFormatter.ofPattern("uuuu/MM/d")
                .withResolverStyle(ResolverStyle.STRICT)
                .format(LocalDate.parse(today, DateTimeFormatter.BASIC_ISO_DATE)) + "*";

        Set<String> keys = redisTemplate.keys(key);
        List<Chat> chats = new ArrayList<>();

        for (String k : keys) {
            Map<Object, Object> chatRecord = redisTemplate.opsForHash().entries(k);
            String time = k.substring(k.lastIndexOf("_") + 1, k.indexOf("-"));
            String body = (String) chatRecord.get("body");
            Chat chat = new Chat(time, body);
            chats.add(chat);
        }

        // 将聊天记录按时间先后排序
        chats.sort((c1, c2) -> c1.getTime().compareTo(c2.getTime()));

        return R.success(chats);
    }
}
