package cn.qianyekeji.ruiji.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类
 */
@Data
public class Sms implements Serializable {

    private static final long serialVersionUID = 1L;

    //短信模板ID
    private Integer id;
    //电话号码
    private String phone;
    //压力测试电话号码
    private String p;

}
