package cn.qianyekeji.wechatsdk.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.wechatsdk.entity.ChatgptRequest;
import cn.qianyekeji.wechatsdk.mapper.ChatgptMapper;
import cn.qianyekeji.wechatsdk.service.ChatgptService;
import cn.qianyekeji.wechatsdk.service.CodeMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeMessageServiceImpl implements CodeMessageService {

    @Override
    public void getCode(String token_code, String chatRoom, String name_code, String name, String value) throws Exception {
        String url_2 = "http://api.uoomsg.com/zc/data.php";
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("code", "getPhone");
        formData.put("token", token_code);
        formData.put("cardType", "实卡");
        HttpResponse response = HttpUtil.createPost(url_2).form(formData).execute();
        if (response.isOk()) {
            String responseBody = response.body();
            String url_3 = "http://127.0.0.1:8888/api/";
            HashMap<String, Object> map3 = new HashMap<>();
            map3.put("type", 10009);
            map3.put("userName", chatRoom);
            map3.put("msgContent", "@" + name + value + " " + "当前获取到的手机号是:" + responseBody + "验证码1分钟后发送至当前群聊");
            String jsonString = JSONUtil.toJsonStr(map3);
            HttpUtil.createPost(url_3).body(jsonString, "application/json").execute();

            new Thread(() -> {
                try {
                    //获取到验证码后自动发送到当前群聊
                    int maxRetries = 4;
                    int retries = 0;
                    while (retries < maxRetries) {

                        HashMap<String, Object> formData1 = new HashMap<>();
                        formData1.put("code", "getMsg");
                        formData1.put("phone", responseBody);
                        formData1.put("token", token_code);
                        formData1.put("keyWord", name_code);
                        HttpResponse response1 = HttpUtil.createPost(url_2).form(formData1).execute();
                        System.out.println("响应："+response1.body());
                        Thread.sleep(20000);
                        if (response1.body().contains("尚未收到")||response1.body().contains("屏蔽")) {
                            retries++;
                            if (retries == 3) {
                                String url_4 = "http://127.0.0.1:8888/api/";
                                HashMap<String, Object> map4 = new HashMap<>();
                                map4.put("type", 10009);
                                map4.put("userName", chatRoom);
                                map4.put("msgContent", "@" + name + value + " " + "未获取到验证码，请检查短信关键词或稍后重试。");
                                String jsonString4 = JSONUtil.toJsonStr(map4);
                                HttpUtil.createPost(url_4).body(jsonString4, "application/json").execute();
                            }
                        }else{
                            String body = response1.body();
                            String[] parts = body.split("/");
                            String msg = parts[parts.length - 1];
                            String url_5 = "http://127.0.0.1:8888/api/";
                            HashMap<String, Object> map4 = new HashMap<>();
                            map4.put("type", 10009);
                            map4.put("userName", chatRoom);
                            map4.put("msgContent", "@" + name + value + " " + msg);
                            String jsonString4 = JSONUtil.toJsonStr(map4);
                            HttpUtil.createPost(url_5).body(jsonString4, "application/json").execute();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            // 处理错误
            System.err.println("获取手机号出错。。。");
        }
    }
}
