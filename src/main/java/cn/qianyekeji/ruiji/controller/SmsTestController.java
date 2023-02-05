package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.utils.SMS_TX_Utils;
import cn.qianyekeji.ruiji.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/sms")
@Slf4j
public class SmsTestController {

    public static final int REGISTER = 1173965;//注册
    public static final int LOGIN = 1330048;//登录

    /**
     * 朋友发送短信让他调这个接口，直接给ak，sk权限太大了
     *
     * @param
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Sms sms) {
        boolean abc=false;
        if (sms.getId()==1173965||sms.getId()==1330048){
            abc=true;
        }
        if (sms.getId() != null && StringUtils.isNotEmpty(sms.getPhone())&&(abc)) {
                Integer integer = ValidateCodeUtils.generateValidateCode(4);
                String s = String.valueOf(integer);
                SMS_TX_Utils.TX_Utils(sms.getId(), sms.getPhone(), s);
                return R.success(s);
        }else {
            return R.success("传递参数不合法");
        }
    }
}
