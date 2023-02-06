package cn.qianyekeji.ruiji;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author liangshuai
 * @date 2023/1/18
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan//不写这个的话过滤器不生效
@EnableCaching //开启Spring Cache注解方式是缓存功能
public class RuijiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuijiApplication.class,args);
        log.info("项目启动");
    }
}
