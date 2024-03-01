package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品
 */
@Data
public class Xcx_2Goods implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * _id:'该商品的唯一标示,商品id',
     * 		goods_title:'商品标题',
     * 		goods_banner:[{商品横幅,image:''}],
     * 		goods_cover:'商品封面图,商品横幅的第一张图片作为封面图',
     * 		video_url:'短视频链接',
     * 		category:'所属分类',
     * 		goods_price:'商品价格',
     * 		stock:'库存',
     * 		sku:'true or false 是否有sku规格',
     * 		goods_details:[{商品详情图,image:''}],
     * 		sold:'商品已售多少',
     * 		shelves:'商品上架与否 true or false',
     * 		seckill:'该商品是否参与秒杀 true or false'
     */
    @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
    private Long id;
//    private Long id;
//    private String goods_title;
    private String goodsTitle;
    private String goodsBanner;
    private String goodsCover;
    private String videoUrl;
    private String category;
    private String goodsPrice;
    private String stock;
    private String sku;
    private String goodsDetails;
    private String sold;
    private String shelves;
    private String seckill;



}
