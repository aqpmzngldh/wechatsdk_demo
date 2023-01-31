package cn.qianyekeji.ruiji.dto;

import cn.qianyekeji.ruiji.entity.Setmeal;
import cn.qianyekeji.ruiji.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
