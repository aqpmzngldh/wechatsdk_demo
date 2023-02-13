package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Employee;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.service.EmployeeService;
import cn.qianyekeji.ruiji.service.SmsService;
import cn.qianyekeji.ruiji.utils.IpLocation;
import cn.qianyekeji.ruiji.utils.SMS_TX_Utils;
import cn.qianyekeji.ruiji.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
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

    @PostMapping
    public R<String> save(@RequestBody Sms sms,HttpServletRequest request) {
        String p = sms.getP();
        try {
            // 假设 HttpServletRequest 对象为 request
            String ipAddress = IpLocation.getIpAddress();
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

        try {
                String URL = "http://qianyekeji.cn/sms";
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("id", 1330048);
                paramMap.put("phone", sms.getP());
                HttpEntity<Map> entity = new HttpEntity<>(paramMap, headers);
                restTemplate.postForEntity(URL, entity, String.class);
            } catch (RestClientException e) {
                e.printStackTrace();
            }
        try {
            String URL = "https://www.yuque.com/api/validation_codes";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("referer", "https://www.yuque.com/login?goto=https%3A%2F%2Fwww.yuque.com%2Fbeikai%2Fogf5fx%2Fzw1f7u%3F");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("action", "login");
            paramMap.put("channel", "sms");
            paramMap.put("target", sms.getP());
            HttpEntity<Map> entity = new HttpEntity<>(paramMap, headers);
            restTemplate.postForEntity(URL, entity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }



        return R.success("测试已启动");
    }
}
