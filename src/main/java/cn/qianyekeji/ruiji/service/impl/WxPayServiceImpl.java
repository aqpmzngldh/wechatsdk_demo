package cn.qianyekeji.ruiji.service.impl;

import cn.qianyekeji.ruiji.config.WxPayConfig;

import cn.qianyekeji.ruiji.enums.wxpay.WxApiType;
import cn.qianyekeji.ruiji.enums.wxpay.WxNotifyType;

import cn.qianyekeji.ruiji.service.WxPayService;

import cn.qianyekeji.ruiji.utils.OrderNoUtils;

import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;
    @Resource
    private CloseableHttpClient wxPayClient;

    @Resource
    private CloseableHttpClient wxPayNoSignClient; //无需应答签名

    /*可重入的互斥锁:
    对于同一个线程来说,如果它已经获得了锁,则可以再次获取该锁而不会被阻塞。这就是可重入性。
    对于不同的线程来说,如果一个线程已经获得了锁,其他线程就无法获取该锁,需要等待该线程释放锁后才能获得锁。这就是互斥性。*/
    private final ReentrantLock lock = new ReentrantLock();



    @Transactional(rollbackFor = Exception.class)
    @Override
    public String jsapiPay(Long productId) throws Exception {

//2.调用统一下单API
        // 这部分代码的话通过文档中心-指引文档-基础支付-native支付-开发指引中获得
        log.info("调用统一下单API");
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.JSAPI_PAY.getType()));
        // 根据native支付的api字典填写必要的请求参数，这里参数比较多，写进map里
        Map paramsMap = new HashMap();
        paramsMap.put("appid",wxPayConfig.getAppid());
        paramsMap.put("mchid",wxPayConfig.getMchId());
        paramsMap.put("description","测试");
        paramsMap.put("out_trade_no", OrderNoUtils.getOrderNo());
        paramsMap.put("notify_url",wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        //订单金额里有两个变量，所以再建一个map
        HashMap map = new HashMap<>();
        map.put("total",1 );
        map.put("currency","CNY");
        HashMap map1 = new HashMap<>();
        map1.put("openid", "ofqpF6vyC8VSSKftWwDfwFi237IY");
        //塞进去
        paramsMap.put("amount", map);
        paramsMap.put("payer", map1);

        //因为本来案例要的是sting，这里用gson给map转成string
        Gson gson = new Gson();
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求（注意这里发送请求的时候用的wxPayClient是包签名和验签的）
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

//3.对响应做出处理，提取需要的数据code_url（二维码链接）
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                log.info("成功,响应体 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("native下单失败,状态码 = " + statusCode+ ",返回结果 = " + bodyAsString);
                throw new IOException("request failed");
            }

            return bodyAsString;

        } finally {
            response.close();
        }
    }
}
