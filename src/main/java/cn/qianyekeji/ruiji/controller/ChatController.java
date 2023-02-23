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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
    @Value("${ruiji.path2}")
    private String basePath;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private GiteeUploader giteeUploader;

    @PostMapping
    public R<String> save(@RequestParam(value = "file", required = false) MultipartFile multipartFile, String address, String name, String time, String body) throws Exception {

        String forObject = "";
        String URL = "http://api.map.baidu.com/?qterm=pc&coding=utf-8&coord=bd09ll&extensions=1&callback_type=jsonp&ak=B13d386658b7f5e9c2e2294e0314afbe&qt=hip&v=3.0&ie=utf-8&oue=1&fromproduct=jsapi&res=api&ak=B13d386658b7f5e9c2e2294e0314afbe&callback=BMap._rd._cbk52177&v=3.0&seckey=KSvl4YKPd09sveyXHd34A5AtqJjd34WGW%2BQrZZ7unLw%3D%2CQPaRZS15geTSh-2b4EtwJFykhQVSONaFWVgb24ZAn7ZTJHdXlOHvBjB4uf4101iaxa0HG3kTySkDJVjjPJyBCy7KaF06nPNXdXBQBC4B8YabR-QkYm4SS9efnNDneboyj6pXJej1j688j8fpC0F_LTgzAORDCVqHUjE7nHTxi3pKuHMtMhPUQwhPs8Ib9X-Z&timeStamp=1676978163595&sign=4d2178bd7efc";
        String encodedURL = URLEncoder.encode(URL, "UTF-8");
        String decodedURL = URLDecoder.decode(encodedURL, "UTF-8");
        URI uri = new URI(decodedURL);
        RestTemplate restTemplate = new RestTemplate();
        forObject = restTemplate.getForObject(uri, String.class);
        System.out.println("----------------------");
        System.out.println(forObject);


        if (multipartFile != null) {
            //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
            log.info(multipartFile.toString());
            //原始文件名
            String originalFilename = multipartFile.getOriginalFilename();//abc.jpg
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//.jpg
            //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
            String fileName = UUID.randomUUID().toString() + suffix;//dfsdfdfd.jpg
            //创建一个目录对象
            File dir = new File(basePath);
            //判断当前目录是否存在
            if (!dir.exists()) {
                //目录不存在，需要创建
                dir.mkdirs();
            }
            try {
                //将临时文件转存到指定位置
                multipartFile.transferTo(new File(basePath + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);

            String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", "");
            chatRecord.put("url", fileName);
            chatRecord.put("name", name);
            chatRecord.put("address", address);
            chatRecord.put("s", forObject);
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

            return R.success(fileName);

        } else {
//        log.info("传递的数据分别是{}和{}和{}",time,body,name );
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);

            String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", body);
            chatRecord.put("url", "");
            chatRecord.put("name", name);
            chatRecord.put("address", address);
            chatRecord.put("s", forObject);
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
            String name = (String) chatRecord.get("name");
            Chat chat = new Chat(time, body, url, name);
            chats.add(chat);
        }

        // 将聊天记录按时间先后排序
        chats.sort((c1, c2) -> c1.getTime().compareTo(c2.getTime()));

        return R.success(chats);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
