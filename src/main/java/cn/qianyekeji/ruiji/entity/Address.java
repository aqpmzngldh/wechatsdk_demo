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

}
