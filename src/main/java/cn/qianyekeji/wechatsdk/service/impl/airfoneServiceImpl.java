package cn.qianyekeji.wechatsdk.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.wechatsdk.service.AirfoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class airfoneServiceImpl implements AirfoneService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void airfoneChat(String nickName, String msg, String chatRoom, String name, String value) throws Exception {
        String url = "http://127.0.0.1:8888/api/";
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", 10058);
        map.put("dbName", "MicroMsg.db");
        map.put("sql", "SELECT UserName,Remark,NickName,PYInitial,RemarkPYInitial,t2.smallHeadImgUrl FROM Contact t1 LEFT JOIN ContactHeadImgUrl t2 ON t1.UserName = t2.usrName WHERE t1.VerifyFlag = 0 AND (t1.Type = 3 OR t1.Type > 50) and t1.Type != 2050 AND t1.UserName NOT IN ('qmessage', 'tmessage') ORDER BY t1.Remark DESC;");
        String jsonString = JSONUtil.toJsonStr(map);
        HttpResponse response = HttpUtil.createPost(url).body(jsonString, "application/json").execute();
        if (response.isOk()) {
            String responseBody = response.body();
            JSONArray dataList = JSONUtil.parseObj(responseBody).getJSONObject("data").getJSONArray("data");
            System.out.println("获取到de好友列表：" + dataList);
            boolean found = false;
            for (int i = 0; i < dataList.size(); i++) {
                JSONObject item = dataList.getJSONObject(i);
                String NickName = item.getStr("NickName");
                if (nickName.equals(NickName)) {
                    found = true;
                    String UserName = item.getStr("UserName");
                    String url_2 = "http://127.0.0.1:8888/api/";
                    HashMap<String, Object> map2 = new HashMap<>();
                    map2.put("type", 10009);
                    map2.put("userName", UserName);
                    map2.put("msgContent", msg);
                    String jsonString2 = JSONUtil.toJsonStr(map2);
                    HttpUtil.createPost(url_2).body(jsonString2, "application/json").execute();
                    break;
                }
            }
            if (!found) {
                String url_2 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> map2 = new HashMap<>();
                map2.put("type", 10009);
                map2.put("userName", chatRoom);
                //防止被滥用，不主动添加别人
                map2.put("msgContent", "@" + name + value + " " + "没有找到当前好友,请先添加当前机器人为好友");
                String jsonString2 = JSONUtil.toJsonStr(map2);
                HttpUtil.createPost(url_2).body(jsonString2, "application/json").execute();
            } else {
                String url_1 = "http://127.0.0.1:8888/api/";
                HashMap<String, Object> hashMap_1 = new HashMap<>();
                hashMap_1.put("type", 30);
                hashMap_1.put("chatroomUserName", chatRoom);
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
                    redisTemplate.opsForHash().put("a_route", nickName, encryptUserName);

                }
            }
        } else {
            // 处理错误
            System.err.println("查询全部好友出错。。。");
        }

    }
}
