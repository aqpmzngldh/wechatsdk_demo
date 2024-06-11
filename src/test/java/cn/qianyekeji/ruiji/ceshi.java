package cn.qianyekeji.ruiji;

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


/**
 * @author liangshuai
 * @date 2023/3/5
 */

@SpringBootTest
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
    void def() {
        String user = "120.2155118__30.25308298_111";
        String userWithoutEnding = user.substring(0, user.lastIndexOf("_"));
        String[] userCoordinates = userWithoutEnding.split("__");
        String userLongitude = userCoordinates[0];
        String userLatitude = userCoordinates[1];
        System.out.println(userLongitude);
        System.out.println(userLatitude);
    }

}
