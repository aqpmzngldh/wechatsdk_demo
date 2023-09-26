package cn.qianyekeji.ruiji.service;

import cn.qianyekeji.ruiji.entity.AddressBook;
import cn.qianyekeji.ruiji.entity.ceshi;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CeShiService extends IService<ceshi> {
    String access_token();
    String getOpenid(String code);
}
