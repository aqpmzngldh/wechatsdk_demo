package cn.qianyekeji.ruiji.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.entity.ceshi;
import cn.qianyekeji.ruiji.mapper.AddressBookMapper;
import cn.qianyekeji.ruiji.mapper.CeShiMapper;
import cn.qianyekeji.ruiji.service.AddressBookService;
import cn.qianyekeji.ruiji.service.CeShiService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CeShiServiceImpl extends ServiceImpl<CeShiMapper, ceshi> implements CeShiService {
    private static final String APP_ID = "wx61c514e5d83894bf";
    private static final String APP_SECRET = "9c8478ca8fea4c3ba2014a7ced03c46e";
    private static String accessToken;
    private static long expirationTime;
    // 声明锁
    private final ReentrantLock lock = new ReentrantLock();


    @Override
    public String access_token() {
        if (lock.tryLock()) {
            try {
                if (accessToken == null || System.currentTimeMillis() >= expirationTime) {
                    refreshAccessToken();
                }
            } finally {
                lock.unlock(); // 释放锁
            }
        }

        // 使用Access Token进行其他操作，例如调用微信API
        System.out.println("Access Token: " + accessToken);

        long remainingTime = (expirationTime - System.currentTimeMillis()) / 1000; // 剩余时间（秒）
        System.out.println("剩余过期时间: " + remainingTime + " 秒");
        return accessToken;
    }
    private void refreshAccessToken() {
        try {
            // 构建获取Access Token的URL
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APP_ID + "&secret=" + APP_SECRET;

            // 发送GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            if (response.isOk()) {
                String responseBody = response.body();
                Map<String, Object> map = JSONUtil.parseObj(responseBody);
                accessToken = (String) map.get("access_token");
                Integer expiresIn = (Integer) map.get("expires_in");// 过期时间（秒）
                expirationTime = System.currentTimeMillis() + expiresIn * 1000; // 转为毫秒
            } else {
                // 处理错误
                System.err.println("Failed to refresh Access Token: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
