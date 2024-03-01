package cn.qianyekeji.ruiji.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品横幅
 */
@Data
public class Xcx_2Banner implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;
    private String bannerCover;
    private String videoUrl;



}
