package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.JiaXiao;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//驾校一点通小程序后端接口
@RestController
@RequestMapping("/jiaxiao")
@Slf4j
public class jiaxiao {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PostMapping("/wxLogin")
//    public R<String> access_token(String data) {
    public R<String> access_token(HttpServletRequest request) {

        System.out.println("驾校小程序入口");
        // 获取请求参数
//        String appid = request.getParameter("appid");
//        System.out.println(appid);
//        String secret = request.getParameter("secret");
//        System.out.println(secret);
        //因为上面两个参数appid和secret不能直接在前端展示，不符合小程序规范，所以在后端写
        String appid="wxf88c1f139cc9011a";
        String secret="5c61c913504bc153234471f8e6b4f917";
        String code = request.getParameter("code");

        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
        // 发送GET请求
        HttpResponse response = HttpUtil.createGet(url).execute();
        if (response.isOk()) {
            String responseBody = response.body();
            System.out.println("responseBody==="+responseBody);
            Map<String, Object> map1 = JSONUtil.parseObj(responseBody);
            System.out.println("map1===,"+map1);
            String openid = (String) map1.get("openid");
            System.out.println("openid==="+openid);
            return R.success(openid);
        } else {
            // 处理错误
            System.err.println("Failed.............." + response.getStatus());
        }
        return null;
    }

    @GetMapping("/getScore")
    public R<List<JiaXiao>> getScore(Integer correctFrequency,String openid,HttpServletRequest request) {

        String score = String.valueOf(correctFrequency * 2);
        System.out.println("驾校小程序提交当前成绩，返回最近十次成绩");
        System.out.println("传递过来的成绩是" + score);
        System.out.println("小程序的open是，" + openid);

        //把这次提交的分数进行保存到redis
        Boolean openid11 = redisTemplate.opsForHash().hasKey("xcx_1", openid);
        if (openid11) {
            Object openid1 = redisTemplate.opsForHash().get("xcx_1", openid);
            String timeScoreStr111 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "_" + score;
            String s = openid1 + "," + timeScoreStr111;
            redisTemplate.opsForHash().put("xcx_1", openid, s);

            //能进这里说明不止一条，是多条，我们设置一下，让他最多存储十条
            String value = (String)redisTemplate.opsForHash().get("xcx_1", openid);
            String[] items = value.split(",");
            int length = items.length;
            if(length > 10) {
                // 进行删除最早的数据
                value = value.substring(value.indexOf(",") + 1); // 删除第一个","及其前数据
                redisTemplate.opsForHash().put("xcx_1", openid, value);
            }
        } else {
            String timeScoreStr11 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "_" + score;
            redisTemplate.opsForHash().put("xcx_1", openid, timeScoreStr11);
        }
        //对最近10次成绩进行取出
        String openid1111 = (String) redisTemplate.opsForHash().get("xcx_1", openid);
//        2024-02-17 11:01_99分,2024-02-17 11:01_99分
//        对openid111先进行，分割
//        分割后再进行下划线_分割
//        创建实体类对象，返回
        String[] pairs = openid1111.split(",");
        ArrayList<JiaXiao> jiaXiaos = new ArrayList<>();
//        for (String pair : pairs) {
        for (int i=pairs.length-1;i>=0;i--) {
            String[] parts = pairs[i].split("_");
            String time = parts[0];
            String score1 = parts[1];
            System.out.println("时间: " + time + ", 分数: " + score1);
            JiaXiao jiaXiao = new JiaXiao();
            jiaXiao.setTime(time);
            jiaXiao.setScore(score1);
            jiaXiaos.add(jiaXiao);
        }
        System.out.println(jiaXiaos);
        return R.success(jiaXiaos);
    }
}
