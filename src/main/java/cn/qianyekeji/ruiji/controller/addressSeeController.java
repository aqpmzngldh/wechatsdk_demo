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
        // 定义中国的经纬度范围
        double minLongitude = 73.446960;
        double maxLongitude = 135.084727;
        double minLatitude = 18.153520;
        double maxLatitude = 53.563500;
        HashSet<Address> addresses = new HashSet<>();
        // 定义Set集合的key
        String key = "wcls";
        // 使用RedisTemplate获取Set集合的元素
        Set<String> set = redisTemplate.opsForSet().members(key);
        // 遍历Set集合的元素，解析其中的数据
        for (String element : set) {
            // 使用Java的字符串操作方法，按照"__"分割元素中的数据
            String[] data = element.split("__");
            if (data.length != 2) {
                // 数据格式不正确，忽略该元素
                continue;
            }
            // 获取元素中的纬度数据
            String latitude = data[0];
            // 获取元素中的经度数据
            String longitude = data[1];
            // 判断经纬度是否在中国范围内，如果不在则忽略该元素
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            if (lat < minLatitude || lat > maxLatitude || lng < minLongitude || lng > maxLongitude) {
                continue;
            }
            Address address = new Address(latitude, longitude);
            addresses.add(address);
        }

        return R.success(addresses);
    }
}
