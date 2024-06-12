package cn.qianyekeji.wechatsdk;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import it.sauronsoftware.jave.AudioUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeString.substring;


/**
 * @author liangshuai
 * @date 2023/3/5
 */

//@SpringBootTest
public class ceshi {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void a() {
        String input = "这是一个包含 \"双引号包裹的内容\"哈哈哈";
        // 定义正则表达式模式，匹配双引号包裹的内容
        String regex = "\"(.*?)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        } else {
            System.out.println("没有找到匹配的内容");
        }
    }

    @Test
    void b() {
        String str="wxid_o42elvr0ggen22:\n" +
                "在吗";
        int newlineIndex = str.indexOf('\n');
        if (newlineIndex != -1) {
            String substring = str.substring(0, newlineIndex);
            int lastColonIndex = substring.lastIndexOf(':');
            String newStr = str.substring(0, lastColonIndex);
            System.out.println("当前群聊中是谁发送的消息："+newStr);
            System.out.println("发送的消息是："+str.substring(newlineIndex + 1));
        }
    }

}
