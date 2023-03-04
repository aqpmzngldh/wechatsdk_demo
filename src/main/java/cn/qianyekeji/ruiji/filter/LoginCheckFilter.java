package cn.qianyekeji.ruiji.filter;

import cn.qianyekeji.ruiji.common.BaseContext;
import cn.qianyekeji.ruiji.common.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liangshuai
 * @date 2023/1/22
 */
@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 黑马头条中是通过拦截器进行拦截操作的，瑞吉外卖里面用的是过滤器
     *
     * 访问有些页面的时候是不需要进行过滤的，比如登录页面，访问登录页面如果不让人家过的话那校验是否登录就不行
     * 找出这些页面后获取用户访问的请求看是不是包含在这些页面里，是的话直接放行
     * 不是的话我们再判断用户是否登录，这时候就用到session了，因为我们登录成功的用户在session里面存储了它的信息
     * 没登录的话直接重定向或者转发到登录页面，这里是响应数据后，结合前段request.js中对响应的拦截器两个一起判断的，都可以
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html

        // 判断请求是否是根路径
        if (requestURI.equals("/")) {
            // 将请求重定向到默认访问页面
            response.sendRedirect("/front/page/chat.html");
            return;
        }
//        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//移动端发送短信
                "/user/login",//移动端登录
                "/dish/list",//菜品缓存后再访问跳过登录
                "/sms/**",//免费短信接口的
                "/sss/**",//短信压力测试的
                "/chat/**",//短信压力测试的
                "/MP_verify_03jsGPvZkHlHOejC.txt",
                "/e94e1abec6a39542e13386eaaa1bdb3d.txt"
//                ,
//                "/category/list",
//                "/shoppingCart/list"


        };


        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if(check){
//            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            /*因为在设置了公共字段填充后，对于createUser和updateUser本来的数据是从session中获取的，但是
                    在实现了metaObjecthandler接口的方法中不能获取session，这时候我们就要通过threadlocal解决*/

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
