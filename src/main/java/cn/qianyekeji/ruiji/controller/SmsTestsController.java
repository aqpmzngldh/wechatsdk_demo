package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.utils.SMS_TX_Utils;
import cn.qianyekeji.ruiji.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@RestController
@RequestMapping("/sss")
@Slf4j
public class SmsTestsController {
    @PostMapping
    public R<String> save(@RequestBody Sms sms) {
        String s=sms.getPhone();

        String URL = "https://www.chanel.cn/zh_CN/fragrance-beauty/services/local/sms/send";
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.postForObject(U, , )


        return R.success("开发中，请等待。。。");
    }
}
