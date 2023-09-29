package cn.qianyekeji.ruiji.enums.wxpay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WxApiType {

	/**
	 * Native下单
	 */
	NATIVE_PAY("/v3/pay/transactions/native"),

	/**
	 * jsapi下单
	 */
	JSAPI_PAY("/v3/pay/transactions/jsapi"),

    /**
     * Native下单v2
     */
    NATIVE_PAY_V2("/pay/unifiedorder"),

	/**
	 * 查询订单
	 */
	ORDER_QUERY_BY_NO("/v3/pay/transactions/out-trade-no/%s"),

	/**
	 * 关闭订单
	 */
	CLOSE_ORDER_BY_NO("/v3/pay/transactions/out-trade-no/%s/close"),

	/**
	 * 申请退款
	 */
	DOMESTIC_REFUNDS("/v3/refund/domestic/refunds"),

	/**
	 * 查询单笔退款
	 */
	DOMESTIC_REFUNDS_QUERY("/v3/refund/domestic/refunds/%s"),

	/**
	 * 申请交易账单
	 */
	TRADE_BILLS("/v3/bill/tradebill"),

	/**
	 * 申请资金账单
	 */
	FUND_FLOW_BILLS("/v3/bill/fundflowbill");


	/**
	 * 类型
	 */
	private final String type;
}
