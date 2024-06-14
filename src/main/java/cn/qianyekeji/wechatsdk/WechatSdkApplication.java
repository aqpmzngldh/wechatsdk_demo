package cn.qianyekeji.wechatsdk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@Slf4j
@SpringBootApplication
public class WechatSdkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WechatSdkApplication.class, args);
        log.info("项目启动");
    }
}
