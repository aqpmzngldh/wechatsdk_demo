package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 咸鱼之王用户提交数据
 */
@Data
public class Xcx_2Xian implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
    private Long id;

    private String openid;
    private String price;
    private String stock;
    private String price1;
    private String stock1;
    private String time;




}
