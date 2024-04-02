package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import cn.qianyekeji.ruiji.utils.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/other1")
@Slf4j
public class Other1Controller {
    @Value("${ruiji.path2}")
    private String basePath;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private GiteeUploader giteeUploader;
    @Autowired
    private SmsService smsService;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private CeShiService ceShiService;

    @PostMapping
    public R<String> save(@RequestParam(value = "file", required = false) MultipartFile multipartFile, String parameter, String parameter1, HttpServletRequest request, String address, String uuid, String name, String time, String body) throws Exception {


        String prefix = name.substring(0, 3); // 截取前三个字符
        String suffix1 = name.substring(3); // 截取剩余的字符

        List<String> prefixList = Arrays.asList("淘气的", "爱动的", "调皮的", "可爱的", "聪明的");
        List<String> suffixList = Arrays.asList("大熊", "哆嗦A梦", "小夫", "胖虎", "蝎子莱莱", "鲨鱼辣椒", "蜘蛛侦探", "蟑螂恶霸", "汤姆", "杰瑞");

        if (!prefixList.contains(prefix) || !suffixList.contains(suffix1)) {
            return null;
        }
        if (parameter == null) {
            return null;
        }
        String touxiang = "";
        if ("汤姆".equals(suffix1)) {
            touxiang = "./../images/1.ico";
        }
        if ("杰瑞".equals(suffix1)) {
            touxiang = "./../images/2.ico";
        }
        if ("大熊".equals(suffix1)) {
            touxiang = "./../images/3.ico";
        }
        if ("小夫".equals(suffix1)) {
            touxiang = "./../images/4.ico";
        }
        if ("哆嗦A梦".equals(suffix1)) {
            touxiang = "./../images/5.ico";
        }
        if ("胖虎".equals(suffix1)) {
            touxiang = "./../images/6.ico";
        }
        if ("蝎子莱莱".equals(suffix1)) {
            touxiang = "./../images/7.ico";
        }
        if ("鲨鱼辣椒".equals(suffix1)) {
            touxiang = "./../images/8.ico";
        }
        if ("蜘蛛侦探".equals(suffix1)) {
            touxiang = "./../images/9.ico";
        }
        if ("蟑螂恶霸".equals(suffix1)) {
            touxiang = "./../images/10.ico";
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

            String s = parameter;
            String key = s + "," + time; // 生成唯一键
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
            redisTemplate.expire(key, 72, TimeUnit.HOURS); // 设置键的过期时间为 24 小时

            return R.success(fileName);

        } else {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
            time = currentDateTime.format(formatter);
            String s = parameter;
            String key = s + "," + time; // 生成唯一键
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
            redisTemplate.expire(key, 72, TimeUnit.HOURS); // 设置键的过期时间为 24 小时
            return R.success("聊天记录已成功保存");
        }
    }

    @GetMapping
    public R<List<Chat>> list(String parameter) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
        String time1 = currentDateTime.format(formatter);

        String s = parameter;
//        String key = s + "," + time1 + "*";
        String key = s + "*";

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
            Chat chat = new Chat(time, body, url, name, k, number, touXiang, uuid,voice);
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

    @PostMapping("/value/{k}")
    public R<String> dianzan(@PathVariable("k") String k,HttpServletRequest request) throws Exception {
        Set<String> members = redisTemplate.opsForSet().members("guanli");
        int guanli = members.size();
        if (guanli>6){
            return R.error("当前管理已满");
        }

        // 根据传递的参数k在数据库中查找同名邮箱
        String existingValue = null;
        for (String value : members) {
            String[] parts = value.split("---");
            if (parts.length == 2 && parts[0].equals(k)) {
                existingValue = value;
                break; // 找到匹配的邮箱就退出循环
            }
        }

        if (existingValue != null) {
            String[] parts = existingValue.split("---");
            if (parts[1].equals("0")) {
                // 激活失败，返回前端让用户去激活
                return R.error("请登录该邮箱后完成管理员激活");
            } else {
                // 已经是激活成功状态
                return R.error("您已经是管理员，无需进一步操作");
            }
        } else {
            // 数据库中的数据里没有同名邮箱，创建一个新的并发送激活邮件
            String code = RandomUtil.randomNumbers(4);
            String openid = "_"+request.getSession().getAttribute("openid");
            redisTemplate.opsForValue().set(k, code+openid);
            code+=openid;
            mailUtil.send("", k, "【匿名群聊提醒】", "<a href=https://qianyekeji.cn/other/active/" + k + "/" + code + ">【匿名群聊】-点击激活管理员</a>", Collections.singletonList(""));

            // 将新邮箱加入数据库
            String newValue = k + "---0";
            redisTemplate.opsForSet().add("guanli", newValue);

            return R.error("请登录该邮箱后完成管理员激活");
        }
    }


    @GetMapping("/active/{k}/{code}")
    public R<String> active(@PathVariable("k") String k,@PathVariable("code") String code,HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(k);
        Set<String> members = redisTemplate.opsForSet().members("guanli");
        String s = redisTemplate.opsForValue().get(k);

        String[] parts1 = s.split("_");

        s = parts1[0];
        String openid = parts1[1];
        System.out.println(s+"-------"+openid);
        if (!s.equals(code.split("_")[0])){
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("请不要胡乱操作，谢谢ε=(´ο｀*)))唉");
            return null;
        }
        for (String value : members) {
            String[] parts = value.split("---"); // 按照---分隔符分割元素
            if (parts.length == 2 && parts[0].equals(k)) {
                //这时候有同名邮箱这时候进一步判断激活状态是成功1还是失败0
                if (parts[1].equals("0")) {
                    //把0改成1，设置成已激活，然后页面回写已激活，请登录：localhost...
                    parts[1]="1";
                    redisTemplate.opsForSet().remove("guanli", value);
                    redisTemplate.opsForSet().add("guanli", parts[0] + "---" + parts[1]);
                    response.setContentType("text/html;charset=UTF-8");
//                    response.getWriter().write("激活成功，请<a href='http://localhost:8089/front/page/chat.html'>登录</a>");
                    response.getWriter().write("激活成功，请<a href='https://qianyekeji.cn/front/page/chat.html'>登录</a>");

                    String s1 = ceShiService.access_token();
                    System.out.println(openid+"-----------------");
                    System.out.println(s1+"---------------------");
                    if (openid==null){
                        return null;
                    }
                    String url ="https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+s1;
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("touser",openid);
                    hashMap.put("template_id","2QEHcTlHp7CH7JTc-h604QdsbyaMoUaJ5dPYVUiVaRI");
                    HashMap<String, Object> hashMap1 = new HashMap<>();
                    HashMap<String, Object> thing2 = new HashMap<>();
                    thing2.put("value", "管理员添加成功");
                    hashMap1.put("thing2", thing2);

                    HashMap<String, Object> thing3 = new HashMap<>();
                    thing3.put("value", "没有模板随便找一个");
                    hashMap1.put("thing3", thing3);

                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    String currentDate = sdf.format(date);
                    HashMap<String, Object> time4 = new HashMap<>();
                    time4.put("value", currentDate);
                    hashMap1.put("time4", time4);
                    hashMap.put("data",hashMap1);

                    String jsonString111 = JSONUtil.toJsonStr(hashMap);
                    HttpUtil.createPost(url).body(jsonString111, "application/json").execute();
//                    HttpResponse execute = HttpUtil.createPost(url).execute();
                    return null;
                } else {
                    //已经是激活成功转态,页面回写已激活
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write("您已经是管理员，无需进一步操作");
                    return null;
                }
            }
        }
        return null;
    }
    @PostMapping("/tiXing")
    public R<String> tiXing() throws Exception {
        Set<String> members = redisTemplate.opsForSet().members("guanli");
        for (String value : members) {
            String[] parts = value.split("---"); // 按照---分隔符分割元素
            if (parts.length == 2 && parts[1].equals("1")) {
                mailUtil.send("",parts[0],"【匿名群聊提醒】","有人来找你聊天了", Collections.singletonList(""));

//                Set<String> members1 = redisTemplate.opsForSet().members(parts[0]);
                String value1 = redisTemplate.opsForValue().get(parts[0]);

                String[] parts1 = value1.split("_"); // 按照---分隔符分割元素
                        //获取管理员的openid
                        String open_id = parts1[1];
                        //给管理员发送模板消息提醒有人发消息了
                        String s1 = ceShiService.access_token();
                        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + s1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("touser", open_id);
                        hashMap.put("template_id", "2QEHcTlHp7CH7JTc-h604QdsbyaMoUaJ5dPYVUiVaRI");
                        HashMap<String, Object> hashMap1 = new HashMap<>();
                        HashMap<String, Object> thing2 = new HashMap<>();
                        thing2.put("value", "管理员-有人找你聊天啦");
                        hashMap1.put("thing2", thing2);

                        HashMap<String, Object> thing3 = new HashMap<>();
                        thing3.put("value", "没有模板随便找一个");
                        hashMap1.put("thing3", thing3);

                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                        String currentDate = sdf.format(date);
                        HashMap<String, Object> time4 = new HashMap<>();
                        time4.put("value", currentDate);
                        hashMap1.put("time4", time4);
                        hashMap.put("data", hashMap1);

                        String jsonString111 = JSONUtil.toJsonStr(hashMap);
                        HttpUtil.createPost(url).body(jsonString111, "application/json").execute();


            }
        }
        return null;
    }

    @PostMapping("/ETH")
    public R<Double> eth() throws Exception {
//        URL url = new URL( "https://data.mifengcha.com/api/v3/price?slug=ethereum&api_key=AWEIVXYYD8O4PWJL55UNTTA1ZZUEPDT1NRHPFKZE");
        String URL = "https://data.mifengcha.com/api/v3/price?slug=ethereum&api_key=MNZFFJLNHDRFEM8C9QUDSHNGYXRHSOC05O5NVFFI";
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(URL, String.class);
        Dict dict = Dict.create();
        dict.put("data", forObject);
        JSONObject jsonObject = JSONUtil.parseObj(dict);
        // 获取数据对象数组
        JSONArray dataArray = jsonObject.getJSONArray("data");
        // 获取数组中的第一个对象
        JSONObject dataObject = dataArray.getJSONObject(0);
        // 获取 u 属性的值
        double uValue = dataObject.getDouble("u");
        return R.success(uValue);
    }

    @PostMapping("/eth")
    public R<List<String>> ETH() throws Exception {
        // 当前时间的凌晨
        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long nowMillis = now.toEpochMilli();

        // 15天之前的凌晨
        LocalDate localDate = LocalDate.now().minusDays(15);
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant();
        long startMillis = instant.toEpochMilli();

        String URL = "https://data.mifengcha.com/api/v3/price/history?slug=ethereum&interval=1d&start="+startMillis+"&end="+nowMillis+"&api_key=MNZFFJLNHDRFEM8C9QUDSHNGYXRHSOC05O5NVFFI";
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(URL, String.class);
        //因为蜜蜂提供数据的特殊，这里要拼接一下再取要不然报错
        Dict dict = Dict.create();
        dict.put("data", forObject);
        JSONObject jsonObject = JSONUtil.parseObj(dict);
        // 获取数据对象数组
        JSONArray dataArray = jsonObject.getJSONArray("data");
        List<String> uValues = new ArrayList<>();
        // 循环遍历每一个数据对象，获取 u 值并存储到 uValues 中
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            double uValue = dataObject.getDouble("u");
            Long t = new Double(dataObject.getDouble("T")).longValue();
            String time = getTime(t);
            String s = uValue +"--"+ time;
            uValues.add(s);
        }

        return R.success(uValues);
    }
    public String getTime(Long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd")
                .withZone(ZoneId.of("UTC"));
        String formattedDateTime = formatter.format(instant);
        return formattedDateTime;
    }
}