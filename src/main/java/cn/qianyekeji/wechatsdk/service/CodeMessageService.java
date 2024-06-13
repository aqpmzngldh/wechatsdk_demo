package cn.qianyekeji.wechatsdk.service;


import cn.qianyekeji.wechatsdk.entity.ChatgptRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CodeMessageService{
    void getCode(String token_code, String chatRoom, String name_code, String name, String value)throws Exception;

}
