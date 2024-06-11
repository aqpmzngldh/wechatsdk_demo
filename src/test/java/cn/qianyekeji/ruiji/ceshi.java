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


/**
 * @author liangshuai
 * @date 2023/3/5
 */

@SpringBootTest
public class ceshi {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void abc() {
        String str1 = "1234";
        String str2 = "1";
        String[] arr2 = str2.split(",");

        Integer num = 0;
        ArrayList<String> list = new ArrayList<>(Arrays.asList(arr2));
        if (list.contains(str1)) {
            num++;
            list.remove(str1);
        } else {
            list.add(str1);
        }

        String[] newArr2 = list.toArray(new String[0]);

        String newStr2 = String.join(",", newArr2); // 将新数组转换成以逗号分隔的字符串

        System.out.println("原字符串str2：" + str2);
        System.out.println("新字符串newStr2：" + newStr2);
        if (num == 0) {
//            不包含，这时候新增number+1
        } else {
//            包含，number-1
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
