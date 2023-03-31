package cn.qianyekeji.ruiji.entity;

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
    private String number;

}
