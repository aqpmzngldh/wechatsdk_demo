package cn.qianyekeji.ruiji;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.JiaXiao;
import cn.qianyekeji.ruiji.service.CeShiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import it.sauronsoftware.jave.AudioUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        String user="120.2155118__30.25308298_111";
        String userWithoutEnding = user.substring(0, user.lastIndexOf("_"));
        String[] userCoordinates = userWithoutEnding.split("__");
        String userLongitude = userCoordinates[0];
        String userLatitude = userCoordinates[1];
        System.out.println(userLongitude);
        System.out.println(userLatitude);
    }

    @Test
    void eee(){
        String jsonString = "{\"code\":200,\"msg\":\"success\",\"result\":{\"content\":\"生活就像淋浴，方向转错，水深火热。\",\"source\":\"佚名\"}}";

        JSONObject jsonObject = JSONUtil.parseObj(jsonString);
        String content = jsonObject.getJSONObject("result").getStr("content");
        String s = jsonObject.getStr("code");

        System.out.println(content);
        System.out.println(s);
    }

    @Test
    void rrr(){
        String k="0566cghmortwxxyz,03/25 11:41:43";
        int length = 14; // 我们要截取的子串长度为8
        String time = k.substring(k.length() - length);
        System.out.println(time);
    }
    @Test
    void ttt(){
//        // 时间戳start对应2023年3月21日凌晨，计算其对应的毫秒值
//        Instant start = Instant.parse("2023-03-21T00:00:00Z");
//        long startMillis = start.toEpochMilli();
//        System.out.println("start = " + startMillis);
//
//        // 时间戳end对应2023年3月27日凌晨，计算其对应的毫秒值
//        Instant end = Instant.parse("2023-03-27T00:00:00Z");
//        long endMillis = end.toEpochMilli();
//        System.out.println("end = " + endMillis);
        // 当前时间的凌晨
        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long nowMillis = now.toEpochMilli();
        System.out.println("now = " + nowMillis);

        // 7天前21号凌晨
        LocalDate localDate = LocalDate.now().minusDays(7).withDayOfMonth(21);
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant();
        long startMillis = instant.toEpochMilli();
        System.out.println("start = " + startMillis);
    }
    @Test
    void timestampToDateTime() {
        long timestamp = 1679356800000L; // 2022-07-21 00:00:00 UTC
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd")
                .withZone(ZoneId.of("UTC"));
        String formattedDateTime = formatter.format(instant);
        System.out.println("Timestamp " + timestamp + " corresponds to " + formattedDateTime);
    }

    @Test
    void ooo(){
        Instant instant = Instant.parse("2023-03-31T02:02:02.000Z");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        String formattedDateTime = zonedDateTime.format(formatter);
        System.out.println(formattedDateTime);
    }


    @Test
    void oo(){
        //在内存中创建一个Excel文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        //创建工作表，指定工作表名称
        XSSFSheet sheet = workbook.createSheet("基础数据表");
        //创建行，0表示第一行
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("送货时间");
        row.createCell(1).setCellValue("送货人姓名");
        row.createCell(2).setCellValue("送货人手机号");
        row.createCell(3).setCellValue("送货车牌");
        row.createCell(4).setCellValue("货品品类");
        row.createCell(5).setCellValue("货品数量");

    }

    @Test
    void ppp(){
        String ss="爸到爸";
        String [] arr={"狗","傻","爷","爹","妈","爸","B","b","操","奶子","胸","逼","猪","黄","电影","片","视频","日"};
        for (String s : arr) {
            if(ss.contains(s)){
                System.out.println("ss contains " + s);
            }
        }
    }
    @Test
    void  p(){
        String s="1,2";
        int length = s.split(",").length;
        System.out.println(length);
    }

    @Test
    void  pttrr()throws Exception{
//        LocalTime currentTime = LocalTime.now();
//        // 给定时间
//        LocalTime givenTime = LocalTime.parse("21:58", DateTimeFormatter.ofPattern("H:mm"));
//        // 将LocalTime转换为LocalDateTime
//        LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(), currentTime);
//        LocalDateTime givenDateTime = LocalDateTime.of(LocalDate.now(), givenTime);
//
//        // 计算分钟差值的绝对值
//        long minutesDiff = Math.abs(currentDateTime.until(givenDateTime, ChronoUnit.MINUTES));
//
//        // 输出分钟差值
//        System.out.println("分钟差值: " + minutesDiff);

        // 因为自己电脑时间不准，就不用上面那种方式了，用获取网络时间这种方式
        URL url = new URL("http://www.baidu.com");
        long networkTime = url.openConnection().getDate();

        // 将网络时间转换为 UTC ZonedDateTime
        Instant instant = Instant.ofEpochMilli(networkTime);
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));

        // 将 UTC ZonedDateTime 转换为中国时区
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime shanghaiTime = utcDateTime.withZoneSameInstant(shanghaiZone);

        System.out.println("中国时间: " + shanghaiTime);

        // 给定时间
        String givenTimeStr = "03:02";
        LocalTime givenTime = LocalTime.parse(givenTimeStr, DateTimeFormatter.ofPattern("H:mm"));
        ZonedDateTime givenDateTime = ZonedDateTime.of(shanghaiTime.toLocalDate(), givenTime, shanghaiZone);
        System.out.println("给定时间: " + givenDateTime);
        // 计算分钟差值的绝对值
        long minutesDiff = Math.abs(ChronoUnit.MINUTES.between(shanghaiTime, givenDateTime));
        System.out.println("分钟差值的绝对值: " + minutesDiff);
    }

    @Test
    void  pppp(){
        File source = new File("C:\\Users\\qianye\\Downloads\\wuhqWBgCZ-i8TFGehZor1-e_aUgjttrsZu3qsCHsFC1EoAac0gwfbq7kZ9k2YaO5.amr");
        File target = new File("C:\\Users\\qianye\\Downloads\\1.mp3");
        AudioUtils.amrToMp3(source, target);
    }

    @Test
    void  pppppppppp(){
        long timestamp = System.currentTimeMillis() / 1000;
        System.out.println(timestamp);
    }


    @Test
    void  pppppp(){
        //时间戳转换成当前时间
        long timestamp = 1717764574;

        // 转换为 UTC 时间
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZonedDateTime utcTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);

        // 转换为中国标准时间 (CST)
        ZonedDateTime cstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));

        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String cstTimeStr = cstTime.format(formatter);

        System.out.println(cstTimeStr);
    }

    @Test
//    R<List<JiaXiao>> opop(){
    void opop(){

        //从session中取出来的小程序的openid（小程序登录的时候放进去的）
        String openid="openid4";
        //传递到后端的分数
        String scroe="99";

        //把这次提交的分数进行保存到redis
        Boolean openid11 = redisTemplate.opsForHash().hasKey("xcx_1", openid);
        if (openid11){
            Object openid1 = redisTemplate.opsForHash().get("xcx_1", openid);
            String timeScoreStr111 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "_" + scroe;
            String s = openid1 + ","+timeScoreStr111;
            redisTemplate.opsForHash().put("xcx_1", openid, s);
        }else{
            String timeScoreStr11 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "_" + scroe;
            redisTemplate.opsForHash().put("xcx_1", openid, timeScoreStr11);
        }
        //对最近10次成绩进行取出
        String openid1111 = (String)redisTemplate.opsForHash().get("xcx_1", openid);
//        2024-02-17 11:01_99分,2024-02-17 11:01_99分
//        对openid111先进行，分割
//        分割后再进行下划线_分割
//        创建实体类对象，返回
        String[] pairs = openid1111.split(",");
        ArrayList<JiaXiao> jiaXiaos = new ArrayList<>();
        for (String pair : pairs) {
            String[] parts = pair.split("_");
            String time = parts[0];
            String score = parts[1];
            System.out.println("时间: " + time + ", 分数: " + score);
            JiaXiao jiaXiao = new JiaXiao();
            jiaXiao.setTime(time);
            jiaXiao.setScore(score);
            jiaXiaos.add(jiaXiao);
        }
        System.out.println(jiaXiaos+"000");
//        return R.success(jiaXiaos);
//        return null;
    }
}
