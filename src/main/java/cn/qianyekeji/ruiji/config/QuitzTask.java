package cn.qianyekeji.ruiji.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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

    @Scheduled(cron = "0 00 6 * * ?")
    public void executeTask() {
        //获取每日推荐句子：
        String tianapi_data = "";
        try {
            URL url = new URL( "https://apis.tianapi.com/zaoan/index");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            OutputStream outputStream = conn.getOutputStream();
            String content = "key=0ddda4a92837e9f61720eb89f5717827";
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            byte[] data = new byte[1024];
            StringBuilder tianapi = new StringBuilder();
            int len;
            while ((len=inputStream.read(data)) != -1) {
                String t = new String(data,0,len);
                tianapi.append(t);
            }
            tianapi_data = tianapi.toString();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONUtil.parseObj(tianapi_data);
        String content = jsonObject.getJSONObject("result").getStr("content");
        // 执行要定时执行的任务
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String time = currentDateTime.format(formatter);
        String key = time +(int)(Math.random() * 90 + 10); // 生成唯一键
        String touxiang="./../images/1.ico";
        Map<String, String> chatRecord = new HashMap<>();
        chatRecord.put("body", content);
        chatRecord.put("name", "聪明的汤姆");
        chatRecord.put("touXiang", touxiang);
        chatRecord.put("zan", "1");
        chatRecord.put("uuid", "abcdefgh");
        chatRecord.put("number", "0");
        chatRecord.put("address", "108.94615__34.33264");
        chatRecord.put("url", "");
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
        redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时


    }

//    @Scheduled(cron = "0 22 9 * * ?")
//    public void executeTask1() {
//
//        // 执行要定时执行的任务
//        LocalDateTime currentDateTime = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        String time = currentDateTime.format(formatter);
//        String key = time +(int)(Math.random() * 90 + 10); // 生成唯一键
//        String touxiang="./../images/1.ico";
//        Map<String, String> chatRecord = new HashMap<>();
//
//        String[] arr = {"有人吗", "你好", "这是做什么的", "666", "这挺好玩"};
//        Random random = new Random();
//        int index = random.nextInt(arr.length);
//        String randomElement = arr[index];
//
//        chatRecord.put("body", randomElement);
//        chatRecord.put("name", "聪明的汤姆");
//        chatRecord.put("touXiang", touxiang);
//        chatRecord.put("zan", "1");
//        chatRecord.put("uuid", "abcdefg");
//        chatRecord.put("number", "0");
//        chatRecord.put("address", "109.60054205623__35.177045407445");
//        chatRecord.put("url", "");
//        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
//        chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
//        redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
//        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时
//
//
//    }
}
