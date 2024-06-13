package cn.qianyekeji.wechatsdk.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.wechatsdk.common.R;
import cn.qianyekeji.wechatsdk.entity.WxVoice;
import cn.qianyekeji.wechatsdk.service.*;
import cn.qianyekeji.wechatsdk.utils.AudioUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.qianyekeji.wechatsdk.utils.AudioUtils.transferAudioSilk;

@Controller
public class WechatSdkController {
    @Autowired
    private Wx_voiceService wx_voiceService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Value("${wecahtsdk.name}")
    private String name;
    @Autowired
    private ChatgptService chatGptService;
    @Autowired
    private CsdnNewsService csdnNewsService;
    @Value("${wecahtsdk.token}")
    private String token;
    @Autowired
    private CountService countService;
    @Value("${wecahtsdk.token_code}")
    private String token_code;
    @Autowired
    private CodeMessageService codeMessageService;
    @Autowired
    private AirfoneService airfoneService;

    static {
        String url = "http://127.0.0.1:8888/api/";
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", 1001);
        hashMap.put("protocol", 2);
        hashMap.put("url", "http://127.0.0.1:8089/api/setCallback");
        String jsonString = JSONUtil.toJsonStr(hashMap);
        HttpUtil.createPost(url).body(jsonString, "application/json").execute();
    }

    @SneakyThrows
    @PostMapping("/api/setCallback")
    public void setCallbackUrl(@RequestBody Map<String, Object> data) throws Exception {
        Map<String, String> data1 = (Map<String, String>) data.get("data");
        System.out.println("看一下这个data的数据"+data1);
        String type = String.valueOf(data1.get("type"));
        System.out.println("这个值是："+type);

        String from1 = data1.get("from");
        String to1 = data1.get("to");
        String chatroomMemberInfoJson1 = JSONUtil.toJsonStr(data1.get("chatroomMemberInfo"));
        // 将JSON字符串解析为Map
        JSONObject chatroomMemberInfo1 = JSONUtil.parseObj(chatroomMemberInfoJson1);
        String belongChatroomNickName1 = (String) chatroomMemberInfo1.get("belongChatroomNickName");
        if (belongChatroomNickName1 != null) {
            redisTemplate.opsForHash().put("wx_voice", belongChatroomNickName1, from1);
        }
        //获取的数据根据格式来看有时候不能通过上面那种方式存入redis，所以这个if中再做补充，这种情况是别人在群里中发送消息，自己接收到
        if (from1.endsWith("@chatroom")) {
            String url_1 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> hashMap_1 = new HashMap<>();
            hashMap_1.put("type", 30);
            hashMap_1.put("chatroomUserName", from1);
            String jsonString_1 = JSONUtil.toJsonStr(hashMap_1);
            HttpResponse response_1 = HttpUtil.createPost(url_1).body(jsonString_1, "application/json").execute();
            if (response_1.isOk()) {
                String responseBody_1 = response_1.body();
                JSONObject entries = JSONUtil.parseObj(responseBody_1);
                // 获取 encryptUserName
                String encryptUserName = entries.getJSONObject("data")
                        .getJSONObject("data")
                        .getJSONObject("profile")
                        .getJSONObject("data")
                        .getStr("nickName");
                if (encryptUserName!=null){
                redisTemplate.opsForHash().put("wx_voice", encryptUserName, from1);
                }

            }
        }
        //这种情况是自己在群里中发送消息，自己接收到
        if (to1.endsWith("@chatroom")) {
            String url_1 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> hashMap_1 = new HashMap<>();
            hashMap_1.put("type", 30);
            hashMap_1.put("chatroomUserName", to1);
            String jsonString_1 = JSONUtil.toJsonStr(hashMap_1);
            HttpResponse response_1 = HttpUtil.createPost(url_1).body(jsonString_1, "application/json").execute();
            if (response_1.isOk()) {
                String responseBody_1 = response_1.body();
                JSONObject entries = JSONUtil.parseObj(responseBody_1);
                // 获取 encryptUserName
                String encryptUserName = entries.getJSONObject("data")
                        .getJSONObject("data")
                        .getJSONObject("profile")
                        .getJSONObject("data")
                        .getStr("nickName");
                if (encryptUserName!=null) {
                    redisTemplate.opsForHash().put("wx_voice", encryptUserName, to1);
                }
            }
        }
        //根据文档type是34的时候，是语音消息
        if ("34".equals(type)) {
            System.out.println("这个是语音消息");
            String from = data1.get("from");
            String to = data1.get("to");
            // 获取chatroomMemberInfo字段，它是一个JSON字符串,就不能像上面那样获取了
            String chatroomMemberInfoJson = JSONUtil.toJsonStr(data1.get("chatroomMemberInfo"));
            // 将JSON字符串解析为Map
            JSONObject chatroomMemberInfo = JSONUtil.parseObj(chatroomMemberInfoJson);
            String belongChatroomNickName = (String) chatroomMemberInfo.get("belongChatroomNickName");
            handleAudioMsg(data1, from, to, belongChatroomNickName);

        } else if ("1".equals(type)) {
            String from = data1.get("from");
            String to = data1.get("to");
            String content = data1.get("content");
            System.out.println("看一下这个是" + from + "发给" + to + "的消息" + content);
            if ("filehelper".equals(to)) {
                if (content.split("=").length == 3) {
                    //这时候确定是想发送语音消息了
                    // 音色=1群名=文字
                    // 音色=2微信号=文字
                    String[] split = content.split("=");
                    String yinse = split[0];
                    String address = split[1];
                    String wenzi = split[2];

                    //按照自己设计的规则，1表示想发送到群聊中,2发送到某个人
                    if ("1".equals(address.charAt(0) + "")) {
                        String substring = address.substring(1);
                        switch (yinse) {
                            case "懒羊羊":
                                String url = "https://api.lolimi.cn/API/yyhc/lyy.php";
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("msg", wenzi);
                                HttpResponse response = HttpUtil.createPost(url).form(hashMap).execute();

                                if (response.isOk()) {
                                    String responseBody = response.body();
                                    Map<String, Object> map = JSONUtil.parseObj(responseBody);
                                    String music = (String) map.get("music");
                                    System.out.println("文字转语音后的wav文件链接是：" + music);
                                    // 下载音乐文件到本地指定目录
                                    String localFilePath = downloadFile(music, "F:\\\\yuyin\\\\");
                                    String fileName = localFilePath.substring(localFilePath.lastIndexOf("\\") + 1);
                                    //这时候给wav转成silk，然后通过wechatsdk进行发送
                                    String s = AudioUtils.transferAudioSilk("F:\\\\yuyin\\\\", fileName, false);
                                    System.out.println("看一下这个s：" + s);

                                    String qunId = (String) redisTemplate.opsForHash().get("wx_voice", substring);
                                    System.out.println("查询到的群id是：" + qunId);
                                    String url_2 = "http://127.0.0.1:8888/api/";
                                    HashMap<String, Object> hashMap_2 = new HashMap<>();
                                    hashMap_2.put("type", 10014);
                                    hashMap_2.put("userName", qunId);
                                    hashMap_2.put("filePath", s);
                                    String jsonString_2 = JSONUtil.toJsonStr(hashMap_2);
                                    HttpUtil.createPost(url_2).body(jsonString_2, "application/json").execute();


                                }


                                break;
                            case "哈哈":

                                break;
                        }
                    } else if ("2".equals(address.charAt(0) + "")) {
                        String substring = address.substring(1);
                        switch (yinse) {
                            case "懒羊羊":
                                String url = "https://api.lolimi.cn/API/yyhc/lyy.php";
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("msg", wenzi);
                                HttpResponse response = HttpUtil.createPost(url).form(hashMap).execute();

                                if (response.isOk()) {
                                    String responseBody = response.body();
                                    Map<String, Object> map = JSONUtil.parseObj(responseBody);
                                    String music = (String) map.get("music");
                                    System.out.println("文字转语音后的wav文件链接是：" + music);
                                    // 下载音乐文件到本地指定目录
                                    String localFilePath = downloadFile(music, "F:\\\\yuyin\\\\");
                                    String fileName = localFilePath.substring(localFilePath.lastIndexOf("\\") + 1);
                                    //这时候给wav转成silk，然后通过wechatsdk进行发送
                                    String s = AudioUtils.transferAudioSilk("F:\\\\yuyin\\\\", fileName, false);
                                    System.out.println("看一下这个s：" + s);

                                    //获取准备发送到这个人的wxid，这里先根据微信号查询出具体的wxid
                                    String url_1 = "http://127.0.0.1:8888/api/";
                                    HashMap<String, Object> hashMap_1 = new HashMap<>();
                                    hashMap_1.put("type", 43);
                                    hashMap_1.put("keyword", substring);
                                    String jsonString_1 = JSONUtil.toJsonStr(hashMap_1);
                                    HttpResponse response_1 = HttpUtil.createPost(url_1).body(jsonString_1, "application/json").execute();
                                    if (response_1.isOk()) {
                                        String responseBody_1 = response_1.body();
                                        JSONObject entries = JSONUtil.parseObj(responseBody_1);
                                        // 获取 encryptUserName
                                        String encryptUserName = entries.getJSONObject("data")
                                                .getJSONObject("data")
                                                .getStr("encryptUserName");
                                        System.out.println("查询到的微信号：" + encryptUserName);


                                        String url_2 = "http://127.0.0.1:8888/api/";
                                        HashMap<String, Object> hashMap_2 = new HashMap<>();
                                        hashMap_2.put("type", 10014);
                                        hashMap_2.put("userName", encryptUserName);
                                        hashMap_2.put("filePath", s);
                                        String jsonString_2 = JSONUtil.toJsonStr(hashMap_2);
                                        HttpUtil.createPost(url_2).body(jsonString_2, "application/json").execute();
                                    }

                                }


                                break;
                            case "哈哈":

                                break;
                        }

                    }
                } else if (content.split("=").length == 4) {
//                    语音转发=发送语音人的微信号=要发送到人的微信号=1
                    String[] split = content.split("=");
                    String biao = split[0];
                    String from_wx = split[1];
                    String to_wx = split[2];
                    String number = split[3];
                    if ("语音转发".equals(biao)) {
                        //确定要进行语音转发了，这时候我们根据传入的number判断要转发哪个语音，并取出silk文件，然后发送给to_wx
                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type", 43);
                        map.put("keyword", from_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");

                            HashMap<String, Object> map_1 = new HashMap<>();
                            map_1.put("type", 43);
                            map_1.put("keyword", to_wx);
                            String jsonString44 = JSONUtil.toJsonStr(map_1);
                            // 发送POST请求
                            HttpResponse response4 = HttpUtil.createPost(url).body(jsonString44, "application/json").execute();
                            if (response4.isOk()) {
                                String body4 = response4.body();
                                JSONObject entries4 = JSONUtil.parseObj(body4);
                                String jsonObject4 = entries4.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                                System.out.println("提取人微信号" + jsonObject);
                                System.out.println("发送人微信号" + jsonObject4);
                                QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                                //下面第二个eq主要是为了防止本机登录了多个微信，而他们有相同好友都给其发送了语音，这时候不加就不能区分
                                objectQueryWrapper.eq("from_wx", jsonObject)
                                        .eq("to_wx", "wxid_mxx11pv88oj422")
                                        .orderByDesc("times");

                                List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                                WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                                String address = wxVoice.getAddress();
                                System.out.println("当前的语音聊天数据是：" + address);

                                String url_2 = "http://127.0.0.1:8888/api/";
                                HashMap<String, Object> map_2 = new HashMap<>();
                                map_2.put("type", 10014);
                                map_2.put("userName", jsonObject4);
                                map_2.put("filePath", address);
                                String jsonString444 = JSONUtil.toJsonStr(map_2);
                                // 发送POST请求
                                HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();


                            }
                        }
                    } else if ("人群".equals(biao)) {
                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type", 43);
                        map.put("keyword", from_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                            System.out.println("提取人微信号" + jsonObject);

                            QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                            //下面第二个eq主要是为了防止本机登录了多个微信，而他们有相同好友都给其发送了语音，这时候不加就不能区分
                            objectQueryWrapper.eq("from_wx", jsonObject)
                                    .eq("to_wx", "wxid_mxx11pv88oj422")
                                    .orderByDesc("times");

                            List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                            WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                            String address = wxVoice.getAddress();
                            System.out.println("当前的语音聊天数据是：" + address);

                            String qunId = (String) redisTemplate.opsForHash().get("wx_voice", to_wx);
                            String url_2 = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map_2 = new HashMap<>();
                            map_2.put("type", 10014);
                            map_2.put("userName", qunId);
                            map_2.put("filePath", address);
                            String jsonString444 = JSONUtil.toJsonStr(map_2);
                            // 发送POST请求
                            HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();

                        }
                    } else if ("群人".equals(biao)) {
                        String qunId = (String) redisTemplate.opsForHash().get("wx_voice", from_wx);

                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type", 43);
                        map.put("keyword", to_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                            System.out.println("提取人微信号" + jsonObject);

                            QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                            objectQueryWrapper.eq("from_wx", qunId)
                                    .eq("to_wx", "wxid_o42elvr0ggen22")
                                    .orderByDesc("times");

                            List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                            WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                            String address = wxVoice.getAddress();
                            System.out.println("当前的语音聊天数据是：" + address);


                            String url_2 = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map_2 = new HashMap<>();
                            map_2.put("type", 10014);
                            map_2.put("userName", jsonObject);
                            map_2.put("filePath", address);
                            String jsonString444 = JSONUtil.toJsonStr(map_2);
                            // 发送POST请求
                            HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();
                        }

                    }
                }
            }else{
                if (from.endsWith("@chatroom")){
                    int newlineIndex = content.indexOf('\n');
                    //按照格式，肯定存在一个换行符
                    //并且在使用机器人的时候，必须@机器人名才可以,对这种消息我们才做处理
                    if (newlineIndex != -1) {
                        String substring = content.substring(0, newlineIndex);
                        int lastColonIndex = substring.lastIndexOf(':');
                        String newStr = content.substring(0, lastColonIndex);
                        String substring1 = content.substring(newlineIndex + 1);
                        System.out.println("在当前群聊中，用户："+newStr+"发送了消息，具体内容是："+substring1);
                        JSONObject entries = JSONUtil.parseObj(data1);
                        String nickName = entries.getJSONObject("chatroomMemberInfo")
                                .getStr("nickName");
                        countService.addCount(from,nickName);

                        if (substring1.trim().contains("@"+name)){
                            String message = StrUtil.trim(substring1.replace("@" + name, "").replaceAll("\\s+", ""));
                            handleMessage(nickName,from,message,newStr);

                        }


                    }
                }else{
                    String str = JSONUtil.parseObj(data1).getJSONObject("talkerInfo").getStr("nickName");
                    String chatRoom = (String)redisTemplate.opsForHash().get("a_route", from);
                    String url_2 = "http://127.0.0.1:8888/api/";
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("type", 10009);
                    map.put("userName", chatRoom);
                    map.put("msgContent", str+"对"+name+"说："+content);
                    String jsonString = JSONUtil.toJsonStr(map);
                    // 发送POST请求
                    HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
                }
            }
        }else if ("10002".equals(type)) {
            String user = handleTextMsg(data1);
            if (!user.isEmpty()){
                String[] split = user.split("=");
                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map = new HashMap<>();
                map.put("type", 10009);
                map.put("userName", split[1]);
                map.put("msgContent", "掌声欢迎："+split[0]+" 加入本群聊!");
                String jsonString = JSONUtil.toJsonStr(map);
                // 发送POST请求
                HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();

            }
        }else if ("37".equals(type)) {
            Map<String, String> stringStringMap = handleFriendMsg(data1);
            String encryptusername= stringStringMap.get("encryptusername");
            String ticket= stringStringMap.get("ticket");
            String scene= stringStringMap.get("scene");

            String url_3 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map3 = new HashMap<>();
            map3.put("type", 10035);
            map3.put("encryptUserName", encryptusername);
            map3.put("ticket", ticket);
            map3.put("scene", Integer.parseInt(scene));
            String jsonString3 = JSONUtil.toJsonStr(map3);
            // 发送POST请求
            HttpUtil.createPost(url_3).body(jsonString3, "application/json").execute();

        }else if ("3".equals(type)) {
//            Map<String, String> stringStringMap = handleFriendMsg(data1);
//            String encryptusername= stringStringMap.get("encryptusername");
//            String ticket= stringStringMap.get("ticket");
//            String scene= stringStringMap.get("scene");
//
//            String url_3 = "http://127.0.0.1:8888/api/";
//            HashMap<String, Object> map3 = new HashMap<>();
//            map3.put("type", 10035);
//            map3.put("encryptUserName", encryptusername);
//            map3.put("ticket", ticket);
//            map3.put("scene", Integer.parseInt(scene));
//            String jsonString3 = JSONUtil.toJsonStr(map3);
//            // 发送POST请求
//            HttpUtil.createPost(url_3).body(jsonString3, "application/json").execute();

        }
    }

    /**
     * 下载文件并保存到本地指定目录
     *
     * @param fileURL 远程文件 URL
     * @param saveDir 本地保存目录
     * @return 本地文件路径
     * @throws Exception
     */
    public static String downloadFile(String fileURL, String saveDir) throws Exception {
        URL url = new URL(fileURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        InputStream inputStream = con.getInputStream();
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        String saveFilePath = saveDir + fileName;
        // 检查目录是否存在，如果不存在则创建
        Path directory = Paths.get(saveDir);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path filePath = Paths.get(saveFilePath);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
//        Files.copy(inputStream, Paths.get(saveFilePath));
        inputStream.close();
        return saveFilePath;
    }

    /**
     * 解析语音的xml提取信息
     * @param data
     * @param from
     * @param to
     * @param belongChatroomNickName
     * @throws Exception
     */
    private void handleAudioMsg(Map<String, String> data, String from, String to, String belongChatroomNickName) throws Exception {
        String xmlContent = data.get("content");
        System.out.println("看一下值1：" + xmlContent);
        // 微信群发言是有前缀的，这里需要去掉
        String[] split = xmlContent.split(":\n");
        xmlContent = split.length > 1 ? split[1] : xmlContent;
        System.out.println("看一下值2：" + xmlContent);

        // 使用Hutool的XmlUtil解析XML
        Document doc = XmlUtil.parseXml(xmlContent);
        Element msgElem = doc.getDocumentElement();  // 获取根元素
        Node voicemsgNode = msgElem.getElementsByTagName("voicemsg").item(0);
        if (voicemsgNode != null && voicemsgNode.getNodeType() == Node.ELEMENT_NODE) {
            Element voicemsgElem = (Element) voicemsgNode;
            String aeskey = voicemsgElem.getAttribute("aeskey");
            String fileid = voicemsgElem.getAttribute("voiceurl");
            // 下载音频文件
            downloadAudioFile(fileid, aeskey, from, to, belongChatroomNickName);
        } else {
            System.out.println("No voicemsg element found.");
        }
    }

    /**
     * 下载语音，存储语音
     * @param fileid
     * @param aeskey
     * @param from
     * @param to
     * @param belongChatroomNickName
     */
    private void downloadAudioFile(String fileid, String aeskey, String from, String to, String belongChatroomNickName) {

        String url = "http://127.0.0.1:8888/api/";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", 66);
        requestBody.put("fileid", fileid);
        requestBody.put("aeskey", aeskey);
        requestBody.put("fileType", 15);
        requestBody.put("savePath", "F:\\yuyin\\zhuan\\" + aeskey + ".slik");
        String jsonString = JSONUtil.toJsonStr(requestBody);
        HttpUtil.createPost(url).body(jsonString, "application/json").execute();

        //为了方便语音转发，这时候给语音数据存储起来
        System.out.println("谁发送的语音" + from);
        System.out.println("谁接收的语音" + to);
        WxVoice wx_voice = new WxVoice();
        wx_voice.setFromWx(from);
        wx_voice.setToWx(to);
        wx_voice.setLiao(belongChatroomNickName);
        wx_voice.setAddress("F:\\\\yuyin\\zhuan\\\\" + aeskey + ".slik");
        wx_voice.setTimes(System.currentTimeMillis() / 1000 + "");
        wx_voiceService.save(wx_voice);

//        redisTemplate.opsForHash().put("wx_voice",belongChatroomNickName,from);
    }

    /**
     * 解析新人入群的xml
     * @param data
     * @throws Exception
     */
    private String handleTextMsg(Map<String, String> data) throws Exception {
        String xmlContent = data.get("content");
        // 微信群发言是有前缀的,这里需要去掉
        String[] split = xmlContent.split(":\n");
        xmlContent = split.length > 1 ? split[1] : xmlContent;

        // 使用Hutool的XmlUtil解析XML
        Document doc = XmlUtil.parseXml(xmlContent);
        Element msgElem = doc.getDocumentElement(); // 获取根元素
        Node textNode = msgElem.getElementsByTagName("text").item(0);
        String user="";
        if (textNode != null && textNode.getNodeType() == Node.ELEMENT_NODE) {
            Element textElem = (Element) textNode;
            String textContent = textElem.getTextContent();
            System.out.println("新用户: " + textContent);
//            你邀请"千夜"加入了群聊
//            "千夜"通过扫描你分享的二维码加入群聊
            if ((textContent.trim().endsWith("\"加入了群聊"))||(textContent.trim().endsWith("\"通过扫描你分享的二维码加入群聊"))){
                // 定义正则表达式模式，匹配双引号包裹的内容
                String regex = "\"(.*?)\"";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(textContent);
                if (matcher.find()) {
                    user=matcher.group(1)+"="+split[0];
                }
            }
        }
        return user;
    }

    /**
     * 解析陌生人添加自己为好友的xml
     * @param data
     * @return
     * @throws Exception
     */
    private Map<String, String> handleFriendMsg(Map<String, String> data) throws Exception {
        String xmlContent = data.get("content");
        Document doc = XmlUtil.parseXml(xmlContent);
        Element msgElem = doc.getDocumentElement(); // 获取根元素
        String encryptusername = msgElem.getAttribute("encryptusername");
        String ticket = msgElem.getAttribute("ticket");
        String scene = msgElem.getAttribute("scene");
        Map<String, String> result = new HashMap<>();
        result.put("encryptusername", encryptusername);
        result.put("ticket", ticket);
        result.put("scene", scene);
        return result;
    }

    private void handleMessage(String name,String chatRoom,String message,String newStr) throws Exception {
        System.out.println("看一下这个是谁发的消息"+name);
        System.out.println("看一下在哪个群聊中发的消息"+chatRoom);
        System.out.println("看一下这个消息内容是"+message);
        String value = (String) redisTemplate.opsForHash().get(chatRoom, name);
        if (value==null){
            value="";
        }
//      对@机器人的消息集中在这个方法中做出处理
        if (message.contains("天气")){
            String url_1 = "https://chatbot.weixin.qq.com/openapi/sign/"+token;
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("userid",1);
            String jsonString1 = JSONUtil.toJsonStr(hashMap1);
            HttpResponse execute = HttpUtil.createPost(url_1).body(jsonString1, "application/json").execute();
            if (execute.isOk()) {
                String responseBody = execute.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                String signature = (String)map.get("signature");

                String url_2 = "https://chatbot.weixin.qq.com/openapi/aibot/"+token;
                HashMap<String, Object> hashMap2 = new HashMap<>();
                hashMap2.put("signature",signature);
                hashMap2.put("query",message);
                String jsonString2 = JSONUtil.toJsonStr(hashMap2);
                HttpResponse execute2 = HttpUtil.createPost(url_2).body(jsonString2, "application/json").execute();
                if (execute2.isOk()) {
                    System.out.println(execute2);
                    String responseBody2 = execute2.body();
                    Map<String, Object> map2 = JSONUtil.parseObj(responseBody2);
                    String answer = (String)map2.get("answer");
                    String url_3 = "http://127.0.0.1:8888/api/";
                    HashMap<String, Object> map3 = new HashMap<>();
                    map3.put("type", 10009);
                    map3.put("userName", chatRoom);
                    map3.put("msgContent", "@"+name+value+" "+answer);
                    String jsonString3 = JSONUtil.toJsonStr(map3);
                    // 发送POST请求
                    HttpUtil.createPost(url_3).body(jsonString3, "application/json").execute();
                }
            }
        }else if ("今日早报".equals(message)){
            String s = csdnNewsService.csdn_to();
            System.out.println(s);
            String url_2 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", 10009);
            map.put("userName", chatRoom);
            map.put("msgContent", s);
            String jsonString = JSONUtil.toJsonStr(map);
            // 发送POST请求
            HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
        }else if (
                message.contains("白羊座")||
                message.contains("金牛座")||
                message.contains("双子座")||
                message.contains("巨蟹座")||
                message.contains("狮子座")||
                message.contains("处女座")||
                message.contains("天秤座")||
                message.contains("天蝎座")||
                message.contains("射手座")||
                message.contains("摩羯座")||
                message.contains("水瓶座")||
                message.contains("双鱼座")
        ){
            String[] zodiacSigns = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
            String foundSign = null;
            for (String sign : zodiacSigns) {
                if (message.contains(sign)) {
                    foundSign = sign;
                    break;
                }
            }
            String url = "https://api.qqsuu.cn/api/dm-fortune?astro="+foundSign;
            HttpResponse response = HttpUtil.createGet(url).execute();
            if (response.isOk()) {
                String responseBody = response.body();
                // 解析响应数据
                JSONObject jsonObject = JSONUtil.parseObj(responseBody);
                JSONArray dataList = jsonObject.getJSONObject("data").getJSONArray("list");
                StringBuilder sum = new StringBuilder();
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject item = dataList.getJSONObject(i);
                    String type = item.getStr("type");
                    String content = item.getStr("content");
                    sum.append(type).append("：").append(content).append("\n");
                }
                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map = new HashMap<>();
                map.put("type", 10009);
                map.put("userName", chatRoom);
                map.put("msgContent", sum.toString());
                String jsonString = JSONUtil.toJsonStr(map);
                // 发送POST请求
                HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
            } else {
                // 处理错误
                System.err.println("查询星座错误" + response.getStatus());
            }

        }else if ("美女".equals(message)){
            String url = "https://v2.api-m.com/api/heisi";
            HttpResponse response1 = HttpUtil.createGet(url).execute();
            if (response1.isOk()) {
                String responseBody = response1.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                String imgUrl = (String)map.get("data");
                String localFilePath = downloadFile(imgUrl, "F:\\\\yuyin\\\\pic\\\\");

                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map_1 = new HashMap<>();
                map_1.put("type", 10010);
                map_1.put("userName", chatRoom);
                map_1.put("filePath", localFilePath);
                String jsonString = JSONUtil.toJsonStr(map_1);
                HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
            } else {
                // 处理错误
                System.out.println("请求图片出错");
            }
        }else if ("视频".equals(message)){
            String url = "https://api.qqsuu.cn/api/dm-xjj?type=json&apiKey=b4bd29e2d83ea412fa368e2747c8ef41";
            HttpResponse response1 = HttpUtil.createGet(url).execute();
            if (response1.isOk()) {
                String responseBody = response1.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                String videoUrl = (String)map.get("video");
                String localFilePath = downloadFile(videoUrl, "F:\\\\yuyin\\\\video\\\\");

                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map_1 = new HashMap<>();
                map_1.put("type", 10093);
                map_1.put("userName", chatRoom);
                map_1.put("filePath", localFilePath);
                String jsonString = JSONUtil.toJsonStr(map_1);
                HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
            } else {
                // 处理错误
                System.out.println("请求视频出错");
            }
        }else if (message.startsWith("http://")||message.startsWith("https://")){
            String url = "https://www.qrgpt.io/api/generate";
            HashMap<String, Object> map = new HashMap<>();
            map.put("prompt","A beautiful glacier");
            map.put("url", message);
            String jsonString = JSONUtil.toJsonStr(map);
            // 发送POST请求
            HttpResponse response = HttpUtil.createPost(url).body(jsonString, "application/json").execute();
            if (response.isOk()) {
                String responseBody = response.body();
                Map<String, Object> map_1 = JSONUtil.parseObj(responseBody);
                String image_url = (String)map_1.get("image_url");
                String localFilePath = downloadFile(image_url, "F:\\\\yuyin\\\\pic\\\\");

                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map_2 = new HashMap<>();
                map_2.put("type", 10010);
                map_2.put("userName", chatRoom);
                map_2.put("filePath", localFilePath);
                String jsonString_2 = JSONUtil.toJsonStr(map_2);
                HttpUtil.createPost(url_2).body(jsonString_2, "application/json").execute();
            } else {
                // 处理错误
                System.err.println("链接转二维码出错。。。");
            }
        }else if ("活跃度查询".equals(message)){
            HashMap hashMapR = countService.selectCount(chatRoom);
            System.out.println("看一下这个活跃度："+hashMapR);
            StringBuilder sb = new StringBuilder();
            sb.append("该群聊的活跃度如下:\n\n");

            for (Object obj : hashMapR.entrySet()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) obj;
                sb.append(entry.getKey() + ":" + entry.getValue() + "\n");
            }

            String result = sb.toString();
            System.out.println(result);
            String url_2 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", 10009);
            map.put("userName", chatRoom);
            map.put("msgContent", result);
            String jsonString = JSONUtil.toJsonStr(map);
            HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
        }else if (message.contains("接码=")&&message.split("=").length==2){
            String[] split = message.split("=");
            String name_code=split[1];
            codeMessageService.getCode(token_code,chatRoom,name_code,name,value);

        }else if (message.contains("传话筒=")&&message.split("=").length==3){
            String[] split = message.split("=");
            airfoneService.airfoneChat(split[1],split[2],chatRoom,name,value);

        }else if (message.startsWith("叫我")&&message.length()<7){
            String result = message.substring(2);
            redisTemplate.opsForHash().put(chatRoom, name, result);

            String url_2 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", 10009);
            map.put("userName", chatRoom);
            map.put("msgContent", "收到");
            String jsonString = JSONUtil.toJsonStr(map);
            HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
        }else{
            String chat = chatGptService.chat(newStr, message);
            String url_2 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", 10009);
            map.put("userName", chatRoom);
            map.put("msgContent", "@"+name+value+" "+chat);
            String jsonString = JSONUtil.toJsonStr(map);
            // 发送POST请求
            HttpUtil.createPost(url_2).body(jsonString, "application/json").execute();
        }

    }
}