package cn.qianyekeji.ruiji.service;

import cn.qianyekeji.ruiji.common.R;

import java.security.GeneralSecurityException;
import java.util.Map;

public interface WxPayService {

    R<String> jsapiPay(String getNonceStr, String timestamp, Long productId)throws Exception;
}
