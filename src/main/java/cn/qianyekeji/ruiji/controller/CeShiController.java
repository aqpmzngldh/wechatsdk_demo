package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Message;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.entity.ceshi;
import cn.qianyekeji.ruiji.service.AddressBookService;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.ChatGptService;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import cn.qianyekeji.ruiji.utils.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/wx")
@Slf4j
public class CeShiController {
    private static final String APP_ID = "wx61c514e5d83894bf";
    private static final String APP_SECRET = "9c8478ca8fea4c3ba2014a7ced03c46e";
    private static String accessToken;
    private static long expirationTime;
    // 声明锁
    private final ReentrantLock lock = new ReentrantLock();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private CeShiService ceShiService;
    @Autowired
    private ChatGptService chatGptService;

    @GetMapping
//    public long ex(HttpServletRequest request){
    public void ex(HttpServletRequest request, HttpServletResponse response) {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
//        long echostr = Long.parseLong(request.getParameter("echostr"));
        String echostr = request.getParameter("echostr");
        log.info("参数分别是，{},{},{},{},{}", signature, timestamp, nonce, echostr, request.getParameter("echostr"));

        // 1. 将token、timestamp、nonce三个参数进行字典序排序
        String[] arr = new String[]{"aqpmzngldh", timestamp, nonce};

        Arrays.sort(arr);

        // 2. 将三个参数字符串拼接成一个字符串进行sha1加密
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String ciphertext = null;
        try {
            ciphertext = DatatypeConverter.printHexBinary(md.digest(content.toString().getBytes("UTF-8"))).toLowerCase();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 3. 将加密后的字符串与signature对比,如果相同,则签名验证通过
        if (ciphertext.equals(signature)) {
            log.info("签名验证通过");
            try {
                //方法返回值写成long，然后return long也行，不过因为瑞吉外卖前端接收到long的时候精度有问
                // 题，通过自定义消息转换器换成了string，也就导致这里如果返回long也会被转成string，这样
                // 微信的token验证失败，所以这样写也是可以的
                response.getWriter().write(echostr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.error("签名验证失败");

        }

    }

    @RequestMapping("/access_token")
    public String access_token() {
        if (lock.tryLock()) {
            try {
                if (accessToken == null || System.currentTimeMillis() >= expirationTime) {
                    String s = refreshAccessToken();
                    return s;
                }
            } finally {
                lock.unlock(); // 释放锁
            }
        }

        // 使用Access Token进行其他操作，例如调用微信API
        System.out.println("Access Token: " + accessToken);

        long remainingTime = (expirationTime - System.currentTimeMillis()) / 1000; // 剩余时间（秒）
        System.out.println("剩余过期时间: " + remainingTime + " 秒");
        return accessToken;
    }

    private String refreshAccessToken() {
        try {
            // 构建获取Access Token的URL
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APP_ID + "&secret=" + APP_SECRET;
            System.out.println(url);
            // 发送GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            if (response.isOk()) {
                String responseBody = response.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                accessToken = (String) map.get("access_token");
                Integer expiresIn = (Integer) map.get("expires_in");// 过期时间（秒）
                expirationTime = System.currentTimeMillis() + expiresIn * 1000; // 转为毫秒
            } else {
                // 处理错误
                System.err.println("Failed to refresh Access Token: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            return accessToken;
    }

    @PostMapping
    public void message(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream input = request.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] by = new byte[1024];
        int length = 0;
        while ((length = input.read(by)) != -1) {
            output.write(by, 0, length);
        }
        output.close();
        String xmlData = new String(output.toByteArray(), "UTF-8");
        // 解析XML数据包
        Map<String, String> message = parseXmlData(xmlData);
        System.out.println(message);

        String str = "";
        Message message1 = new Message();
        //已经获取到用户发送的消息，接下来我们构建xml回复
        if ("text".equals(message.get("MsgType"))) {
            //不直接回复了，有些用户5秒内问的问题响应不了，我们直接回复，然后调用客服接口发消息
            //在用户发送消息后立即展示正在输入中
            String fromUserName = message.get("FromUserName");
            String access_token = ceShiService.access_token();
            String url3="https://api.weixin.qq.com/cgi-bin/message/custom/typing?access_token="+access_token;
            HashMap<String, Object> hashMap3 = new HashMap<>();
            hashMap3.put("touser",fromUserName);
            hashMap3.put("command","Typing");

            String jsonString111 = JSONUtil.toJsonStr(hashMap3);
            HttpUtil.createPost(url3).body(jsonString111, "application/json").execute();


            response.setContentType("text/html;charset=utf-8");
            PrintWriter writer = response.getWriter();
            // 将XML数据写入响应
            writer.print(str);
            // 关闭PrintWriter
            writer.close();

//            String fromUserName = message.get("FromUserName");
            String chat = chatGptService.chat(fromUserName, message.get("Content"));
//            String access_token = ceShiService.access_token();
//            System.out.println(access_token);
            String url="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="+access_token;
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("touser",fromUserName);
            hashMap.put("msgtype","text");
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("content",chat);
            hashMap.put("text",hashMap1);
            String jsonString = JSONUtil.toJsonStr(hashMap);
            HttpResponse execute = HttpUtil.createPost(url).body(jsonString, "application/json").execute();
            if (execute.isOk()) {
                System.out.println(execute+"++++"+execute.isOk());
            } else {
                // 处理错误

            message1.setContent("内容大小超出限制，请重试...");
            message1.setCreateTime(System.currentTimeMillis());
            message1.setFromUserName(message.get("ToUserName"));
            message1.setMsgType("text");
            message1.setToUserName(message.get("FromUserName"));
            str = Message.objectToXml(message1);
            }

        } else if ("image".equals(message.get("MsgType"))) {
            message1.setCreateTime(System.currentTimeMillis());
            message1.setFromUserName(message.get("ToUserName"));
            message1.setMsgType("image");
            message1.setToUserName(message.get("FromUserName"));
            // 设置图片素材的MediaId
            Message.Image image = new Message.Image();
            image.setMediaId(message.get("MediaId"));
            message1.setImage(image);
            str = Message.objectToXml(message1);
            System.out.println("--------------" + str + "-----------");
        } else if ("voice".equals(message.get("MsgType"))) {
            message1.setCreateTime(System.currentTimeMillis());
            message1.setFromUserName(message.get("ToUserName"));
            message1.setMsgType("voice");
            message1.setToUserName(message.get("FromUserName"));
            // 设置视频的MediaId
            Message.Voice voice = new Message.Voice();
            voice.setMediaId(message.get("MediaId"));
            message1.setVoice(voice);
            str = Message.objectToXml(message1);
            System.out.println("--------------" + str + "-----------");
        } else if ("subscribe".equals(message.get("Event"))) {
            String s = message.get("FromUserName");
            String wx = "wxgz_";
            // 对该FromUserName也就是发送方的openid进行存储并自增
            redisTemplate.opsForValue().increment(wx + s);
            //获取存储的次数
            String num = redisTemplate.opsForValue().get(wx + s);
            //响应给用户提示第几次关注公众号
            String ss = redisTemplate.opsForValue().get("wxfs");
            Integer integer = Integer.valueOf(ss)+1;
            redisTemplate.opsForValue().set("wxfs", integer+"");

            message1.setContent("感谢你成为公众号第"+integer+"位粉丝，这是你第" + num + "次关注该公众号，该公众号已交AI智能管理，可以回答你的基本问题,加入管理员可获得更多权限");
            message1.setCreateTime(System.currentTimeMillis());
            message1.setFromUserName(message.get("ToUserName"));
            message1.setMsgType("text");
            message1.setToUserName(message.get("FromUserName"));
            str = Message.objectToXml(message1);
            String guanzhu = "   用户" + s + "已关注公众号";
            Set<String> members = redisTemplate.opsForSet().members("guanli");
            for (String value : members) {
                String[] parts = value.split("---"); // 按照---分隔符分割元素
                if (parts.length == 2 && parts[1].equals("1")) {
                    mailUtil.send("", parts[0], "【关注公众号提醒】", guanzhu, Collections.singletonList(""));
                }
            }
        }else if ("unsubscribe".equals(message.get("Event"))) {
            String ss = redisTemplate.opsForValue().get("wxfs");
            int integer = Integer.valueOf(ss)-1;
            redisTemplate.opsForValue().set("wxfs", integer+"");

        } else if ("LOCATION".equals(message.get("Event"))) {
            String Longitude = message.get("Longitude");
            String Latitude = message.get("Latitude");
            String formatted_address = null;
            try {
                // 构建百度地图逆向地理编码的URL
                String url = "https://api.map.baidu.com/reverse_geocoding/v3/?ak=iNrukKkdcDcb2gmStdpyKAnn1Ivpsy9A&output=json&coordtype=wgs84ll&location=" + Latitude + "," + Longitude;

                // 发送GET请求
                HttpResponse response1 = HttpUtil.createGet(url).execute();

                if (response1.isOk()) {
                    String responseBody = response1.body();
                    Map<String, Object> map = JSONUtil.parseObj(responseBody);
                    Object result = map.get("result");
                    JSONObject entries = JSONUtil.parseObj(result);
                    formatted_address = (String) entries.get("formatted_address");
                    String address = "   用户" + message.get("FromUserName") + "已进入公众号，当前所在地理位置为" + formatted_address;

                    Set<String> members = redisTemplate.opsForSet().members("guanli");
                    for (String value : members) {
                        String[] parts = value.split("---"); // 按照---分隔符分割元素
                        if (parts.length == 2 && parts[1].equals("1")) {
                            mailUtil.send("", parts[0], "【已授权位置用户提醒】", address, Collections.singletonList(""));
                        }
                    }
                } else {
                    // 处理错误
                    System.err.println("检查百度地图key余额" + response1.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(("CLICK".equals(message.get("Event")))&&("meituanlingquan".equals(message.get("EventKey")))){

            String htmlContent ="<a href=\"https://offsiteact.meituan.com/act/cps/promotion?\n" +
                    "p=d3e55e27b518484eb6e2e5a892980bab\"> 美团外卖商家红包</a>\n \n" +
                    "<a href=\"https://click.meituan.com/t?t=1&c=2&p=ndABdb5zidEL\"> 美团外卖每日红包</a>\n \n" +
                    "<a href=\"https://click.meituan.com/t?t=1&c=2&p=JoyFTr5z_oKQ\"> 美团外卖天天神券</a>";

            message1.setContent(htmlContent);
            message1.setCreateTime(System.currentTimeMillis());
            message1.setFromUserName(message.get("ToUserName"));
            message1.setMsgType("text");
            message1.setToUserName(message.get("FromUserName"));
            str = Message.objectToXml(message1);
        }

        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        // 将XML数据写入响应
        writer.print(str);
        // 关闭PrintWriter
        writer.close();
    }

    // 解析XML数据包并将其转换成Map
    public Map<String, String> parseXmlData(String xmlData) throws Exception {
        Map<String, String> message = new HashMap<>();

        // 使用Hutool的XmlUtil将XML字符串解析为Document对象
        Document document = XmlUtil.parseXml(xmlData);

        // 获取根节点
        Element rootElement = document.getDocumentElement();
        message.put(rootElement.getNodeName(), rootElement.getTextContent());

        // 获取子元素
        NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                message.put(childElement.getNodeName(), childElement.getTextContent());
            }
        }
        return message;
    }


    /**
     * 上面的方式有点麻烦，用这种
     * 但是这种也有不如上面地方的，这个接收参数的时候要一直在实体类加，上面那个不用
     * 上面那个只用把返回参数往实体类里加就好了
     *
     * @param
     * @return
     * @throws Exception
     */
//    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
//    public Object  message(@RequestBody Message requestMessage) throws Exception {
//        System.out.println("post方法入参："+requestMessage);
//        String fromUserName = requestMessage.getFromUserName();
//        String toUserName = requestMessage.getToUserName();
//
//        //新建一个响应对象
//        Message responseMessage = new Message();
//        if ("text".equals(requestMessage.getMsgType())){
//        //消息来自谁
//        responseMessage.setFromUserName(toUserName);
//        //消息发送给谁
//        responseMessage.setToUserName(fromUserName);
//        //消息类型，返回的是文本
//        responseMessage.setMsgType("text");
//        //消息创建时间，当前时间就可以
//        responseMessage.setCreateTime(System.currentTimeMillis());
//        //这个是响应消息内容，直接复制收到的内容做演示，甚至整个响应对象都可以直接使用原请求参数对象，只需要换下from和to就可以了哈哈哈
//        responseMessage.setContent(requestMessage.getContent());
//        }else if("image".equals(requestMessage.getMsgType())){
//            responseMessage.setFromUserName(toUserName);
//            responseMessage.setToUserName(fromUserName);
//            responseMessage.setMsgType("image");
//            responseMessage.setCreateTime(System.currentTimeMillis());
//
//            // 设置图片素材的MediaId
//            responseMessage.setMediaId(requestMessage.getMediaId());
//        }
//        return responseMessage;
//    }
    @RequestMapping("/delete")
    public void delete() {
        try {
            String access_token = ceShiService.access_token();
            // 构建自定义菜单删除url
            String url = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=" + access_token;

            // 发送GET请求
            HttpResponse response1 = HttpUtil.createGet(url).execute();

            if (response1.isOk()) {
                String responseBody = response1.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                Boolean result = "ok".equals(map.get("errmsg"));
                System.out.println(result);
            } else {
                // 处理错误
                System.err.println("检查百度地图key余额" + response1.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/add")
    public void add() {
        try {
            String access_token = ceShiService.access_token();
            // 构建新增自定义菜单url
            String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + access_token;
            //文档上写的发送的是json数据，先map添加数据，再转换成json字符串
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<HashMap<String, Object>> buttons = new ArrayList<>();

            HashMap<String, Object> button1 = new HashMap<>();
            button1.put("type", "click");
            button1.put("name", "美团领券");
            button1.put("key", "meituanlingquan");

            HashMap<String, Object> button2 = new HashMap<>();
            button2.put("name", "开发服务");
            ArrayList<HashMap<String, Object>> subButtons = new ArrayList<>();
            HashMap<String, Object> subButton1 = new HashMap<>();
            subButton1.put("type", "view");
            subButton1.put("name", "匿名群聊");
            subButton1.put("url", "https://www.qianyekeji.cn/");
            subButtons.add(subButton1);

            HashMap<String, Object> button3 = new HashMap<>();
            button3.put("name", "AI");
            ArrayList<HashMap<String, Object>> subButtons3 = new ArrayList<>();
            HashMap<String, Object> subButton3 = new HashMap<>();
            subButton3.put("type", "view");
            subButton3.put("name", "chatgpt");
            subButton3.put("url", "https://chat.openai.com/");
            HashMap<String, Object> subButton4 = new HashMap<>();
            subButton4.put("type", "view");
            subButton4.put("name", "claude");
            subButton4.put("url", "https://claude.ai/");
            subButtons3.add(subButton3);
            subButtons3.add(subButton4);


            button2.put("sub_button", subButtons);
            button3.put("sub_button", subButtons3);

            buttons.add(button1);
            buttons.add(button3);
            buttons.add(button2);

            map.put("button", buttons);
            String jsonString = JSONUtil.toJsonStr(map);
            // 发送POST请求
            HttpResponse response1 = HttpUtil.createPost(url).body(jsonString, "application/json").execute();

            if (response1.isOk()) {
                String responseBody = response1.body();
                Map<String, Object> map1 = JSONUtil.parseObj(responseBody);
                Boolean result = "ok".equals(map1.get("errmsg"));
                System.out.println(result);
            } else {
                // 处理错误
                System.err.println("错误。。。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/signature/{getNonceStr}/{timestamp}")
    public String getSignature(@PathVariable("getNonceStr") String getNonceStr, @PathVariable("timestamp") String timestamp){
        //生成签名需要jsapi_ticket，我们先获取jsapi_ticket
        //获取签名之前要先获取access_token
        String s = ceShiService.access_token();
        try {
            // 构建获取Access Token的URL
            String url="https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+s+"&type=jsapi";
            // 发送GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            if (response.isOk()) {
                String responseBody = response.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                String ticket = (String) map.get("ticket");
                String uri="http://qianyekeji.cn:8089/front/page/wx.html";
                String signature = generateSignature(ticket, getNonceStr, timestamp, uri);
                System.out.println("生成的签名：" + signature);
                return signature;
            } else {
                // 处理错误
                System.err.println("Failed.............." + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateSignature(String jsapiTicket, String nonceStr, String timestamp, String url) {
        try {
            // 步骤1：将参数按字典序排序
            Map<String, String> paramMap = new TreeMap<>();
            paramMap.put("jsapi_ticket", jsapiTicket);
            paramMap.put("noncestr", nonceStr);
            paramMap.put("timestamp", timestamp);
            paramMap.put("url", url);

            List<String> keyList = new ArrayList<>(paramMap.keySet());
            Collections.sort(keyList);

            String string1 = "";
            for (String key : keyList) {
                string1 = string1.concat(key + "=" + paramMap.get(key) + "&");
            }
            string1 = string1.substring(0, string1.length() - 1);

            // 步骤2：对string1进行SHA-1加密
            return DigestUtils.sha1Hex(string1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}