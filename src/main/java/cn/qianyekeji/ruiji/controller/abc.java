package cn.qianyekeji.ruiji.controller;

import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;

import java.io.File;
import java.util.Collections;

import static com.qcloud.cos.demo.BucketRefererDemo.cosClient;

/**
 * @author liangshuai
 * @date 2023/2/17
 */
public class abc {
    public static void main(String[] args) {
        // 指定要上传的文件
        File localFile = new File("D:\\Backup\\Downloads\\1.jpg");
        // 指定文件将要存放的存储桶
        String bucketName = "qianykeji-1304134216";
        // 指定文件上传到 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
        String key = "img/1.jpg";
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        System.out.println(putObjectResult);
    }
}
