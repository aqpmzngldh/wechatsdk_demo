package cn.qianyekeji.ruiji.config;

import cn.qianyekeji.ruiji.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.ClassPath;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author liangshuai
 * @date 2023/1/18
 */
@Configuration
@Slf4j
//public class WebMvcConfiguration extends WebMvcConfigurationSupport {
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开启静态资源映射");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");

        // 静态资源映射
        registry.addResourceHandler("/img2/**")     // 映射路径, 其中的img可以随便改
                .addResourceLocations("file:/www/server/img2/");  // 服务器中存放图片的路径

        registry.addResourceHandler("/niuniu/**")
                .addResourceLocations("file:/www/server/img2/niuniu/");
        registry.addResourceHandler("/ba/**")
                .addResourceLocations("file:/www/server/img2/ba/");
        registry.addResourceHandler("/ma/**")
                .addResourceLocations("file:/www/server/img2/ma/");
    }

    /**
     * 扩展mvc框架的消息转换器
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }
}
