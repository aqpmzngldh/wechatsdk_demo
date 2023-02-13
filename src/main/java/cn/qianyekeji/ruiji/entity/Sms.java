package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类
 */
@Data
public class Sms implements Serializable {

    private static final long serialVersionUID = 1L;
    //主键
    private Long id;
    //短信模板ID
    private Integer ids;
    //电话号码
    private String phone;
    //压力测试电话号码
    private String p;
    //ip地址
    private String ipAddress;
    //当前号码调用次数
    private String number;

    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private Long updateUser;
}
