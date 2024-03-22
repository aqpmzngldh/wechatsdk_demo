package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单数据
 */
@Data
public class Xcx_2OrderData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
    private Long id;

    private String goodsId;
    //    '商品id',
    private String goodsImage;
    //    '商品图片'
    private String goodsTitle;
    //    '商品标题',
    private String goodsPrice;
    //    '商品价格',
    private String buyAmount;
    //    '购买数量',
    private String specs;
    //    '规格',
    private String subtotal;
    //    '总价',
    private String selectOr;
    //    '购物车是否选中'
    private String orderNumber;
    //    '订单编号',
    private String address;
    //    '姓名,手机号码，省市区，详细地址，是否默认选中',
    private String orderTime;
    //    '下单时间:年月日时分秒',
    private String queryTime;
    //    '用于商家查询当天的数据:年月日',
    private String paySuccess;
    //    '支付成功: success,待支付: not_pay,已取消订单: can_order',
    private String deliver;
    //    '待发货: stay,已发货/待收货: already,已收货: rece_goods,退款中: ref_pro, 退款成功: ref_succ',
    private String evaluate;
    //    '待评价:false,已评价:true',
    private String waybillNo;
    //    '运单号',
    private String payment;
    //    '统一下单返回的数据包',
    private String ReReason;
    //    '退款原因',
    private String outTrade;
    //    '商户订单号',
    private String outRefund;
//    '商户退款单号',
    private String openid;
    //用户标识


}
