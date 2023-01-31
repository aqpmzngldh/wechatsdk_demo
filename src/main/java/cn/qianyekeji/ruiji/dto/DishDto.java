package cn.qianyekeji.ruiji.dto;

import cn.qianyekeji.ruiji.entity.Dish;
import cn.qianyekeji.ruiji.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
