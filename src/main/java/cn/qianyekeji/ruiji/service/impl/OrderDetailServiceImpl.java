package cn.qianyekeji.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qianyekeji.ruiji.entity.OrderDetail;
import cn.qianyekeji.ruiji.mapper.OrderDetailMapper;
import cn.qianyekeji.ruiji.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}