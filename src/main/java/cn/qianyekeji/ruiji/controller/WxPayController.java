package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.WxPayService;
import cn.qianyekeji.ruiji.utils.HttpUtils;
import cn.qianyekeji.ruiji.utils.OrderNoUtils;
import cn.qianyekeji.ruiji.utils.WechatPay2ValidatorForRequest;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
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

    @Resource
    private Verifier verifier;


    @PostMapping("/jsapi/{getNonceStr}/{timestamp}/{productId}")
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId,HttpServletRequest request) throws Exception {

        log.info("发起支付请求 v3");

        //返回支付二维码连接和订单号
        String orderNo = OrderNoUtils.getOrderNo();
        R<String> prepay_id= wxPayService.jsapiPay(getNonceStr,timestamp,productId,request,"999","999","千小夜商城",orderNo);
        log.info("prepay_id={}",prepay_id);
        return prepay_id;
    }

    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     * 通知是微信主动给我们发的，我们也要进行验签，之前的签名和验签都封装在了httpclient调用excute中去了
     * 在这里我们把逻辑从之前的源码中拿出来，创建WechatPay2ValidatorForRequest并改写
     * 验签都差不多，只不过一个是响应的验签，这个是请求的验签
     */
    @PostMapping("/jsapi/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response){

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();//应答对象

        try {

            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String)bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);
            //log.info("支付通知的完整数据 ===> {}", body);
            //int a = 9 / 0;

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                    = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if(!wechatPay2ValidatorForRequest.validate(request)){

                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");

            //验签成功了，确定是自己人了，接下来我们再从微信请求体里获取数据来处理订单
            wxPayService.processOrder(bodyMap,request);

            //应答超时
            //模拟接收微信端的重复通知
//            TimeUnit.SECONDS.sleep(5);

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);

        } catch (Exception e) {
            e.printStackTrace();
            //失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }

    }


    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // 获取文件名
        String filename = request.getParameter("filename");

        // 构造文件对象
        File file = new File("/www/server/tomcat/999/" + filename);

        // 设置response头部信息
        response.setContentType(request.getServletContext().getMimeType(filename));
        response.setContentLength((int)file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // 使用FileChannel读取文件内容到ByteBuffer
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fileChannel = raf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect((int)fileChannel.size());
        fileChannel.read(buffer);

        ServletOutputStream outputStream = response.getOutputStream();

        FileInputStream inputStream = new FileInputStream(file);

        byte[] buffer1 = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer1)) > 0) {
            outputStream.write(buffer1, 0, length);
        }

        inputStream.close();
        outputStream.close();
    }

    @RequestMapping("/Q1SbMpygd1.txt")
    public void Q1SbMpygd1(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // 获取文件名
        String filename = request.getParameter("filename");

        // 构造文件对象
        File file = new File("/www/server/tomcat/999/" + filename);

        // 设置response头部信息
        response.setContentType(request.getServletContext().getMimeType(filename));
        response.setContentLength((int)file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // 使用FileChannel读取文件内容到ByteBuffer
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fileChannel = raf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect((int)fileChannel.size());
        fileChannel.read(buffer);

        ServletOutputStream outputStream = response.getOutputStream();

        FileInputStream inputStream = new FileInputStream(file);

        byte[] buffer1 = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer1)) > 0) {
            outputStream.write(buffer1, 0, length);
        }

        inputStream.close();
        outputStream.close();
    }
}
