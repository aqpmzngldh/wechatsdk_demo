package cn.qianyekeji.ruiji.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.entity.ChatRequest;
import cn.qianyekeji.ruiji.entity.ceshi;
import cn.qianyekeji.ruiji.mapper.CeShiMapper;
import cn.qianyekeji.ruiji.mapper.ChatGptMapper;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.ChatGptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ChatGptiServiceImpl extends ServiceImpl<ChatGptMapper, ChatRequest> implements ChatGptService {
//    private final String OPENAI_API_KEY = "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";
    private final String OPENAI_API_KEY = "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";
    private final String OPENAI_API_URL = "https://ls.zhao9wan6.work/proxy/api.openai.com/v1/chat/completions";
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
        try {
            response = restTemplate.postForEntity(OPENAI_API_URL,request, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            return "网络错误，请重试";
        }
        HttpStatus statusCode = response.getStatusCode();
        System.out.println("---------------------"+statusCode+"--------------------");
        // 提取回复消息
        String responseBody = response.getBody();
        String reply = extractReplyFromResponse(responseBody);
        System.out.println("-------------------"+reply+"--------------------");

        //把回复消息也存进当前用户的的list中，方便上下文记忆
        sessionMessages.add(reply);

        return reply;
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
}
