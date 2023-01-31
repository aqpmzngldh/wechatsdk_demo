package cn.qianyekeji.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qianyekeji.ruiji.entity.ShoppingCart;
import cn.qianyekeji.ruiji.mapper.ShoppingCartMapper;
import cn.qianyekeji.ruiji.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
