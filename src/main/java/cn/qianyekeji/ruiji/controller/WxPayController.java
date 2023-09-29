package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.service.WxPayService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin //跨域
@RestController
@RequestMapping("/api/wx-pay")
@Slf4j
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private Verifier verifier;


    @PostMapping("/jsapi/{getNonceStr}/{timestamp}/{productId}")
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId) throws Exception {

        log.info("发起支付请求 v3");

        //返回支付二维码连接和订单号
        R<String> prepay_id= wxPayService.jsapiPay(getNonceStr,timestamp,productId);
        log.info("prepay_id={}",prepay_id);
        return prepay_id;
    }

}
