package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import it.sauronsoftware.jave.AudioUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    @Autowired
    private SmsService smsService;
    @Autowired
    private CeShiService ceShiService;

    @PostMapping
    public R<String> save(@RequestParam(value = "file", required = false) MultipartFile multipartFile, HttpServletRequest request, String address,String uuid, String name, String time, String body,String voiceid) throws Exception {


        String prefix = name.substring(0, 3); // 截取前三个字符
        String suffix1 = name.substring(3); // 截取剩余的字符

        List<String> prefixList = Arrays.asList("淘气的", "爱动的", "调皮的", "可爱的", "聪明的");
        List<String> suffixList = Arrays.asList("大熊", "哆嗦A梦", "小夫", "胖虎", "蝎子莱莱", "鲨鱼辣椒", "蜘蛛侦探", "蟑螂恶霸", "汤姆", "杰瑞");

        if (!prefixList.contains(prefix) || !suffixList.contains(suffix1)) {
            return null;
        }
        if (uuid==""||uuid==null){
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

        String ss = redisTemplate.opsForValue().get("maren");
        String[] split = ss.split(",");
        for (String s : split) {
            if(body!=null&&body.contains(s)){
                return null;
            }
        }

//        try {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        // 对该 IP 地址对应的计数器进行自增操作
        redisTemplate.opsForValue().increment(ipAddress);
        // 设置过期时间为 2 分钟
        redisTemplate.expire(ipAddress, 2, TimeUnit.MINUTES);

        //这是先进行封印判断，在的直接返回
        Boolean isBanned2 = redisTemplate.opsForSet().isMember("banned_ips", ipAddress);
        if (isBanned2) {
            // IP 地址已经被封禁
            return null;
        }

        //这个是两分钟超过12条消息的自动封印
        int threshold = 12;
        // 获取该 IP 地址对应的计数器的当前值
        String s = redisTemplate.opsForValue().get(ipAddress);
        // 如果当前值超过了阈值，就进行封禁操作
        if (s != null && Integer.parseInt(s) > threshold) {
            // 添加到set集合键banned_ips永久封禁该 IP 地址
            Boolean isBanned = redisTemplate.opsForSet().isMember("banned_ips", ipAddress);
            if (isBanned) {
                // IP 地址已经被封禁
            } else {
                // 将该 IP 地址添加到已封禁的 IP 地址集合中
                redisTemplate.opsForSet().add("banned_ips", ipAddress);
            }
            return null;
        }


//            这里咱就不存到mysql了，全部交给redis管理
//            LambdaQueryWrapper<Sms> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(Sms::getIpAddress, ipAddress);
//            Sms sms1 = smsService.getOne(queryWrapper);
//            if (sms1 == null) {
//                Sms sms = new Sms();
//                sms.setNumber("1");
//                sms.setIpAddress(ipAddress);
//                smsService.save(sms);
//            } else {
//                sms1.setNumber((Integer.parseInt(sms1.getNumber()) + 1) + "");
//                smsService.updateById(sms1);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println("----------------------");
        System.out.println(multipartFile);
        System.out.println(voiceid);
        System.out.println("----------------------");
         if (multipartFile != null) {
            System.out.println("---------");
            System.out.println(multipartFile);
            System.out.println("---------");
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
                //将临时文件转存到指定位置（转存时候容易出问题，这里不转存了，改成下面代码）
                //上传的文件保存到指定目录，而不是先复制到临时目录
                //multipartFile.transferTo(new File(basePath + fileName));
                InputStream inputStream = multipartFile.getInputStream();
                OutputStream outputStream = new FileOutputStream(new File(basePath + fileName));
                IOUtils.copy(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);

//            因为我要更进行地图查询功能，直接从聊天界面复制过去的我们已经给时间戳截取了，这样的话我们
//            直接用hash中前半段去匹配比较麻烦，所以这时候直接显示上去，到时候就能复制整个键
//            String key = time + "-" + UUID.randomUUID().toString(); // 生成唯一键
            String key = time + (int)(Math.random() * 90 + 10); // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", "");
            chatRecord.put("url", fileName);
            chatRecord.put("name", name);
            chatRecord.put("number", "0");
            chatRecord.put("address", address);
            chatRecord.put("ipAddress", ipAddress);
            chatRecord.put("zan", "1");
            chatRecord.put("uuid", uuid);
            chatRecord.put("touXiang", touxiang);
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

            return R.success(fileName);

        } else {
            if (address==""||address==null||uuid==""||uuid==null||body.length()>50){
                return null;
            }
//        log.info("传递的数据分别是{}和{}和{}",time,body,name );
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);

            String key = time +(int)(Math.random() * 90 + 10); // 生成唯一键
            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put("body", body);
            chatRecord.put("url", "");
            chatRecord.put("name", name);
            chatRecord.put("number", "0");
            chatRecord.put("address", address);
            chatRecord.put("ipAddress", ipAddress);
            chatRecord.put("uuid", uuid);
            chatRecord.put("zan", "1");
            chatRecord.put("touXiang", touxiang);
             chatRecord.put("voice", voiceid);
            long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
            chatRecord.put("timestamp", Long.toString(timestamp)); // 存储时间戳
            redisTemplate.opsForHash().putAll(key, chatRecord); // 将聊天记录存储到 Redis 中
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

//            存经纬度到redis的set数据结构中，方便地图查看
//            redisTemplate.opsForSet().add("wcls", address);

            // 不用set了，因为我要获取具体的排序，直接用sortedset，里面的score排序列用集合中元素size，这样也不会重复
            // 获取操作sortedset类型的ZSetOperations对象
//            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            // 获取集合中元素的数量作为排序字段
//            long score = zSetOps.size("wcls")+1;
            // 将wcls键存储到sortedset类型中，将集合中元素的数量作为排序字段存储到score中
//            zSetOps.add("wcls", address, score);

            //不用上面那样搞了，因为会通过百度地图查看id获取最新用户知道他的位置，这时候我们更改实现方式
            //存时间戳，然后取出时候，根据和当前时间的差值比较计算，来实现等级的修炼提升，比如存储时间超过一个月了
            //这时候就是从练气一层到练气二层
//            redisTemplate.opsForZSet().add("wcls", address, timestamp);
            // 获取当前address在sorted set中的分值
            Double score = redisTemplate.opsForZSet().score("wcls", address);
            if (score == null) {
                // 如果分值为null，则说明该address不存在，直接添加
                redisTemplate.opsForZSet().add("wcls", address, timestamp);
            } else {
                // 如果分值不为null，则说明该address已存在，不更新timestamp
            }
            return R.success("聊天记录已成功保存");
        }
    }

    @GetMapping
    public R<List<Chat>> list() {
        List<Chat> chats;
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        String key = today + "*";
//        String key = "2023/2/16" + "*";
            String key = DateTimeFormatter.ofPattern("uuuu/MM/dd")
                    .withResolverStyle(ResolverStyle.STRICT)
                    .format(LocalDate.parse(today, DateTimeFormatter.BASIC_ISO_DATE)) + "*";

            Set<String> keys = redisTemplate.keys(key);
            chats = new ArrayList<>();

            for (String k : keys) {
                Map<Object, Object> chatRecord = redisTemplate.opsForHash().entries(k);
                String time = k.substring(0, k.length() - 2);
//                String time = k;
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

    @GetMapping("/audio")
    public void audio(String name, HttpServletResponse response)throws Exception {
        //name就是媒体id，然后token也有了，发送get请求下载文件
        String s = ceShiService.access_token();
        // 构建URL
        String url="https://api.weixin.qq.com/cgi-bin/media/get?access_token="+s+"&media_id="+name+"";
        // 发送GET请求
        HttpResponse execute = HttpUtil.createGet(url).execute();
        InputStream inputStream = execute.bodyStream();
        // 创建本地文件输出流
        String outFile = "/www/server/img2/" + name + ".amr";
        OutputStream outputStream1 = new FileOutputStream(outFile);

        // 从输入流读取并写入输出流
        byte[] buffer = new byte[1024];
        int len1;
        while((len1=inputStream.read(buffer))!=-1){
            outputStream1.write(buffer,0,len1);
        }

        // 关闭流
        outputStream1.close();
        inputStream.close();

        System.out.println(name+"---------------=============-----------");

        File source = new File("/www/server/img2/"+name+".amr");
//        File target = new File("/www/server/img2/"+1+".mp3");
        File target = new File("/www/server/img2/"+name+".mp3");
        AudioUtils.amrToMp3(source, target);

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name+".mp3"));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("audio/mpeg");

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


    @PostMapping("/banned/{p}")
    public R<String> banned(@PathVariable("p") String p) throws Exception {
        System.out.println(p);
        if (p!=null) {
            try {
                p = new String(Base64.getDecoder().decode(p));
                System.out.println(p);
                if (p == null || p.length() == 0 || p.split(",").length != 2) {
                    return R.error("您没有资格操作");
                }

                Set<String> members = redisTemplate.opsForSet().members("guanli");
                for (String member : members) {
                    String[] parts = member.split("---"); // 按照---分隔符分割元素
                    if ("1".equals(parts[1])&&p.split(",")[1].equals(parts[0])){
                        Set<String> matchingKeys = redisTemplate.keys(p.split(",")[0]+"*");
                        for (String matchingKey : matchingKeys) {
                            Map<Object, Object> chatRecord = redisTemplate.opsForHash().entries(matchingKey);
                            String ipAddress = (String) chatRecord.get("ipAddress");
                            redisTemplate.opsForSet().add("banned_ips", ipAddress);
                        }
                        return R.error("封禁成功");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return R.error("无效操作");
    }

    @PostMapping("/delete/{p}")
    public R<String> delete(@PathVariable("p") String p) throws Exception {
        System.out.println(p);
        if (p!=null) {
            try {
                p = new String(Base64.getDecoder().decode(p));
                System.out.println(p);
                if (p == null || p.length() == 0 || p.split(",").length != 2) {
                    return R.error("您没有资格操作");
                }

                Set<String> members = redisTemplate.opsForSet().members("guanli");
                for (String member : members) {
                    String[] parts = member.split("---"); // 按照---分隔符分割元素
                    if ("1".equals(parts[1])&&p.split(",")[1].equals(parts[0])){
                        Set<String> matchingKeys = redisTemplate.keys(p.split(",")[0]+"*");
                        for (String matchingKey : matchingKeys) {
                            redisTemplate.delete(matchingKey);
                        }
                        return R.error("删除成功");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return R.error("无效操作");
    }
}
