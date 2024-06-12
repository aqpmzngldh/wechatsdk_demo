package cn.qianyekeji.wechatsdk.mapper;

import cn.qianyekeji.wechatsdk.entity.ChatgptRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatgptMapper extends BaseMapper<ChatgptRequest> {

}
