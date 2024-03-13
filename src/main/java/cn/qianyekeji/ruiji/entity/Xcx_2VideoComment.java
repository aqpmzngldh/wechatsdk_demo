package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 短视频评论数据库
 */
@Data
public class Xcx_2VideoComment implements Serializable {

    private static final long serialVersionUID = 1L;

 @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
   private Integer primary_key;
    //关联的商品id
    private Integer id;

    //头像
    private String avatarUrl;
    //昵称
    private String nickName;
    //评论时间
    private String time;
    //评论内容
    private String content;



}
