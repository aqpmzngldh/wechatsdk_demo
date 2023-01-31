package cn.qianyekeji.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qianyekeji.ruiji.entity.DishFlavor;
import cn.qianyekeji.ruiji.mapper.DishFlavorMapper;
import cn.qianyekeji.ruiji.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper,DishFlavor> implements DishFlavorService {
}
