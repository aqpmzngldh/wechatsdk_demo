package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private GiteeUploader giteeUploader;

    @PostMapping
    public R<String> save(@RequestParam(value = "file", required = false) MultipartFile multipartFile, String time, String body) throws Exception {
        if (multipartFile!=null) {
            log.info("uploadImg()请求已来临...");
            //根据文件名生成指定的请求url
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("請求失敗");
            }
            String targetURL = giteeUploader.createUploadFileUrl(originalFilename);
            log.info("目标url：" + targetURL);
            //请求体封装
            Map<String, Object> uploadBodyMap = giteeUploader.getUploadBodyMap(multipartFile.getBytes());
            //借助HttpUtil工具类发送POST请求
            String JSONResult = HttpUtil.post(targetURL, uploadBodyMap);
            //解析响应JSON字符串
            JSONObject jsonObj = JSONUtil.parseObj(JSONResult);
            //请求失败
            if (jsonObj == null || jsonObj.getObj("commit") == null) {
                return R.error("請求失敗");
            }
            //请求成功：返回下载地址
            JSONObject content = JSONUtil.parseObj(jsonObj.getObj("content"));
            String url = (String) content.get("download_url");

            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            time=currentDateTime.format(formatter);

            String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("url", url);
            chatRecord.put("body","");
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

            return R.success("图片已成功保存");

        }else {
        log.info("传递的数据分别是{}和{}",time,body );
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        time=currentDateTime.format(formatter);

        String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
        Map<String, String> chatRecord = new HashMap<>();
        chatRecord.put("body", body);
        chatRecord.put("url","");
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
        redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时
        return R.success("聊天记录已成功保存");
        }
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
            String url = (String) chatRecord.get("url");
//            Chat chat = new Chat(time, body);
            Chat chat = new Chat(time, body,url);
            chats.add(chat);
        }

        // 将聊天记录按时间先后排序
        chats.sort((c1, c2) -> c1.getTime().compareTo(c2.getTime()));

        return R.success(chats);
    }
}
