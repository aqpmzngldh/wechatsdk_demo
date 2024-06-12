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
    private String OPENAI_API_KEY;
    @Value("${wecahtsdk.OPENAI_API_URL}")
    private String OPENAI_API_URL;
    private Map<String, List<String>> userSessions = new HashMap<>();

    @Override
    public String chat(String userId,String message) {
        // 检查用户会话是否存在
        if (!userSessions.containsKey(userId)) {
            //第一次进来肯定不存在会话中，这时候我们放进去
            userSessions.put(userId, new ArrayList<>());
        }
        //把发送的消息扔进这个人的list中
        List<String> sessionMessages = userSessions.get(userId);
        sessionMessages.add(message);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY);

        // 构建请求体
        String requestBody = buildRequestBody(userId, sessionMessages);

        // 发送请求
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;

        //gpt3.5频率1分钟三次，所以这里加入重试
        int maxRetries = 5;
        int retries = 0;
        while (retries < maxRetries) {
            try {
                response = restTemplate.postForEntity(OPENAI_API_URL, request, String.class);
                HttpStatus statusCode = response.getStatusCode();
                // 提取回复消息
                String responseBody = response.getBody();
                String reply = extractReplyFromResponse(responseBody);
                System.out.println("-------------------" + reply + "--------------------");
                // 如果成功收到回复，返回回复消息，并把回复消息也存进当前用户的的list中，方便上下文记忆
                sessionMessages.add(reply);
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


    private String buildRequestBody(String userId, List<String> sessionMessages) {
        JSONArray messagesArray = new JSONArray();
        for (String message : sessionMessages) {
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", message);
            messagesArray.add(messageObj);
        }

        JSONObject requestBodyObj = new JSONObject();
        requestBodyObj.put("model", "gpt-3.5-turbo");
        requestBodyObj.put("messages", messagesArray);

        return requestBodyObj.toString();
    }

    private String extractReplyFromResponse(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String reply = message.getStr("content");

        return reply;
    }


    // 设置定时任务，每小时执行一次
    @Scheduled(cron = "0 0 * * * *")
    public void cleanUserSessions() {
//        System.out.println("不清理的话，随着聊天数据变多会出错");

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 遍历用户会话数据，清理过期数据
        for (String userId : userSessions.keySet()) {
            List<String> sessionMessages = userSessions.get(userId);
            // 判断会话数据是否过期，这里假设会话数据过期时间为1小时
            if (sessionMessages != null && !sessionMessages.isEmpty()) {
                LocalDateTime lastMessageTime = LocalDateTime.parse(sessionMessages.get(sessionMessages.size() - 1));
                if (lastMessageTime.plusHours(1).isBefore(currentTime)) {
                    // 如果会话数据过期，清理该用户会话数据
                    userSessions.remove(userId);
                }
            }
        }

        System.out.println("清理完成");
    }
}
