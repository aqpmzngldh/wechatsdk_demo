package cn.qianyekeji.ruiji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.qianyekeji.ruiji.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);

}
