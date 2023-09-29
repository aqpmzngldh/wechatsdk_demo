package cn.qianyekeji.ruiji.service;

import java.security.GeneralSecurityException;
import java.util.Map;

public interface WxPayService {

    String jsapiPay(Long productId)throws Exception;
}
