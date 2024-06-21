package cn.qianyekeji.wechatsdk.service;


import cn.qianyekeji.wechatsdk.entity.ChatgptRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ChatgptService extends IService<ChatgptRequest> {
    String chat(String userId, String message,String tag);
}
