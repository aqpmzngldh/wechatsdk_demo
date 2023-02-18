package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 匿名聊天
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;
    //当前对话的日期
    private String time;
    //当前对话的内容
    private String body;
    //当前对话的图片
    private String url;

}
