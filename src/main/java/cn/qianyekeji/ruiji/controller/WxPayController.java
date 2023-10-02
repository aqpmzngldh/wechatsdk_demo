package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.WxPayService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin //跨域
@RestController
@RequestMapping("/api/wx-pay")
@Slf4j
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private CeShiService ceShiService;


    @PostMapping("/jsapi/{getNonceStr}/{timestamp}/{productId}")
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId) throws Exception {

        log.info("发起支付请求 v3");

        //返回支付二维码连接和订单号
        R<String> prepay_id= wxPayService.jsapiPay(getNonceStr,timestamp,productId);
        log.info("prepay_id={}",prepay_id);
        return prepay_id;
    }
}
