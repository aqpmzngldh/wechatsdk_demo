//package cn.qianyekeji.ruiji.controller;
//
//import cn.qianyekeji.ruiji.common.R;
//import cn.qianyekeji.ruiji.entity.Sms;
//import cn.qianyekeji.ruiji.utils.SMS_TX_Utils;
//import cn.qianyekeji.ruiji.utils.ValidateCodeUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.FileInputStream;
//
//
//@RestController
//@Slf4j
//public class WeiXinController {
//
//    @RequestMapping("/MP_verify_03jsGPvZkHlHOejC.txt")
//    public void id(HttpServletResponse response){
////        return "03jsGPvZkHlHOejC";
//        try {
//            //输入流，通过输入流读取文件内容
//            FileInputStream fileInputStream = new FileInputStream(new File("/MP_verify_03jsGPvZkHlHOejC.txt"));
//            //输出流，通过输出流将文件写回浏览器
//            ServletOutputStream outputStream = response.getOutputStream();
//            response.setContentType("text/plain");
//            response.setHeader("Content-Disposition", "attachment;filename=\"filename.txt\"");
//            int len = 0;
//            byte[] bytes = new byte[1024];
//            while ((len = fileInputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, len);
//                outputStream.flush();
//            }
//
//            //关闭资源
//            outputStream.close();
//            fileInputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequestMapping("/e94e1abec6a39542e13386eaaa1bdb3d.txt")
//    public void id2(HttpServletResponse response){
////        return "03jsGPvZkHlHOejC";
//        try {
//            //输入流，通过输入流读取文件内容
//            FileInputStream fileInputStream = new FileInputStream(new File("/e94e1abec6a39542e13386eaaa1bdb3d.txt"));
//            //输出流，通过输出流将文件写回浏览器
//            ServletOutputStream outputStream = response.getOutputStream();
//            response.setContentType("text/plain");
//            response.setHeader("Content-Disposition", "attachment;filename=\"filename.txt\"");
//            int len = 0;
//            byte[] bytes = new byte[1024];
//            while ((len = fileInputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, len);
//                outputStream.flush();
//            }
//
//            //关闭资源
//            outputStream.close();
//            fileInputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
