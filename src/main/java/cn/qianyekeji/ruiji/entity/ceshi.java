package cn.qianyekeji.ruiji.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ceshi implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String date;
    private String phone;
    private String sign;

    private String select1;
    private String number1;
    private String select2;
    private String number2;
    private String select3;
    private String number3;
    private String select4;
    private String number4;
    private String select5;
    private String number5;
    private String select6;
    private String number6;
}
