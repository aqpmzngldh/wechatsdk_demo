package cn.qianyekeji.wechatsdk.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.wechatsdk.entity.ChatgptRequest;
import cn.qianyekeji.wechatsdk.mapper.ChatgptMapper;
import cn.qianyekeji.wechatsdk.service.ChatgptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatgptServiceImpl extends ServiceImpl<ChatgptMapper, ChatgptRequest> implements ChatgptService {

    @Value("${wecahtsdk.OPENAI_API_KEY}")
    private String OPENAI_API_KEY_2;
    @Value("${wecahtsdk.OPENAI_API_URL}")
    private String OPENAI_API_URL;
    private Map<String, List<JSONObject>> userSessions3 = new HashMap<>();


    @Override
    public String chat(String userId,String message, String tag) {
        // 检查用户会话是否存在
        if (!userSessions3.containsKey(userId)) {
            //第一次进来肯定不存在会话中，这时候我们放进去
            userSessions3.put(userId, new ArrayList<>());
        }
        // 将用户发送的消息添加到会话中
        List<JSONObject> sessionMessages3 = userSessions3.get(userId);
        JSONObject userMessage = new JSONObject();
        userMessage.put("content", message);
        userMessage.put("role", "user");
        sessionMessages3.add(userMessage);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY_2);

        // 构建请求体
        String requestBody = buildRequestBody3(sessionMessages3, tag);
        System.out.println("看一下这个：" + requestBody);

        // 发送请求
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;

        int maxRetries = 5;
        int retries = 0;
        while (retries < maxRetries) {
            try {
                response = restTemplate.postForEntity(OPENAI_API_URL, request, String.class);
                HttpStatus statusCode = response.getStatusCode();
                System.out.println("---------------------" + statusCode + "--------------------");
                // 提取回复消息
                String responseBody = response.getBody();
                String reply = extractReplyFromResponse(responseBody);
                System.out.println("-------------------" + reply + "--------------------");

                // 将ChatGPT的回复添加到会话中
                JSONObject replyMessage = new JSONObject();
                replyMessage.put("content", reply);
                replyMessage.put("role", "assistant");
                sessionMessages3.add(replyMessage);
                System.out.println("看一下sessionMessages：" + sessionMessages3);
                return reply;
            } catch (RestClientException e) {
                // 发生异常时增加重试次数，并输出错误信息
                retries++;
                System.out.println("Retry attempt: " + retries);
                e.printStackTrace();
                try {
                    // 延迟5秒后重试
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return "当前访问人数过多，请稍后重试";
    }


    private String extractReplyFromResponse(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String reply = message.getStr("content");

        return reply;
    }


    //通用
    private String buildRequestBody3(List<JSONObject> sessionMessages, String tag) {
        JSONArray messagesArray = new JSONArray();
        for (JSONObject message : sessionMessages) {
            messagesArray.add(message);
        }
        JSONObject requestBodyObj = new JSONObject();
        if ("0".equals(tag)) {
            requestBodyObj.put("model", "gpt-3.5-turbo");
        } else {
            requestBodyObj.put("model", "gpt-4o");
        }
        requestBodyObj.put("messages", messagesArray);
        return requestBodyObj.toString();
    }


    // 设置定时任务,每小时执行一次
    @Scheduled(cron = "0 0 * * * *")
    public void cleanUserSessions() {
        userSessions3.clear();
        System.out.println("会话数据已清理");
    }
}
