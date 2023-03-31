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

    @PostMapping
    public R<String> dianzan(@RequestBody ceshi ceShi) throws Exception {
        System.out.println(ceShi);
        Instant parse = Instant.parse(ceShi.getDate());
        String select1 = ceShi.getSelect1();
        //选择的货这边改一下
        if (select1 != null) {
            String select11 = ceShi.getSelect1();
            if (select11.equals("1")) {
                ceShi.setSelect1("CPU");
            } else if (select11.equals("2")) {
                ceShi.setSelect1("硬盘");
            } else if (select11.equals("2")) {
                ceShi.setSelect1("内存");
            } else if (select11.equals("2")) {
                ceShi.setSelect1("显卡");
            } else if (select11.equals("2")) {
                ceShi.setSelect1("键盘");
            } else if (select11.equals("2")) {
                ceShi.setSelect1("鼠标");
            }
        }
        //日期获取的格式这边改一下
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(parse, ZoneId.systemDefault());
        String formattedDateTime = zonedDateTime.format(formatter);

        ceShi.setDate(formattedDateTime);
        ceShiService.save(ceShi);
        return R.success("1");
    }

    @PostMapping("/excel")
    public R<String> excel() throws Exception {

        List<ceshi> list = ceShiService.list();
        System.out.println(list);
        //在内存中创建一个Excel文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        //创建工作表，指定工作表名称
        XSSFSheet sheet = workbook.createSheet("测试数据表");
        sheet.setColumnWidth(0, (25 * 256));
        sheet.setColumnWidth(1, (25 * 256));
        sheet.setColumnWidth(2, (25 * 256));
        sheet.setColumnWidth(3, (25 * 256));
        sheet.setColumnWidth(4, (25 * 256));
        sheet.setColumnWidth(5, (25 * 256));
        //创建行，0表示第一行
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("送货时间");
        row.createCell(1).setCellValue("送货人姓名");
        row.createCell(2).setCellValue("送货人手机号");
        row.createCell(3).setCellValue("送货车牌");
        row.createCell(4).setCellValue("货品品类");
        row.createCell(5).setCellValue("货品数量");
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
            cell = row.createCell(4);
            cell.setCellValue(list.get(i).getSelect1() == null ? "0" : list.get(i).getSelect1());
            cell = row.createCell(5);
            cell.setCellValue(list.get(i).getNumber() == null ? "0" : list.get(i).getNumber());
        }

        File file = new File("D:\\ceshi.xlsx");
        if (file.exists()) {
            file.delete();
        }
        //通过输出流将workbook对象下载到磁盘
        FileOutputStream out = new FileOutputStream("D:\\ceshi.xlsx");
        workbook.write(out);
        out.flush();
        out.close();
        workbook.close();
        return R.success("1");
    }
}
