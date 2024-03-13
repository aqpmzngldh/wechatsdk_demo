package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息数据库
 */
@Data
public class Xcx_2UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO) // 设置主键生成策略为数据库自动生成
    private Integer primary_key;
    //关联的商品id
    private Integer id;
    //头像
    private String avatarUrl;
    //昵称
    private String nickName;
    //用于支付时的监听
    private String pay;
    //用于支付时的监听
    private String watchNum;
    //用户在小程序的唯一标识
    private String openid;
    //用户是否收藏
    private Boolean collect;


}
