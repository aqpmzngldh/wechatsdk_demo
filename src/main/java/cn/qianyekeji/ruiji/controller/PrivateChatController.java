package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/privateChat")
@Slf4j
public class PrivateChatController {
    @Value("${ruiji.path2}")
    private String basePath;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private GiteeUploader giteeUploader;
    @Autowired
    private SmsService smsService;

    @PostMapping
    public R<String> save(@RequestParam(value = "file", required = false) MultipartFile multipartFile,String parameter,String parameter1, HttpServletRequest request, String address,String uuid, String name, String time, String body) throws Exception {


        String prefix = name.substring(0, 3); // 截取前三个字符
        String suffix1 = name.substring(3); // 截取剩余的字符

        List<String> prefixList = Arrays.asList("淘气的", "爱动的", "调皮的", "可爱的", "聪明的");
        List<String> suffixList = Arrays.asList("大熊", "哆嗦A梦", "小夫", "胖虎", "蝎子莱莱", "鲨鱼辣椒", "蜘蛛侦探", "蟑螂恶霸", "汤姆", "杰瑞");

        if (!prefixList.contains(prefix) || !suffixList.contains(suffix1)) {
            return null;
        }
        if (parameter1==null||parameter==null){
            return null;
        }
        String touxiang="";
        if ("汤姆".equals(suffix1)) {
            touxiang="./../images/1.ico";
        }
        if ("杰瑞".equals(suffix1)) {
            touxiang="./../images/2.ico";
        }
        if ("大熊".equals(suffix1)) {
            touxiang="./../images/3.ico";
        }
        if ("小夫".equals(suffix1)) {
            touxiang="./../images/4.ico";
        }
        if ("哆嗦A梦".equals(suffix1)) {
            touxiang="./../images/5.ico";
        }
        if ("胖虎".equals(suffix1)) {
            touxiang="./../images/6.ico";
        }
        if ("蝎子莱莱".equals(suffix1)) {
            touxiang="./../images/7.ico";
        }
        if ("鲨鱼辣椒".equals(suffix1)) {
            touxiang="./../images/8.ico";
        }
        if ("蜘蛛侦探".equals(suffix1)) {
            touxiang="./../images/9.ico";
        }
        if ("蟑螂恶霸".equals(suffix1)) {
            touxiang="./../images/10.ico";
        }


        try {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            LambdaQueryWrapper<Sms> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Sms::getIpAddress, ipAddress);
            Sms sms1 = smsService.getOne(queryWrapper);
            if (sms1 == null) {
                Sms sms = new Sms();
                sms.setNumber("1");
                sms.setIpAddress(ipAddress);
                smsService.save(sms);
            } else {
                sms1.setNumber((Integer.parseInt(sms1.getNumber()) + 1) + "");
                smsService.updateById(sms1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);

            String s = parameter + parameter1;
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String sortedS = new String(chars);
            String key = sortedS+","+time; // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", "");
            chatRecord.put("url", fileName);
            chatRecord.put("name", name);
            chatRecord.put("number", "0");
            chatRecord.put("address", address);
            chatRecord.put("zan", "1");
            chatRecord.put("uuid", uuid);
            chatRecord.put("touXiang", touxiang);
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

            return R.success(fileName);

        } else {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);
            String s = parameter + parameter1;
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String sortedS = new String(chars);
            String key = sortedS+","+time; // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", body);
            chatRecord.put("url", "");
            chatRecord.put("name", name);
            chatRecord.put("number", "0");
            chatRecord.put("address", address);
            chatRecord.put("uuid", uuid);
            chatRecord.put("zan", "1");
            chatRecord.put("touXiang", touxiang);
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时
            return R.success("聊天记录已成功保存");
        }
    }

    @GetMapping
    public R<List<Chat>> list(String parameter,String parameter1) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        String time1 = currentDateTime.format(formatter);

        String s = parameter + parameter1;
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        String sortedS = new String(chars);
        String key=sortedS+","+time1+ "*";
        int position = key.length() - 3; // 获取要插入字符的位置，即倒数第三个位置
        key = key.substring(0, position) + "/" + key.substring(position); // 在指定位置插入字符

        Set<String> keys = redisTemplate.keys(key);
        List<Chat> chats = new ArrayList<>();

        for (String k : keys) {
            Map<Object, Object> chatRecord = redisTemplate.opsForHash().entries(k);
            int length = 14; // 我们要截取的子串长度为8
            String time = k.substring(k.length() - length);

            String body = (String) chatRecord.get("body");
            String url = (String) chatRecord.get("url");
            String name = (String) chatRecord.get("name");
            String number = (String) chatRecord.get("number");
            String touXiang = (String) chatRecord.get("touXiang");
            String uuid = (String) chatRecord.get("uuid");
            String voice = (String) chatRecord.get("voice");
            Chat chat = new Chat(time, body, url, name, k, number,touXiang,uuid,voice);
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

    @PostMapping("/zan/{p}/{k}")
    public R<String> dianzan(@PathVariable("p") String p, @PathVariable("k") String k) throws Exception {

        if (p==""||p==null){
            return null;
        }

        System.out.println(p + "-----------------------");
        k = new String(Base64.getDecoder().decode(k));
        System.out.println(k + "-----------------------");
        //每次点赞的时候首先判断传递的uuid是否在hash结构中key为zan的字符串中
        //如果不在，那我们新增这个，然后number+1
        //如果在那我们删除掉这个，然后number-1
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        // 将新值设置为哈希类型字段的值
        String zong = hashOps.get(k, "zan");

        String[] arr2 = zong.split(",");
        Integer num = 0;
        ArrayList<String> list = new ArrayList<>(Arrays.asList(arr2));
        if (list.contains(p)) {
            num++;
            list.remove(p);
        } else {
            list.add(p);
        }
        String[] newArr2 = list.toArray(new String[0]);
        String newStr2 = String.join(",", newArr2); // 将新数组转换成以逗号分隔的字符串
        hashOps.put(k, "zan", newStr2);
        //number-1
        if (num == 0) {
//            不包含，这时候新增number+1
            String s = hashOps.get(k, "number");
            String a = s.length() == 0 ? "0" : s;
            hashOps.put(k, "number", (Integer.parseInt(a) + 1) + "");
        } else {
//            包含，number-1
            String s = hashOps.get(k, "number");
            String a = s.length() == 0 ? "0" : s;
            hashOps.put(k, "number", (Integer.parseInt(a) - 1) + "");
        }
        return null;

    }
}
