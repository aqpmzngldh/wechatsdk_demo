package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品规格
 */
@Data
public class Xcx_2SkuData implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;
    private String sku;



}
