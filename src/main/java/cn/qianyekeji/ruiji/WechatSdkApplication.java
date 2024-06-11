package cn.qianyekeji.ruiji;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author liangshuai
 * @date 2023/1/18
 */
@Slf4j
@SpringBootApplication
public class WechatSdkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WechatSdkApplication.class, args);
        log.info("项目启动");
    }
}
