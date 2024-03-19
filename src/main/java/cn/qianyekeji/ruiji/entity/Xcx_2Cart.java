package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车数据
 */
@Data
public class Xcx_2Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
    private Long id;

    private String goodsId;
    private String openid;
    private String goodsImage;
    private String goodsTitle;
    private String goodsPrice;
    private String buyAmount;
    private String subtotal;
    private String selectOr;
    private String specs;



}
