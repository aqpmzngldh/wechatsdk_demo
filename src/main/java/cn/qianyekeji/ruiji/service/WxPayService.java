package cn.qianyekeji.ruiji.service;

import cn.qianyekeji.ruiji.common.R;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface WxPayService {

    R<String> jsapiPay(String getNonceStr, String timestamp, Long productId)throws Exception;

    void processOrder(Map<String, Object> bodyMap, HttpServletRequest request)throws Exception;
}
