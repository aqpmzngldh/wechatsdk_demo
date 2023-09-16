package cn.qianyekeji.ruiji.service;

import cn.qianyekeji.ruiji.entity.ChatRequest;
import cn.qianyekeji.ruiji.entity.ceshi;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ChatGptService extends IService<ChatRequest> {
    String chat(String userId,String message);
}
