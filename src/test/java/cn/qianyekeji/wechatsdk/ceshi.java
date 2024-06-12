package cn.qianyekeji.wechatsdk;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import it.sauronsoftware.jave.AudioUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    @Test
    void c() {
        String substring1 = "@name 今日早报";
        String name = "name";
        // 将"@" + name 替换为空字符串，并且去除所有的空白字符
        String message = substring1.replace("@" + name, "").replaceAll("\\s+", "");
        System.out.println("看一下这个消息内容是" + message);
        System.out.println("看一下这个消息内容是" + StrUtil.trim(message));
    }

    @Test
    void d() {
        // 文件路径
        String filePath = "C:\\Users\\qianye\\Desktop\\csdn.txt";
        StringBuilder resultString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 逐行读取文件内容
            while ((line = br.readLine()) != null) {
                // 将读取的行拼接到结果字符串中，并添加换行符
                resultString.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出结果字符串
        System.out.println(resultString.toString());
    }

    @Test
    void e() {
        // 文件URL
        String fileURL = "https://qianyekeji.cn/img2/csdn.txt";
        StringBuilder resultString = new StringBuilder();
        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 使用BufferedReader读取输入流
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    // 逐行读取文件内容
                    while ((line = br.readLine()) != null) {
                        // 将读取的行拼接到结果字符串中，并添加换行符
                        resultString.append(line).append("\n");
                    }
                }
            } else {
                System.out.println("GET请求失败，响应代码：" + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出结果字符串
        System.out.println(resultString.toString());

    }

}
