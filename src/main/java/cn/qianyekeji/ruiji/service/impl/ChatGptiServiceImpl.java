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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Service
public class ChatGptiServiceImpl extends ServiceImpl<ChatGptMapper, ChatRequest> implements ChatGptService {
//    private final String OPENAI_API_KEY = "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";
    private final String OPENAI_API_KEY = "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";
    private final String OPENAI_API_URL = "https://ls.zhao9wan6.work/proxy/api.openai.com/v1/chat/completions";
    private Map<String, List<String>> userSessions = new HashMap<>();

    private void configureSSLContext() {
        try {
            // 创建一个信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // 设置 SSL 上下文
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // 设置主机名验证器
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public String chat(String userId,String message) {
        // 先配置 SSL 上下文
        configureSSLContext();

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
//        try {
//            response = restTemplate.postForEntity(OPENAI_API_URL,request, String.class);
//        } catch (RestClientException e) {
//            e.printStackTrace();
//            return "网络错误，请重试";
//        }
//        HttpStatus statusCode = response.getStatusCode();
//        System.out.println("---------------------"+statusCode+"--------------------");
//        // 提取回复消息
//        String responseBody = response.getBody();
//        String reply = extractReplyFromResponse(responseBody);
//        System.out.println("-------------------"+reply+"--------------------");
//
//        //把回复消息也存进当前用户的的list中，方便上下文记忆
//        sessionMessages.add(reply);
//
//        return reply;

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
        System.out.println("Starting user session cleanup...");

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
                    System.out.println("Cleaned session for user: " + userId);
                }
            }
        }

        System.out.println("User session cleanup completed.");
    }
}
