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


    @PostMapping("/jsapi/{getNonceStr}/{timestamp}/{productId}")
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId) throws Exception {

        log.info("发起支付请求 v3");

        //返回支付二维码连接和订单号
        R<String> prepay_id= wxPayService.jsapiPay(getNonceStr,timestamp,productId);
        log.info("prepay_id={}",prepay_id);
        return prepay_id;
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
}
