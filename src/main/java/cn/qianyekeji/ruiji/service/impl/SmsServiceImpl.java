package cn.qianyekeji.ruiji.service.impl;

import cn.qianyekeji.ruiji.entity.Employee;
import cn.qianyekeji.ruiji.entity.Sms;
import cn.qianyekeji.ruiji.mapper.EmployeeMapper;
import cn.qianyekeji.ruiji.mapper.SmsMapper;
import cn.qianyekeji.ruiji.service.EmployeeService;
import cn.qianyekeji.ruiji.service.SmsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author liangshuai
 * @date 2023/1/21
 */
@Service
public class SmsServiceImpl extends ServiceImpl<SmsMapper, Sms> implements SmsService {
}
