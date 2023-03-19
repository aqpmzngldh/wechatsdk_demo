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
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
@EnableScheduling
public class addressSeeController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

   /* @GetMapping
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
    }*/


    //上面的操作设计隐私，我们换实现，具体在存储数据进去的时候已经说得很清楚了
    @GetMapping
    public R<Set<Address>> list() {
        String[] arr0 = {"练气期一层", "练气期二层", "练气期三层", "练气期四层", "练气期五层", "练气期六层", "练气期七层", "练气期八层", "练气期九层"};
        String[] arr1 = {"筑基期一层", "筑基期二层", "筑基期三层", "筑基期四层", "筑基期五层", "筑基期六层", "筑基期七层", "筑基期八层", "筑基期九层"};
        String[] arr2 = {"金丹期一层", "金丹期二层", "金丹期三层", "金丹期四层", "金丹期五层", "金丹期六层", "金丹期七层", "金丹期八层", "金丹期九层"};
        String[] arr3 = {"化神期一层", "化神期二层", "化神期三层", "化神期四层", "化神期五层", "化神期六层", "化神期七层", "化神期八层", "化神期九层"};
        String[] arr4 = {"神"};
        HashSet<Address> addresses = new HashSet<>();
        String key = "wcls";
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> set = zSetOps.range(key, 0, -1);
        for (String element : set) {
            long score = zSetOps.score(key, element).longValue();
            int timeDiff = getTimeDiff(score);

            String s = "";
            if (timeDiff != 0) {
                //炼气期一天升一级
                if (timeDiff >= 1 && timeDiff <= 9) {
                    s = arr0[timeDiff - 1];
                }
                //筑基期10天升一级
                if (timeDiff > 9 && timeDiff <= 19) {
                    s = arr1[0];
                }
                if (timeDiff > 19 && timeDiff <= 29) {
                    s = arr1[1];
                }
                if (timeDiff > 29 && timeDiff <= 39) {
                    s = arr1[2];
                }
                if (timeDiff > 39 && timeDiff <= 49) {
                    s = arr1[3];
                }
                if (timeDiff > 49 && timeDiff <= 59) {
                    s = arr1[4];
                }
                if (timeDiff > 59 && timeDiff <= 69) {
                    s = arr1[5];
                }
                if (timeDiff > 69 && timeDiff <= 79) {
                    s = arr1[6];
                }
                if (timeDiff > 79 && timeDiff <= 89) {
                    s = arr1[7];
                }
                if (timeDiff > 89 && timeDiff <= 99) {
                    s = arr1[8];
                }
                //金丹期10天升一级
                if (timeDiff > 99 && timeDiff <= 109) {
                    s = arr2[0];
                }
                if (timeDiff > 109 && timeDiff <= 119) {
                    s = arr2[1];
                }
                if (timeDiff > 119 && timeDiff <= 129) {
                    s = arr2[2];
                }
                if (timeDiff > 129 && timeDiff <= 139) {
                    s = arr2[3];
                }
                if (timeDiff > 139 && timeDiff <= 149) {
                    s = arr2[4];
                }
                if (timeDiff > 149 && timeDiff <= 159) {
                    s = arr2[5];
                }
                if (timeDiff > 159 && timeDiff <= 169) {
                    s = arr2[6];
                }
                if (timeDiff > 169 && timeDiff <= 179) {
                    s = arr2[7];
                }
                if (timeDiff > 179 && timeDiff <= 189) {
                    s = arr2[8];
                }
                //化神期10天升一级
                if (timeDiff > 189 && timeDiff <= 199) {
                    s = arr3[0];
                }
                if (timeDiff > 199 && timeDiff <= 209) {
                    s = arr3[1];
                }
                if (timeDiff > 209 && timeDiff <= 219) {
                    s = arr3[2];
                }
                if (timeDiff > 219 && timeDiff <= 229) {
                    s = arr3[3];
                }
                if (timeDiff > 229 && timeDiff <= 239) {
                    s = arr3[4];
                }
                if (timeDiff > 239 && timeDiff <= 249) {
                    s = arr3[5];
                }
                if (timeDiff > 249 && timeDiff <= 259) {
                    s = arr3[6];
                }
                if (timeDiff > 259 && timeDiff <= 269) {
                    s = arr3[7];
                }
                if (timeDiff > 269 && timeDiff <= 279) {
                    s = arr3[8];
                }
                //神
                if (timeDiff > 279) {
                    s = arr4[0];
                }
                String[] data = element.split("__");
                if (data.length != 2) {
                    continue;
                }
                String latitude = data[0];
                String longitude = data[1];
                Address address = new Address(latitude, longitude, s + "道友");
                addresses.add(address);
            }
        }

        return R.success(addresses);
    }

    public int getTimeDiff(long timestamp) {
        long now = System.currentTimeMillis() / 1000;
        long timeDiff = now - (timestamp / 1000); // 将时间戳转换为秒级别
        if (timeDiff <= 0) {
            return 0;
        } else {
            return (int) Math.ceil(timeDiff / 86400.0); // 向上取整，计算天数
        }
    }


    @PostMapping("/{uuid}/{uuid2}")
    public R<Long> number(@PathVariable("uuid") String uuid, @PathVariable("uuid2") String uuid2) {
//        System.out.println(uuid + "-----------------" + uuid2);
        if (uuid == null) {
            uuid = "1";
        }
        if (uuid2 == null) {
            uuid2 = "_2";
        }
        // 将 uuid 和时间戳信息存储到 Redis 中
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        redisTemplate.opsForZSet().add("lswc", uuid + "_" + uuid2, timestamp);

        // 返回当前 uuid 对应的次数
        Long count = redisTemplate.opsForZSet().zCard("lswc");
        count = count == null ? 0 : count;
        return R.success(count);
    }

    // 每隔3秒执行一次的定时任务
    @Scheduled(fixedDelay = 3000)
    public void removeExpiredElements() {
        // 获取当前时间戳
        long currentTime = Instant.now().toEpochMilli();

        // 获取 Redis 中所有的元素
        Set<ZSetOperations.TypedTuple<String>> elements = redisTemplate.opsForZSet().rangeWithScores("lswc", 0, -1);

        // 遍历所有元素
        for (ZSetOperations.TypedTuple<String> element : elements) {
            String uuid = (String) element.getValue();
            long timestamp = element.getScore().longValue();

            // 如果当前时间与时间戳的差值大于8秒，则删除该元素
            if ((currentTime - timestamp) > 8000) {
                redisTemplate.opsForZSet().remove("lswc", uuid);
            }
        }
    }

    @PostMapping("/id/{userInput}")
    public R<String> person(@PathVariable("userInput") String userInput) {
        String[] parts = new String[0];
        try {
            userInput = new String(Base64.getDecoder().decode(userInput));
            // 使用 opsForHash 方法获取哈希值
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(userInput);

            // 获取地址键的值，并用 "__" 分隔符拆分
            String address = (String) hash.get("address");
            parts = address.split("__");
        } catch (Exception e) {
            return R.error("");
        }

        // 返回地址键的值
        String result = parts[0] + "__" + parts[1];
        return R.success(result);
    }

//    @PostMapping("/address/{address}")
//    public R<String> address(@PathVariable("address") String address) {
//        System.out.println(address+"------------------------");
//        return null;
//    }

    @PostMapping("/address/{address}/{uuid2}")
    public R<List<GeoResult<RedisGeoCommands.GeoLocation<String>>>> address(@PathVariable("address") String address, @PathVariable("uuid2") String uuid2) {
//        System.out.println(address + "------------------------" + uuid2);
        if (uuid2 == null) {
            uuid2 = "_2";
        }
        //获取传入的经纬度
        String[] coordinates = address.split("__");
        String longitude = coordinates[0];
        String latitude = coordinates[1];

        Point point = new Point(Double.parseDouble(longitude), Double.parseDouble(latitude));
        String key = "geoaddress";
        String member = address;

        //在将获取的经纬度存入Redis的Geo数据类型中之前，这时候我们获取在线的sortedset在线人员lswc键中所有的value
        //然后将这些value存入geo数据类型
        Set<String> onlineUsers = redisTemplate.opsForZSet().range("lswc", 0, -1);
        if (!onlineUsers.isEmpty()) {
            Map<String, Point> members = new HashMap<>();
            for (String user : onlineUsers) {
                String userWithoutEnding = user.substring(0, user.lastIndexOf("_"));
                String[] userCoordinates = userWithoutEnding.split("__");
                String userLongitude = userCoordinates[0];
                String userLatitude = userCoordinates[1];
                Point userPoint = new Point(Double.parseDouble(userLongitude), Double.parseDouble(userLatitude));
                members.put(userWithoutEnding, userPoint);
            }
            redisTemplate.opsForGeo().add(key, members);
        }
        // 再将传入的经纬度存储到Redis的Geo数据类型中
        redisTemplate.opsForGeo().add(key, point, member);
        //这时候geo数据类型中有两部分，一部分是传入的经纬度
        // 另一部分是在线人员(这里的在线人员指的只是大厅里的人员)的经纬度
        Circle circle = new Circle(point, new Distance(3000000, Metrics.MILES));
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius(key, circle);
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> resultList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResults) {
            RedisGeoCommands.GeoLocation<String> location = geoResult.getContent();
            // 判断当前遍历到的location是否是传入的经纬度
            if (!location.getName().equals(member)) {
                double distance = redisTemplate.opsForGeo().distance(key, member, location.getName(), Metrics.MILES).getValue();
                resultList.add(new GeoResult<>(location, new Distance(distance, Metrics.MILES)));
            }
        }
//        System.out.println(resultList);
        //清空geo数据类型中的所有内容
        redisTemplate.delete(key);
        return R.success(resultList);
    }

    @PostMapping("/delete/{param1}/{param2}")
    public R<String> delete(@PathVariable("param1") String param1,@PathVariable("param2") String param2) {
        char[] chars = param1.toCharArray();
        Arrays.sort(chars);
        String sortedS = new String(chars);
        param2 = new String(Base64.getDecoder().decode(param2));
        String s = sortedS + "," + param2;
        System.out.println(s);
        Boolean delete = redisTemplate.delete(s);
        System.out.println(delete);
        return R.success("1");
    }

}
