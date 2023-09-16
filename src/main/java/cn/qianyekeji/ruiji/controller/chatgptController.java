package cn.qianyekeji.ruiji.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.entity.ChatRequest;
import cn.qianyekeji.ruiji.service.ChatGptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatgpt")
public class chatgptController {
    private final String OPENAI_API_KEY = "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";
//    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String OPENAI_API_URL = "https://ls.zhao9wan6.work/proxy/api.openai.com/v1/chat/completions";
    private Map<String, List<String>> userSessions = new HashMap<>();
    @Autowired
    private ChatGptService chatGptService;

    @PostMapping
    public String chatWithGPT(@RequestBody ChatRequest chatRequest) {

        //获取用户的标识和消息
        String userId = chatRequest.getUserId();
        String message = chatRequest.getMessage();
        String chat = chatGptService.chat(userId, message);
        return chat;
//        // 检查用户会话是否存在
//        if (!userSessions.containsKey(userId)) {
//            //第一次进来肯定不存在会话中，这时候我们放进去
//            userSessions.put(userId, new ArrayList<>());
//        }
//        //把发送的消息扔进这个人的list中
//        List<String> sessionMessages = userSessions.get(userId);
//        sessionMessages.add(message);
//
//        // 构建请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(OPENAI_API_KEY);
//
//        // 构建请求体
//        String requestBody = buildRequestBody(userId, sessionMessages);
//
//        // 发送请求
//        RestTemplate restTemplate = new RestTemplate();
////        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
////        factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 33210)));
////        factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)));
////        restTemplate.setRequestFactory(factory);
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL,request, String.class);
//        // 提取回复消息
//        String responseBody = response.getBody();
//        String reply = extractReplyFromResponse(responseBody);
//        System.out.println("-------------------"+reply+"--------------------");
//
//        //把回复消息也存进当前用户的的list中，方便上下文记忆
//        sessionMessages.add(reply);
//
//        return reply;
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
