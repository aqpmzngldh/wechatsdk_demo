package cn.qianyekeji.ruiji.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 匿名聊天
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    //当前的经度
    private String lng;
    //当前的纬度
    private String lat;
    //根据经纬度计算的位置信息
//    private String address;
    //根据set集合大小回显不同的修道层次和顺序
    private String address;
}
