package cn.qianyekeji.ruiji.mapper;

import cn.qianyekeji.ruiji.entity.ChatRequest;
import cn.qianyekeji.ruiji.entity.ceshi;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatGptMapper extends BaseMapper<ChatRequest> {

}
