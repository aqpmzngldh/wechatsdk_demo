package cn.qianyekeji.ruiji.controller;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.utils.AudioUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static cn.qianyekeji.ruiji.utils.AudioUtils.transferAudioSilk;

@Controller
public class WeChatWebSocketServer {

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
        //根据文档type是34的时候，是语音消息
        if ("34".equals(type)){
            System.out.println("这个是语音消息");
            handleAudioMsg(data1);

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
                    if (address.charAt(0)==1){
                        String substring = address.substring(1);

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

    private void handleAudioMsg(Map<String, String> data) throws Exception {
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
            downloadAudioFile(fileid, aeskey);
        } else {
            System.out.println("No voicemsg element found.");
        }
    }

    private void downloadAudioFile(String fileid, String aeskey) {

        String url = "http://127.0.0.1:8888/api/";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", 66);
        requestBody.put("fileid", fileid);
        requestBody.put("aeskey", aeskey);
        requestBody.put("fileType", 15);
        requestBody.put("savePath", "F:\\yuyin\\zhuan\\" + aeskey + ".slik");
        String jsonString = JSONUtil.toJsonStr(requestBody);
        HttpUtil.createPost(url).body(jsonString, "application/json").execute();

    }
}