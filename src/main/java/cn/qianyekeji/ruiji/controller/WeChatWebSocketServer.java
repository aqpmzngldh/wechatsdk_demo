package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.entity.WxVoice;
import cn.qianyekeji.ruiji.service.Wx_voiceService;
import cn.qianyekeji.ruiji.utils.AudioUtils;
import cn.qianyekeji.ruiji.utils.BaiduMapGeocoding;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.qianyekeji.ruiji.utils.AudioUtils.transferAudioSilk;

@Controller
public class WeChatWebSocketServer {
    @Autowired
    private Wx_voiceService wx_voiceService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

static {
    String url = "http://127.0.0.1:8888/api/";
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("type", 1001);
    hashMap.put("protocol", 2);
    hashMap.put("url", "http://127.0.0.1:8089/api/setCallback");
    String jsonString = JSONUtil.toJsonStr(hashMap);
    HttpUtil.createPost(url).body(jsonString, "application/json").execute();
}


    @PostMapping("/api/setCallback")
    public void setCallbackUrl(@RequestBody Map<String, Object> data)throws Exception {
        Map<String, String> data1 = (Map<String, String>) data.get("data");
        System.out.println("看一下这个data的数据"+data1);
        String type = String.valueOf(data1.get("type"));
        System.out.println("这个值是："+type);

        String from1 = data1.get("from");
        String chatroomMemberInfoJson1 = JSONUtil.toJsonStr(data1.get("chatroomMemberInfo"));
        // 将JSON字符串解析为Map
        JSONObject chatroomMemberInfo1 = JSONUtil.parseObj(chatroomMemberInfoJson1);
        String belongChatroomNickName1 = (String)chatroomMemberInfo1.get("belongChatroomNickName");
        if (belongChatroomNickName1!=null){
        redisTemplate.opsForHash().put("wx_voice",belongChatroomNickName1,from1);
        }
        //根据文档type是34的时候，是语音消息
        if ("34".equals(type)){
            System.out.println("这个是语音消息");
            String from = data1.get("from");
            String to = data1.get("to");
            // 获取chatroomMemberInfo字段，它是一个JSON字符串,就不能像上面那样获取了
            String chatroomMemberInfoJson = JSONUtil.toJsonStr(data1.get("chatroomMemberInfo"));
            // 将JSON字符串解析为Map
            JSONObject chatroomMemberInfo = JSONUtil.parseObj(chatroomMemberInfoJson);
            String belongChatroomNickName = (String)chatroomMemberInfo.get("belongChatroomNickName");
            handleAudioMsg(data1,from,to,belongChatroomNickName);

        }else if("1".equals(type)){
            String from = data1.get("from");
            String to = data1.get("to");
            String content = data1.get("content");
            System.out.println("看一下这个是"+from+"发给"+to+"的消息"+content);
            if ("filehelper".equals(to)){
                if (content.split("=").length == 3) {
                    //这时候确定是想发送语音消息了
                    // 音色=1群名=文字
                    // 音色=2微信号=文字
                    String[] split = content.split("=");
                    String yinse=split[0];
                    String address=split[1];
                    String wenzi=split[2];

                    //按照自己设计的规则，1表示想发送到群聊中,2发送到某个人
                    if ("1".equals(address.charAt(0)+"")){
                        String substring = address.substring(1);
                        switch (yinse){
                            case "懒羊羊":
                                String url="https://api.lolimi.cn/API/yyhc/lyy.php";
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("msg",wenzi);
                                HttpResponse response = HttpUtil.createPost(url).form(hashMap).execute();

                                if (response.isOk()) {
                                    String responseBody = response.body();
                                    Map<String, Object> map = JSONUtil.parseObj(responseBody);
                                    String music = (String)map.get("music");
                                    System.out.println("文字转语音后的wav文件链接是："+music);
                                    // 下载音乐文件到本地指定目录
                                    String localFilePath  = downloadFile(music, "F:\\\\yuyin\\\\");
                                    String fileName = localFilePath.substring(localFilePath.lastIndexOf("\\") + 1);
                                    //这时候给wav转成silk，然后通过wechatsdk进行发送
                                    String s = AudioUtils.transferAudioSilk("F:\\\\yuyin\\\\", fileName, false);
                                    System.out.println("看一下这个s："+s);

                                    String qunId = (String)redisTemplate.opsForHash().get("wx_voice", substring);
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
                    }else if ("2".equals(address.charAt(0)+"")){
                        String substring = address.substring(1);
                        switch (yinse){
                            case "懒羊羊":
                                String url="https://api.lolimi.cn/API/yyhc/lyy.php";
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("msg",wenzi);
                                HttpResponse response = HttpUtil.createPost(url).form(hashMap).execute();

                                if (response.isOk()) {
                                    String responseBody = response.body();
                                    Map<String, Object> map = JSONUtil.parseObj(responseBody);
                                    String music = (String)map.get("music");
                                    System.out.println("文字转语音后的wav文件链接是："+music);
                                    // 下载音乐文件到本地指定目录
                                    String localFilePath  = downloadFile(music, "F:\\\\yuyin\\\\");
                                    String fileName = localFilePath.substring(localFilePath.lastIndexOf("\\") + 1);
                                    //这时候给wav转成silk，然后通过wechatsdk进行发送
                                    String s = AudioUtils.transferAudioSilk("F:\\\\yuyin\\\\", fileName, false);
                                    System.out.println("看一下这个s："+s);

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
                }else if (content.split("=").length == 4) {
//                    语音转发=发送语音人的微信号=要发送到人的微信号=1
                    String[] split = content.split("=");
                    String biao=split[0];
                    String from_wx=split[1];
                    String to_wx=split[2];
                    String number=split[3];
                    if ("语音转发".equals(biao)){
                        //确定要进行语音转发了，这时候我们根据传入的number判断要转发哪个语音，并取出silk文件，然后发送给to_wx
                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type",43);
                        map.put("keyword",from_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");

                            HashMap<String, Object> map_1 = new HashMap<>();
                            map_1.put("type",43);
                            map_1.put("keyword",to_wx);
                            String jsonString44 = JSONUtil.toJsonStr(map_1);
                            // 发送POST请求
                            HttpResponse response4 = HttpUtil.createPost(url).body(jsonString44, "application/json").execute();
                            if (response4.isOk()) {
                                String body4 = response4.body();
                                JSONObject entries4 = JSONUtil.parseObj(body4);
                                String jsonObject4 = entries4.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                                System.out.println("提取人微信号"+jsonObject);
                                System.out.println("发送人微信号"+jsonObject4);
                                QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                                objectQueryWrapper.eq("from_wx", jsonObject)
                                        .eq("to_wx", "wxid_o42elvr0ggen22")
                                        .orderByDesc("times");

                                List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                                WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                                String address = wxVoice.getAddress();
                                System.out.println("当前的语音聊天数据是："+address);

                                String url_2 = "http://127.0.0.1:8888/api/";
                                HashMap<String, Object> map_2 = new HashMap<>();
                                map_2.put("type",10014);
                                map_2.put("userName",jsonObject4);
                                map_2.put("filePath",address);
                                String jsonString444 = JSONUtil.toJsonStr(map_2);
                                // 发送POST请求
                                HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();


                            }
                        }
                    }else if ("人群".equals(biao)){
                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type",43);
                        map.put("keyword",from_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                            System.out.println("提取人微信号"+jsonObject);

                            QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                            objectQueryWrapper.eq("from_wx", jsonObject)
                                    .eq("to_wx", "wxid_o42elvr0ggen22")
                                    .orderByDesc("times");

                            List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                            WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                            String address = wxVoice.getAddress();
                            System.out.println("当前的语音聊天数据是："+address);

                            String qunId = (String)redisTemplate.opsForHash().get("wx_voice", to_wx);
                            String url_2 = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map_2 = new HashMap<>();
                            map_2.put("type",10014);
                            map_2.put("userName",qunId);
                            map_2.put("filePath",address);
                            String jsonString444 = JSONUtil.toJsonStr(map_2);
                            // 发送POST请求
                            HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();

                        }
                    }else if ("群人".equals(biao)){
                        String qunId = (String)redisTemplate.opsForHash().get("wx_voice", from_wx);

                        String url = "http://127.0.0.1:8888/api/";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type",43);
                        map.put("keyword",to_wx);
                        String jsonString4 = JSONUtil.toJsonStr(map);
                        // 发送POST请求
                        HttpResponse response = HttpUtil.createPost(url).body(jsonString4, "application/json").execute();
                        if (response.isOk()) {
                            String body = response.body();
                            JSONObject entries = JSONUtil.parseObj(body);
                            String jsonObject = entries.getJSONObject("data").getJSONObject("data").getStr("encryptUserName");
                            System.out.println("提取人微信号"+jsonObject);

                            QueryWrapper<WxVoice> objectQueryWrapper = new QueryWrapper<>();
                            objectQueryWrapper.eq("from_wx", qunId)
                                    .eq("to_wx", "wxid_o42elvr0ggen22")
                                    .orderByDesc("times");

                            List<WxVoice> list = wx_voiceService.list(objectQueryWrapper);
                            WxVoice wxVoice = list.get(Integer.parseInt(number) - 1);
                            String address = wxVoice.getAddress();
                            System.out.println("当前的语音聊天数据是："+address);


                            String url_2 = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map_2 = new HashMap<>();
                            map_2.put("type",10014);
                            map_2.put("userName",jsonObject);
                            map_2.put("filePath",address);
                            String jsonString444 = JSONUtil.toJsonStr(map_2);
                            // 发送POST请求
                            HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();
                        }

                    }
                }else if (content.split("-").length == 2){
                    // 位置名-数量
                    String[] split = content.split("-");
                    String address=split[0];
                    Integer number=Integer.parseInt(split[1]);
                    double[] doubles = BaiduMapGeocoding.addressToCoordinate(address);

                    String url_2 = "http://127.0.0.1:8888/api/";
                    HashMap<String, Object> map_2 = new HashMap<>();
                    map_2.put("type",19);
                    map_2.put("longitude",doubles[0]);
                    map_2.put("latitude",doubles[1]);
                    map_2.put("userType",4);
                    String jsonString444 = JSONUtil.toJsonStr(map_2);
                    // 发送POST请求
                    HttpResponse execute = HttpUtil.createPost(url_2).body(jsonString444, "application/json").execute();
                    if (execute.isOk()) {
                        String responseBody = execute.body();
                        // 解析JSON
                        JSONObject root = JSONUtil.parseObj(responseBody);
                        // 获取users数组
                        JSONArray users = root.getJSONObject("data").getJSONObject("data").getJSONArray("users");

                        // 遍历每个用户
                        for (int i = 0; i < number; i++) {
                            JSONObject user = users.getJSONObject(i);
                            String ticket = user.getStr("ticket");
                            String encryptUserName = user.getStr("encryptUserName");

                            String url = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("type",10033);
                            map.put("encryptUserName",encryptUserName);
                            map.put("ticket",ticket);
                            map.put("verifyType",6);
                            String jsonString4 = JSONUtil.toJsonStr(map);
                            // 发送POST请求
                            HttpUtil.createPost(url).body(jsonString4, "application/json").execute();

                        }
                    }
                }
            }
        }
    }

    /**
     * 下载文件并保存到本地指定目录
     * @param fileURL 远程文件 URL
     * @param saveDir 本地保存目录
     * @return 本地文件路径
     * @throws Exception
     */
    public static String downloadFile(String fileURL, String saveDir) throws Exception {
        URL url = new URL(fileURL);
        InputStream inputStream = url.openStream();
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        String saveFilePath = saveDir + fileName;
        Files.copy(inputStream, Paths.get(saveFilePath));
        inputStream.close();
        return saveFilePath;
    }

    private void handleAudioMsg(Map<String, String> data,String from,String to,String belongChatroomNickName) throws Exception {
        String xmlContent = data.get("content");
        System.out.println("看一下值1："+xmlContent);
        // 微信群发言是有前缀的，这里需要去掉
        String[] split = xmlContent.split(":\n");
        xmlContent = split.length > 1 ? split[1] : xmlContent;
        System.out.println("看一下值2："+xmlContent);

        // 使用Hutool的XmlUtil解析XML
        Document doc = XmlUtil.parseXml(xmlContent);
        Element msgElem = doc.getDocumentElement();  // 获取根元素
        Node voicemsgNode = msgElem.getElementsByTagName("voicemsg").item(0);
        if (voicemsgNode != null && voicemsgNode.getNodeType() == Node.ELEMENT_NODE) {
            Element voicemsgElem = (Element) voicemsgNode;
            String aeskey = voicemsgElem.getAttribute("aeskey");
            String fileid = voicemsgElem.getAttribute("voiceurl");
            // 下载音频文件
            downloadAudioFile(fileid, aeskey,from,to,belongChatroomNickName);
        } else {
            System.out.println("No voicemsg element found.");
        }
    }

    private void downloadAudioFile(String fileid, String aeskey,String from,String to,String belongChatroomNickName) {

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
        System.out.println("谁发送的语音"+from);
        System.out.println("谁接收的语音"+to);
        WxVoice wx_voice = new WxVoice();
        wx_voice.setFromWx(from);
        wx_voice.setToWx(to);
        wx_voice.setLiao(belongChatroomNickName);
        wx_voice.setAddress("F:\\\\yuyin\\zhuan\\\\" + aeskey + ".slik");
        wx_voice.setTimes(System.currentTimeMillis() / 1000+"");
        wx_voiceService.save(wx_voice);

//        redisTemplate.opsForHash().put("wx_voice",belongChatroomNickName,from);
    }
}