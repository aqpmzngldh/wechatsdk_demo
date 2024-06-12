package cn.qianyekeji.wechatsdk.service;

import cn.qianyekeji.wechatsdk.common.R;

import java.util.HashMap;

public interface CountService {
    void addCount(String roomId, String talker);
    HashMap selectCount(String roomId);
}
