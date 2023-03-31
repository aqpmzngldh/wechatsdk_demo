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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(parse, ZoneId.systemDefault());
        String formattedDateTime = zonedDateTime.format(formatter);

        ceShi.setDate(formattedDateTime);
        ceShiService.save(ceShi);
        return R.success("1");
    }
}
