package cn.qianyekeji.ruiji.utils;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;

public class SMS_TX_Utils {
	public static final int REGISTER = 1173965;//注册
	public static final int LOGIN = 1330048;//登录
	public static final int  DEFAULT= 1330046;//默认
	public static final int  SUCCESS= 1331117;//预约成功

	public static final int  tixing= 1353672;//tixing
	public static void main(String[] args) {
//		TX_Utils(1330046,"18392528598","1234");
//		TX_Utils(1353908,"18392528598","03-31 23:13");
		TX_Utils(1330048,"18392528598","1234");
	}
	/**
	 * 登入短信
	 * @param phoneNumber
	 * @param code
	 * @return
	 */ 											//发送的手机号   //随机验证码
	public static String TX_Utils(int templateCode,String phoneNumber,String code) {

		String reStr = ""; //定义返回值
		// 短信应用SDK AppID   // 1400开头
		int appid = 1400587486;
		// 短信应用SDK AppKey
		String appkey = "dde2e8b82a5c16b42940ffb69a30b6ae";
		// 短信模板ID，需要在短信应用中申请
		int templateId = templateCode;
		// 签名，使用的是`签名内容`，而不是`签名ID`
		String smsSign = "千夜大人";
		try {
			//参数，一定要对应短信模板中的参数顺序和个数，
			String[] params = {code};
			//创建ssender对象
			SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
			//发送
			SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumber,templateId, params, smsSign, "", "");
			// 签名参数未提供或者为空时，会使用默认签名发送短信
			System.out.println(result.toString());
			if(result.result==0){
				reStr = "success";
			}else{
				reStr = "error";
			}
		} catch (Exception e) {
			// HTTP响应码错误
			e.printStackTrace();
		}
		return reStr;
	}
}