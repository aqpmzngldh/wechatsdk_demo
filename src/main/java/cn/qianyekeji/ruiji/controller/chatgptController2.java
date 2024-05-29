package cn.qianyekeji.ruiji.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.entity.ChatRequest;
import cn.qianyekeji.ruiji.service.ChatGptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatgpt2")
public class chatgptController2 {

    @Autowired
    private ChatGptService chatGptService;

    @PostMapping
    public String chatWithGPT(@RequestBody ChatRequest chatRequest) {

        //获取用户的标识和消息
        String userId = chatRequest.getUserId();
        String message = chatRequest.getMessage();
        System.out.println(userId);
        System.out.println(message);
        String chat = chatGptService.chat_2(userId, message);
        System.out.println(chat);
        return chat;

    }
}
