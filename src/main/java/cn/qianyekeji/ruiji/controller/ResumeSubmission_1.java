package cn.qianyekeji.ruiji.controller;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author BeamStark
 * Boss直聘自动投递111
 * @date 2023-05-01-04:16
 */
@Slf4j
@Controller
@RequestMapping("/boss")
public class ResumeSubmission_1 {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    static String loginUrl = "https://www.zhipin.com/web/user/?ka=header-login&r="+ Math.random();
    static String chatUrl ="https://www.zhipin.com/web/geek/chat";
    static String chatUser ="https://www.zhipin.com/web/geek/recommend";
    // 设置 ChromeDriver 的路径
    static {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\qianye\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe");
    }
    static ChromeDriver driver = new ChromeDriver();
    static WebDriverWait wait15s = new WebDriverWait(driver, 30);
    static WebDriverWait wait2s = new WebDriverWait(driver, 2);
    static List<String> returnList = new ArrayList<>();
    // 本地缓存，记录已处理的消息标识符和对应的消息内容
    static Map<String, String> messageMap = new HashMap<>();

    @GetMapping
    @ResponseBody
    public String boss() {
        String login = login();
        System.out.println("login:"+login);
        if (login!=null){
            // 每次启动的时候清空messageMap
            messageMap.clear();
            chat();
        }
        return "规定时间内未登录";
    }


    @SneakyThrows
    private static String login() {
        driver.get(loginUrl);
        // 等待登录按钮元素出现，但不抛出异常
        try {
            System.out.println("第一次登录");
            wait2s.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='btn-sign-switch ewm-switch']")));
            // 如果登录按钮存在，执行点击操作
            driver.findElement(By.cssSelector("[class*='btn-sign-switch ewm-switch']")).click();
        } catch (TimeoutException e) {
            // 如果按钮元素不存在，说明不是第一次登录
            System.out.println("再次登录");

        }
        log.info("等待登陆..");

        WebElement elementByName = driver.findElementByClassName("qr-img-box");
        String innerHTML = elementByName.getAttribute("innerHTML");
        int srcStartIndex = innerHTML.indexOf("src=\"") + 5; // 找到 src 属性的起始位置
        int srcEndIndex = innerHTML.indexOf("\"", srcStartIndex); // 找到 src 属性值的结束位置
        String srcValue = innerHTML.substring(srcStartIndex, srcEndIndex); // 提取 src 属性值
        System.out.println(srcValue);
        String code="https://www.zhipin.com"+srcValue;
        System.out.println("当前登录的二维码是："+code);
        //这里到时候加一个给这个二维码通过邮件发送给用户的逻辑就好了，不能给二维码return回去，因为return的话就不在程序中走了
        try {
            wait15s.until(ExpectedConditions.presenceOfElementLocated(By.className("nav-school-new")));
        } catch (Exception e) {
            return null;
        }
        return "1";
    }


    //获取最新聊天记录
    @SneakyThrows
    private  void chat() {
        try {

        driver.get(chatUser);
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='username']")));
        String user = driver.findElement(By.className("username")).getText();


        driver.get(chatUrl);
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='text']")));
        List<WebElement> textElements = driver.findElements(By.cssSelector("[class*='text']"));

        int readCount = 0;
        int unreadCount = 0;
        int nonEmptyThirdDivCount = 0;
        int processedCount = 0;

//        for (WebElement textElement : textElements) {
        for (int i = 0; i < textElements.size(); i++) {
            // 获取所有的 <div> 标签
            List<WebElement> divElements = textElements.get(i).findElements(By.tagName("div"));

            // 如果有足够的 <div> 标签
            if (divElements.size() >= 3) {
                processedCount++;  // 增加已处理的消息计数器

                // 第二个 <div> 标签
                WebElement secondDivElement = divElements.get(1);
                // 第三个 <div> 标签
                WebElement thirdDivElement = divElements.get(2);

                // 获取第二个 <div> 标签中的所有 <span> 标签
                List<WebElement> secondDivSpanElements = secondDivElement.findElements(By.tagName("span"));
                // 获取第三个 <div> 标签中的所有 <span> 标签
                List<WebElement> thirdDivSpanElements = thirdDivElement.findElements(By.tagName("span"));

                // 获取第二个 <div> 标签中的第一个 <span> 标签的文本内容
                String secondDivFirstSpanText = secondDivSpanElements.get(1).getText();
                // 获取第二个 <div> 标签中的第二个 <span> 标签的文本内容
                String secondDivSecondSpanText = secondDivSpanElements.get(2).getText();
                String secondDivSecondSpanText3 = secondDivSpanElements.get(3).getText();
                // 获取第三个 <div> 标签中的第一个 <span> 标签的文本内容
                String thirdDivFirstSpanText = thirdDivSpanElements.get(0).getText();

                //相同消息只发送一次到redis
                String messageKey = secondDivSecondSpanText + "的" + secondDivSecondSpanText3 + secondDivFirstSpanText;

                // 检查 thirdDivFirstSpanText 是否为空
                if ("".equals(thirdDivFirstSpanText)) {
                    // 获取 thirdDivElement 的 class 属性值
                    String thirdDivClass = thirdDivElement.getAttribute("class");
                    // 检查 class 属性值是否包含 "status status-read"
                    if (thirdDivClass.contains("status status-read")) {
                        //已读未回复，未回复的话问他为什么不回复，比如：杨女士你怎么不回复我
                        readCount++;
                    } else {
                        //未读，未读的话5小时后进行一次重试
                        unreadCount++;
                    }
                } else {
                    //已回复自己
                    nonEmptyThirdDivCount++;
//                    如果聊天中对方要自己的简历，这种格式是我想要一份您的附件简历，您是否同意，这时候自动发送

                    if (processedCount<=13) {
                        System.out.println("当前登录用户是："+user);
                        System.out.println(secondDivSecondSpanText+"的"+secondDivSecondSpanText3+secondDivFirstSpanText+
                                ":"+thirdDivFirstSpanText);

                        try {
//                            System.out.println("经过观察网页，发现<=13的时候不会出错");
//                            System.out.println("看一下这是小的第几次出错了"+processedCount);
                            textElements.get(i).click();
                            WebElement btnAgree = wait2s.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='btn btn-agree']")));
                            btnAgree.click();
                            System.out.println("只有找到了才会在这里继续执行，找不到会执行catch中代码");

                            //这里去判断一下微信中用户是否回复了该人，是的话，则给他再发送消息
                            // 获取所有的键值对
                            Map<Object, Object> entries = redisTemplate.opsForHash().entries("a_boss_狂人_huifu");
                            // 判断entries是否不为空
                            if (!entries.isEmpty()) {
                                // 遍历输出所有键值对
                                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                                    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                                    if (entry.getKey().equals(messageKey)){
                                        WebElement chatInput = driver.findElement(By.className("chat-input"));
                                        chatInput.sendKeys((String)entry.getValue());
                                        // 尝试查找发送按钮
                                        List<WebElement> sendButtons = driver.findElements(By.cssSelector("[class*='btn-v2 btn-sure-v2 btn-send']"));
                                        sendButtons.get(0).click();

                                        //发送了消息后再给这行数据删掉
                                        redisTemplate.opsForHash().delete("a_boss_狂人_huifu",entry.getKey());
                                    }
                                }
                            }
                        } catch (TimeoutException e) {
                            System.out.println("未找到发送简历按钮");
                            //这里去判断一下微信中用户是否回复了该人，是的话，则给他再发送消息
                            // 获取所有的键值对
                            Map<Object, Object> entries = redisTemplate.opsForHash().entries("a_boss_狂人_huifu");
                            // 判断entries是否不为空
                            if (!entries.isEmpty()) {
                                // 遍历输出所有键值对
                                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                                    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                                    if (entry.getKey().equals(messageKey)){
                                        WebElement chatInput = driver.findElement(By.className("chat-input"));
                                        chatInput.sendKeys((String)entry.getValue());
                                        // 尝试查找发送按钮
                                        List<WebElement> sendButtons = driver.findElements(By.cssSelector("[class*='btn-v2 btn-sure-v2 btn-send']"));
                                        sendButtons.get(0).click();

                                        //发送了消息后再给这行数据删掉
                                        redisTemplate.opsForHash().delete("a_boss_狂人_huifu",entry.getKey());
                                    }
                                }
                            }
                        }
                        // 检查并存储消息到 map 中
                        if (messageMap.containsKey(messageKey)) {
                            if (!messageMap.get(messageKey).equals(thirdDivFirstSpanText)) {
                                messageMap.put(messageKey, thirdDivFirstSpanText);  // 更新 value
                                //这里再加入存入redis的操作
                                String msg = (String)redisTemplate.opsForHash().get("a_boss_狂人", messageKey);
                                if (msg== null){
                                    redisTemplate.opsForHash().put("a_boss_狂人", messageKey, thirdDivFirstSpanText);
                                }else{
                                    //这时候发现已经存储有数据了，然后进行拼接
                                    String pinjie=msg+"==="+thirdDivFirstSpanText;
                                    redisTemplate.opsForHash().put("a_boss_狂人", messageKey, pinjie);
                                }
                            }
                        } else {
                            messageMap.put(messageKey, thirdDivFirstSpanText);  // 新增键值对
                            //这里再加入存入redis的操作
                            String msg = (String)redisTemplate.opsForHash().get("a_boss_狂人", messageKey);
                            if (msg==null){
                                redisTemplate.opsForHash().put("a_boss_狂人", messageKey, thirdDivFirstSpanText);
                            }else{
                                //这时候发现已经存储有数据了，然后进行拼接
                                String pinjie=msg+"==="+thirdDivFirstSpanText;
                                redisTemplate.opsForHash().put("a_boss_狂人", messageKey, pinjie);
                            }
                        }

                    }
                }
            }
        }

        // 输出统计结果
        System.out.println("已读未回复消息数量：" + readCount);
        System.out.println("未读未回复消息数量：" + unreadCount);
        System.out.println("已回复数量：" + nonEmptyThirdDivCount);

        // 在chat()方法的末尾添加对自身的递归调用
            chat();
        } catch (Exception e) {
            System.out.println("报错后继续执行下一次");
            chat();
        }
    }
}
