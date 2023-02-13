package cn.qianyekeji.ruiji.mapper;

import cn.qianyekeji.ruiji.entity.Employee;
import cn.qianyekeji.ruiji.entity.Sms;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author liangshuai
 * @date 2023/1/21
 */
@Mapper
public interface SmsMapper extends BaseMapper<Sms> {
}
