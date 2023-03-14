package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Address;
import cn.qianyekeji.ruiji.entity.Chat;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.GiteeUploader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/addressSee")
@Slf4j
public class addressSeeController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping
    public R<Set<Address>> list() {
        String[] arr = {"渡劫期", "大乘期", "合体期", "炼虚期", "化神期", "元婴期", "金丹期", "筑基期", "练气期"};
        HashSet<Address> addresses = new HashSet<>();
        String key = "wcls";
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> set = zSetOps.range(key, 0, -1);
        for (String element : set) {
            long score = zSetOps.score(key, element).longValue();
            int index = 0;
            if (score!=0L) {
                if (score == 1) {
                    index = 0;
                } else if (score > 1 && score <= 10) {
                    index = 1;
                } else if (score > 10 && score <= 20) {
                    index = 2;
                } else if (score > 20 && score <= 30) {
                    index = 3;
                }else if (score > 30 && score <= 40) {
                    index = 4;
                }else if (score > 40 && score <= 50) {
                    index = 5;
                }else if (score > 50 && score <= 60) {
                    index = 6;
                }else if (score > 60 && score <= 70) {
                    index = 7;
                }else if (score > 70) {
                    index = 8;
                }

                String[] data = element.split("__");
                if (data.length != 2) {
                    continue;
                }
                String latitude = data[0];
                String longitude = data[1];
                Address address = new Address(latitude, longitude, arr[index]+score+"号道友");
                addresses.add(address);
            }
        }

        return R.success(addresses);
    }
}
