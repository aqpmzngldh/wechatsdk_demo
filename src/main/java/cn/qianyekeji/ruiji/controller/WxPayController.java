package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.service.CeShiService;
import cn.qianyekeji.ruiji.service.WxPayService;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
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

    //pdf下载到了static路径下
//    @RequestMapping("/pdf")
//    public String convertImageToPDF() throws IOException, DocumentException {
//
//        // 生成一个唯一的文件名，以避免文件名冲突
//        String fileName = UUID.randomUUID().toString() + ".pdf";
//
//        // 指定保存新PDF文件的路径，这里假设保存在resources目录下
//        String outputPath = "src/main/resources/static/" + fileName;
//
//        // 1.创建Document对象
//        Document document = new Document(PageSize.A4);
//
//        // 2.创建PdfWriter对象，指定生成的PDF的输出流
//        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));
//
//        // 3.打开document
//        document.open();
//
//        // 4.读取图像文件为字节数组
//        byte[] imageBytes = readImageBytes("image.jpg"); // 自定义方法读取图像文件为字节数组
//
//        // 5.将图像字节数组转换为Image对象
//        Image image = Image.getInstance(imageBytes);
//// 设置图像尺寸适应页面大小
//        image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
//        // 6.将图片添加到PDF文档
//        document.add(image);
//
//        // 7.关闭document
//        document.close();
//
//        // 返回新PDF文件的相对路径
//        return "static/" + fileName;
//    }
//
//    // 自定义方法，将图像文件读取为字节数组
//    private byte[] readImageBytes(String imagePath) throws IOException {
//        // 使用Spring的ClassPathResource获取资源文件的输入流
//        ClassPathResource resource = new ClassPathResource(imagePath);
//        InputStream inputStream = resource.getInputStream();
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        return outputStream.toByteArray();
//    }


    //pdf通过浏览器直接下载
//    @RequestMapping("/pdf")
//    public void downloadPDF(HttpServletResponse response) throws IOException, DocumentException {
//        // 生成一个唯一的文件名，以避免文件名冲突
//        String fileName = UUID.randomUUID().toString() + ".pdf";
//
//        // 设置响应头，告诉浏览器这是一个PDF文件
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
//
//        // 获取响应的输出流
//        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//
//        // 创建Document对象
//        Document document = new Document(PageSize.A4);
//
//        // 创建PdfWriter对象，指定生成的PDF的输出流
//        PdfWriter writer = PdfWriter.getInstance(document, pdfOutputStream);
//
//        // 打开document
//        document.open();
//
//        // 读取图像文件为字节数组
//        byte[] imageBytes = readImageBytes("image.jpg"); // 自定义方法读取图像文件为字节数组
//
//        // 将图像字节数组转换为Image对象
//        Image image = Image.getInstance(imageBytes);
//        // 设置图像尺寸适应页面大小
//        image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
//
//        // 将图片添加到PDF文档
//        document.add(image);
//
//        // 关闭document
//        document.close();
//
//        // 将生成的PDF写入响应的输出流
//        response.getOutputStream().write(pdfOutputStream.toByteArray());
//
//        // 刷新输出流
//        response.getOutputStream().flush();
//    }
//
//    // 自定义方法，将图像文件读取为字节数组
//    private byte[] readImageBytes(String imagePath) throws IOException {
//        // 使用Spring的ClassPathResource获取资源文件的输入流
//        ClassPathResource resource = new ClassPathResource(imagePath);
//        InputStream inputStream = resource.getInputStream();
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        return outputStream.toByteArray();
//    }


    //pdf通过浏览器直接下载,图片通过形参传递
    @RequestMapping("/pdf/{serverId}")
    public void downloadPDF(@PathVariable String serverId) throws IOException {

        String accessToken = ceShiService.access_token();

        // 构建微信接口请求URL
        String url = "https://api.weixin.qq.com/cgi-bin/media/get";

        // 拼接参数
        url += "?access_token=" + accessToken;
        url += "&media_id=" + serverId;

        // 获取用户目录
        String userDir = System.getProperty("user.home");

        // 拼接完整的输出路径
        String outputPath = userDir + "/downloaded_image.jpg";

        HttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = httpClient.execute(httpGet);

        HttpEntity entity = response.getEntity();

        if (entity != null) {

            // 获取图片字节数组
            byte[] imageBytes = EntityUtils.toByteArray(entity);

            // 写入文件
            FileOutputStream fos = new FileOutputStream(outputPath);
            fos.write(imageBytes);
            fos.close();

        }

        System.out.println("图片已保存到用户目录下");

    }
}
