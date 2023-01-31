package cn.qianyekeji.ruiji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qianyekeji.ruiji.entity.User;
import cn.qianyekeji.ruiji.mapper.UserMapper;
import cn.qianyekeji.ruiji.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{
}
