package cn.qianyekeji.ruiji.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 小程序-答题返回成绩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiaXiao implements Serializable {

    private static final long serialVersionUID = 1L;
    //当前的时间
    private String time;
    //当前的分数
    private String score;
}
