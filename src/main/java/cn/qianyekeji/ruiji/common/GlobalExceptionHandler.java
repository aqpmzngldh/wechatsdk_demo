package cn.qianyekeji.ruiji.common;

/**
 * @author liangshuai
 * @date 2023/1/22
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * 弹出的异常是java.sql.SQLIntegrityConstraintViolationException,所以这里直接写SQLIntegrityConstraintViolationException.class
     * @return
     */
    @ResponseBody
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
//        因为重复的时候是肯定会弹这个日志的，所以我们再在SQLIntegrityConstraintViolationException基础上确认下
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 异常处理方法
     * @return
     */
//    @ResponseBody
//    @ExceptionHandler(CustomException.class)
//    public R<String> exceptionHandler(CustomException ex){
//        log.error(ex.getMessage());
//
//        return R.error(ex.getMessage());
//    }

    @ResponseBody
    @ExceptionHandler(CustomException.class)
    public String exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        if (ex.getMessage().equals("跳转界面了")){
            return "redirect:/front/page/3.html";
        }
        return null;
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public R<String> exceptionHandler(IllegalArgumentException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
