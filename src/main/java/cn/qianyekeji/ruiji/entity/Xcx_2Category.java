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
public class Xcx_2Category implements Serializable {

    private static final long serialVersionUID = 1L;


    //分类名称
    private String sortName;


    //分类名称下的数量
    private String quantity;



}
