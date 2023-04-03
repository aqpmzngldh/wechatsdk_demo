import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.service.CeShiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liangshuai
 * @date 2023/3/5
 */
public class ceshi {
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

}
