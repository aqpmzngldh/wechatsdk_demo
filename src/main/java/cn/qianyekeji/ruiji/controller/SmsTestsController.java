package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.utils.SMS_TX_Utils;
import cn.qianyekeji.ruiji.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    @PostMapping
    public R<String> save(@RequestBody Sms sms) {

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



        return R.success("今天我也要开心(⊙o⊙)");
    }
}
