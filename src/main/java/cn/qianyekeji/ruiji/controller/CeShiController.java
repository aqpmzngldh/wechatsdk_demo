package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.entity.ceshi;
import cn.qianyekeji.ruiji.service.AddressBookService;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ceShi")
@Slf4j
public class CeShiController {
    @Autowired
    private CeShiService ceShiService;
    @Value("${ruiji.path3}")
    private String basePath;


    @PostMapping
    public R<String> dianzan(@RequestBody ceshi ceShi) throws Exception {
        System.out.println(ceShi);
        Instant parse = Instant.parse(ceShi.getDate());
        //日期获取的格式这边改一下
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(parse, ZoneId.systemDefault());
        String formattedDateTime = zonedDateTime.format(formatter);
        ceShi.setDate(formattedDateTime);

        ceShiService.save(ceShi);
        return R.success("1");
    }

    @PostMapping("/excel")
    public void excel(HttpServletResponse response) throws Exception {

        List<ceshi> list = ceShiService.list();
        System.out.println(list);
        //在内存中创建一个Excel文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        //创建工作表，指定工作表名称
        XSSFSheet sheet = workbook.createSheet("测试数据表");

        //创建行，0表示第一行
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("送货时间");
        row.createCell(1).setCellValue("送货人姓名");
        row.createCell(2).setCellValue("送货人手机号");
        row.createCell(3).setCellValue("送货车牌");
        row.createCell(4).setCellValue("CPU数量");
        row.createCell(5).setCellValue("硬盘数量");
        row.createCell(6).setCellValue("内存数量");
        row.createCell(7).setCellValue("显卡数量");
        row.createCell(8).setCellValue("键盘数量");
        row.createCell(9).setCellValue("鼠标数量");
        XSSFCell cell;
        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
            cell = row.createCell(0);
            cell.setCellValue(list.get(i).getDate());
            cell = row.createCell(1);
            cell.setCellValue(list.get(i).getName());
            cell = row.createCell(2);
            cell.setCellValue(list.get(i).getPhone());
            cell = row.createCell(3);
            cell.setCellValue(list.get(i).getSign());

            if ("1".equals(list.get(i).getSelect1())){
                cell = row.createCell(4);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("1".equals(list.get(i).getSelect2())){
                cell = row.createCell(4);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("1".equals(list.get(i).getSelect3())){
                cell = row.createCell(4);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("1".equals(list.get(i).getSelect4())){
                cell = row.createCell(4);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("1".equals(list.get(i).getSelect5())){
                cell = row.createCell(4);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("1".equals(list.get(i).getSelect6())){
                cell = row.createCell(4);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(4);
                cell.setCellValue("0");
            }


            if ("2".equals(list.get(i).getSelect1())){
                cell = row.createCell(5);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("2".equals(list.get(i).getSelect2())){
                cell = row.createCell(5);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("2".equals(list.get(i).getSelect3())){
                cell = row.createCell(5);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("2".equals(list.get(i).getSelect4())){
                cell = row.createCell(5);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("2".equals(list.get(i).getSelect5())){
                cell = row.createCell(5);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("2".equals(list.get(i).getSelect6())){
                cell = row.createCell(5);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(5);
                cell.setCellValue("0");
            }


            if ("3".equals(list.get(i).getSelect1())){
                cell = row.createCell(6);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("3".equals(list.get(i).getSelect2())){
                cell = row.createCell(6);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("3".equals(list.get(i).getSelect3())){
                cell = row.createCell(6);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("3".equals(list.get(i).getSelect4())){
                cell = row.createCell(6);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("3".equals(list.get(i).getSelect5())){
                cell = row.createCell(6);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("3".equals(list.get(i).getSelect6())){
                cell = row.createCell(6);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(6);
                cell.setCellValue("0");
            }


            if ("4".equals(list.get(i).getSelect1())){
                cell = row.createCell(7);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("4".equals(list.get(i).getSelect2())){
                cell = row.createCell(7);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("4".equals(list.get(i).getSelect3())){
                cell = row.createCell(7);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("4".equals(list.get(i).getSelect4())){
                cell = row.createCell(7);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("4".equals(list.get(i).getSelect5())){
                cell = row.createCell(7);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("4".equals(list.get(i).getSelect6())){
                cell = row.createCell(7);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(7);
                cell.setCellValue("0");
            }


            if ("5".equals(list.get(i).getSelect1())){
                cell = row.createCell(8);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("5".equals(list.get(i).getSelect2())){
                cell = row.createCell(8);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("5".equals(list.get(i).getSelect3())){
                cell = row.createCell(8);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("5".equals(list.get(i).getSelect4())){
                cell = row.createCell(8);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("5".equals(list.get(i).getSelect5())){
                cell = row.createCell(8);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("5".equals(list.get(i).getSelect6())){
                cell = row.createCell(8);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(8);
                cell.setCellValue("0");
            }


            if ("6".equals(list.get(i).getSelect1())){
                cell = row.createCell(9);
                String number1 = list.get(i).getNumber1();
                String s = number1.split(",")[0];
                cell.setCellValue(s);
            }else if ("6".equals(list.get(i).getSelect2())){
                cell = row.createCell(9);
                String number2 = list.get(i).getNumber2();
                String s = number2.split(",")[0];
                cell.setCellValue(s);
            }else if ("6".equals(list.get(i).getSelect3())){
                cell = row.createCell(9);
                String number3 = list.get(i).getNumber3();
                String s = number3.split(",")[0];
                cell.setCellValue(s);
            }else if ("6".equals(list.get(i).getSelect4())){
                cell = row.createCell(9);
                String number4 = list.get(i).getNumber4();
                String s = number4.split(",")[0];
                cell.setCellValue(s);
            }else if ("6".equals(list.get(i).getSelect5())){
                cell = row.createCell(9);
                String number5 = list.get(i).getNumber5();
                String s = number5.split(",")[0];
                cell.setCellValue(s);
            }else if ("6".equals(list.get(i).getSelect6())){
                cell = row.createCell(9);
                String number6 = list.get(i).getNumber6();
                String s = number6.split(",")[0];
                cell.setCellValue(s);
            }else{
                cell = row.createCell(9);
                cell.setCellValue("0");
            }

            System.out.println(list.get(i).getSelect1()+list.get(i).getNumber1()+"-----");
            System.out.println(list.get(i).getSelect2()+list.get(i).getNumber2());
            System.out.println(list.get(i).getSelect3()+list.get(i).getNumber3());
            System.out.println(list.get(i).getSelect4()+list.get(i).getNumber4());
            System.out.println(list.get(i).getSelect5()+list.get(i).getNumber5());
            System.out.println(list.get(i).getSelect6()+list.get(i).getNumber6()+"------");
//            cell.setCellValue(list.get(i).getNumber() == null ? "0" : list.get(i).getNumber());
        }

        File file = new File(basePath);
        if (file.exists()) {
            file.delete();
        }
        //通过输出流将workbook对象下载到磁盘
        FileOutputStream out = new FileOutputStream(basePath);
        workbook.write(out);
        out.flush();
        out.close();
        workbook.close();

    }


    @GetMapping
    public void ex(HttpServletResponse response) {
        System.out.println(basePath);
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(basePath);
            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
