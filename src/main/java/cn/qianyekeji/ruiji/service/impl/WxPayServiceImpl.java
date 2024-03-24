package cn.qianyekeji.ruiji.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.CustomException;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.config.WxPayConfig;

import cn.qianyekeji.ruiji.entity.*;
import cn.qianyekeji.ruiji.enums.wxpay.WxApiType;
import cn.qianyekeji.ruiji.enums.wxpay.WxNotifyType;

import cn.qianyekeji.ruiji.service.*;

import cn.qianyekeji.ruiji.utils.OrderNoUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;
    @Resource
    private CloseableHttpClient wxPayClient;
    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private DishService dishService;
    @Autowired
    private Xcx_2OrderDataService xcx_2OrderDataService;
    @Autowired
    private Xcx_2GoodsService xcx_2GoodsService;
    @Autowired
    private Xcx_2CartService xcx_2CartService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private CloseableHttpClient wxPayNoSignClient; //无需应答签名

    /*可重入的互斥锁:
    对于同一个线程来说,如果它已经获得了锁,则可以再次获取该锁而不会被阻塞。这就是可重入性。
    对于不同的线程来说,如果一个线程已经获得了锁,其他线程就无法获取该锁,需要等待该线程释放锁后才能获得锁。这就是互斥性。*/
    private final ReentrantLock lock = new ReentrantLock();



    @Transactional(rollbackFor = Exception.class)
    @Override
    public R<String> jsapiPay(String getNonceStr,String timestamp,Long productId,HttpServletRequest request,String xcxOrgongzhonghao,String openidOr,String name2,String out_trade_no) throws Exception {

//2.调用统一下单API
        // 这部分代码的话通过文档中心-指引文档-基础支付-native支付-开发指引中获得
        log.info("调用统一下单API");
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.JSAPI_PAY.getType()));
        // 根据native支付的api字典填写必要的请求参数，这里参数比较多，写进map里
        Map paramsMap = new HashMap();
//        paramsMap.put("appid",wxPayConfig.getAppid());
        if ("xcx".equals(xcxOrgongzhonghao)){
            //千小夜小程序appid
            paramsMap.put("appid",wxPayConfig.getAppid1());
        }else{
            //千小夜公众号appid
            paramsMap.put("appid",wxPayConfig.getAppid());
        }
        paramsMap.put("mchid",wxPayConfig.getMchId());
        paramsMap.put("description",name2);
//        paramsMap.put("out_trade_no", OrderNoUtils.getOrderNo());
        paramsMap.put("out_trade_no", out_trade_no);

//        paramsMap.put("notify_url",wxPayConfig.getNotifyDomain().concat(WxNotifyType.JSAPI_NOTIFY.getType()));
//        if ("999".equals(openidOr)){
//            paramsMap.put("notify_url",wxPayConfig.getNotifyDomain().concat(WxNotifyType.JSAPI_NOTIFY.getType()));
//        }else{
        paramsMap.put("notify_url",wxPayConfig.getNotifyDomain().concat(WxNotifyType.JSAPI_NOTIFY2.getType()));
//        }
        //订单金额里有两个变量，所以再建一个map
        HashMap map = new HashMap<>();
        map.put("total",productId);
        map.put("currency","CNY");
        HashMap map1 = new HashMap<>();
        String openid = (String)request.getSession().getAttribute("openid");
//        map1.put("openid", openid);
//        map1.put("openid", "ofqpF6vyC8VSSKftWwDfwFi237IY");
        if ("xcx".equals(xcxOrgongzhonghao)){
            //小程序进来会传递这个xcx字段，openid也从前端带过来
            map1.put("openid", openidOr);
        }else{
            //公众号里面的支付openid直接从session中取
            map1.put("openid", openid);
        }
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
            Object prepay_id = JSONUtil.parseObj(bodyAsString).get("prepay_id");
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                log.info("成功,响应体 = " + prepay_id);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("native下单失败,状态码 = " + statusCode+ ",返回结果 = " + bodyAsString);
                throw new IOException("request failed");
            }

            System.out.println(timestamp+"---"+getNonceStr);
            // 构建签名字符串
//            String text = "wx61c514e5d83894bf\n"+
//                    timestamp+"\n"+
//                    getNonceStr+"\n"+
//                    "prepay_id="+prepay_id + "\n";
//            String text = "wx902a8a53a4554f9a\n"+
//                    timestamp+"\n"+
//                    getNonceStr+"\n"+
//                    "prepay_id="+prepay_id + "\n";
            String text="";
            if ("xcx".equals(xcxOrgongzhonghao)){
                //千小夜小程序zhifu
                text = "wx902a8a53a4554f9a\n"+
                        timestamp+"\n"+
                        getNonceStr+"\n"+
                        "prepay_id="+prepay_id + "\n";
            }else{
                //千小夜公众号支付
                text = "wx61c514e5d83894bf\n"+
                    timestamp+"\n"+
                    getNonceStr+"\n"+
                    "prepay_id="+prepay_id + "\n";
            }
            long timestamp1 = System.currentTimeMillis();//时间戳

            System.out.println(timestamp1+"=====");

            // 读取商户私钥文件
//            PrivateKey privateKey = PemUtil.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()));
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(wxPayConfig.getPrivateKeyPath());
            PrivateKey privateKey = PemUtil.loadPrivateKey(inputStream);

            // 生成签名
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(text.getBytes("utf-8"));
            byte[] signed = signature.sign();

            // base64编码
            String paySign = Base64.getEncoder().encodeToString(signed);

            R<String> add = R.success("").add("bodyAsString", prepay_id).add("timestamp", timestamp)
                    .add("getNonceStr", getNonceStr).add("paySign", paySign);


            Map<String, String> chatRecord = new HashMap<>();
            chatRecord.put(out_trade_no, add+"");
            redisTemplate.opsForHash().putAll(openidOr, chatRecord);
            redisTemplate.expire(openidOr, 90, TimeUnit.MINUTES);
            return add;

        } finally {
            response.close();
        }
    }






    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processOrder(Map<String, Object> bodyMap, HttpServletRequest request) throws Exception {
        log.info("处理订单");

        //解密报文
        String plainText = decryptFromResource(bodyMap);

        //将明文转换成map,方便取出订单号来更改订单状态
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String)plainTextMap.get("out_trade_no");
        System.out.println("------------------");
        System.out.println(plainTextMap);
        System.out.println("------------------");
        System.out.println(orderNo);
        System.out.println(request.getSession().getAttribute("openid"));
        System.out.println("++++++++++++++++");
        String trade_state_desc = (String)plainTextMap.get("trade_state_desc");
        if ("支付成功".equals(trade_state_desc)){
            //支付成功的话这时候我们把销量加1
            //再把就是这个人的购物车清空
            //第二个就是说清空购物车后前端支付界面的话因为没有刷新，所以所以界面还是原界面，想办法告诉前端给category清空
            Object payer = plainTextMap.get("payer");
            JSONObject entries = JSONUtil.parseObj(payer);
            Object openid = entries.get("openid");
            System.out.println("-------------------");
            System.out.println(openid);
            System.out.println("-------------------");
            //获取了openid后再进行清空这三个操作
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId,openid);
            List<ShoppingCart> list = shoppingCartService.list(wrapper);
            for (ShoppingCart cart : list) {
                Long dishId = cart.getDishId();
                Integer number = cart.getNumber();
                LambdaQueryWrapper<Dish> wrapper1 = new LambdaQueryWrapper<>();
                wrapper1.eq(Dish::getId,dishId);
                Dish dish = dishService.getOne(wrapper1);
                number += dish.getSaleNum();
                // 设置回Dish对象
                dish.setSaleNum(number);

                // 更新Dish数据
                dishService.updateById(dish);
            }

            //清空购物车数据
            shoppingCartService.remove(wrapper);
        }

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processOrder1(Map<String, Object> bodyMap, HttpServletRequest request) throws Exception {
        log.info("处理订单");

        //解密报文
        String plainText = decryptFromResource(bodyMap);

        //将明文转换成map,方便取出订单号来更改订单状态
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String)plainTextMap.get("out_trade_no");
        System.out.println(plainTextMap);
        System.out.println(orderNo);
        String trade_state_desc = (String)plainTextMap.get("trade_state_desc");
        System.out.println("交易状态描述："+trade_state_desc);
        if ("支付成功".equals(trade_state_desc)){
            System.out.println("8888888888888888");
            System.out.println("这个支付成功了哈哈");
            System.out.println("8888888888888888");
            //1.支付成功去数据库改成pay_success支付成功，deliver待发货状态
            QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.eq("out_trade",orderNo);
            List<Xcx_2OrderData> list = xcx_2OrderDataService.list(objectQueryWrapper);
            for (Xcx_2OrderData order : list) {
                order.setPaySuccess("success");
                order.setDeliver("stay");
                // 更新到数据库
                xcx_2OrderDataService.updateById(order);
            //2.库存减少，销量增加
                //获取商品id和商品数量，判断是哪个商品卖了多少件
                String goodsId = order.getGoodsId();
                Integer buyAmount = Integer.valueOf(order.getBuyAmount());

                QueryWrapper<Xcx_2Goods> objectQueryWrapper1 = new QueryWrapper<>();
                objectQueryWrapper1.eq("id",goodsId);
                Xcx_2Goods one = xcx_2GoodsService.getOne(objectQueryWrapper1);
                // 更新库存和销量
                UpdateWrapper<Xcx_2Goods> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", goodsId)
                        .set("stock", Integer.valueOf(one.getStock()) - buyAmount)
                        .set("sold", Integer.valueOf(one.getSold()) + buyAmount);
                xcx_2GoodsService.update(updateWrapper);
            //3.把购物车中的数据删除
                //这时候要注意，删除下单那个人的购物车中的下单数据就行了
                //根据payment字段判断是购物车下单的还是直接下单的，直接下单的就没添加进购物车，就不用删
                //payment字段是cart表示是购物车下单的，这时候我们删除购物车数据，
                String payment = order.getPayment();
                System.out.println("是购物车下单的还是直接下单的"+payment);
                if ("cart".equals(payment)){
                LinkedTreeMap   payer = (LinkedTreeMap)plainTextMap.get("payer");
                String openid = (String) payer.get("openid");
                System.out.println("下单的人为"+payer);
                System.out.println("下单的商品id为"+goodsId);
                QueryWrapper<Xcx_2Cart> objectQueryWrapper2 = new QueryWrapper<>();
                // TODO 这里删除的时候可以选择再拼接一个商品规格字段，要不然购物车有同种商品，但是规格不一样，只是下单支付一个，
                // TODO 另外一个也就删掉了，这里我就先不删除了
                objectQueryWrapper2.eq("goods_id",goodsId).eq("openid",openid);
                xcx_2CartService.remove(objectQueryWrapper2);
                }
            }

        }

    }


    /**
     * 对称解密
     * @param bodyMap
     * @return
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {

        log.info("密文解密");

        //通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");

        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);

        log.info("明文 ===> {}", plainText);

        return plainText;
    }
}
