package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Xcx_2Category;
import cn.qianyekeji.ruiji.service.Xcx_2CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("xcx_2/category")
@Slf4j
public class Xcx_2CategoryController {
    @Autowired
    private Xcx_2CategoryService xcx_2CategoryService;
    @GetMapping("/addCategory")
    public R<String> list(Xcx_2Category xcx_2Category) {
        System.out.println("传递过来的分类名称是"+xcx_2Category);
        QueryWrapper<Xcx_2Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sort_name", xcx_2Category.getSortName());
        int count = xcx_2CategoryService.count(queryWrapper);
        if (count>0){
            return R.error("已有同名分类");
        }
        xcx_2CategoryService.save(xcx_2Category);

       return R.success("");
    }

//    @GetMapping("/getCategory")
//    public R<List<Xcx_2Category>> getCategory() {
//        List<Xcx_2Category> list = xcx_2CategoryService.list();
//        System.out.println(list);
//        return R.success(list);
//    }
    @GetMapping("/getCategory")
    public R<Page> page(int page, int pageSize){
        //分页构造器
        Page<Xcx_2Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Xcx_2Category> queryWrapper = new LambdaQueryWrapper<>();

        //分页查询
        xcx_2CategoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping("/deleteCategory")
    public R<String> delete(Xcx_2Category xcx_2Category){
        log.info("删除分类，分类为：{}",xcx_2Category.getSortName());
        System.out.println("删除的分类是"+xcx_2Category.getSortName());

        LambdaQueryWrapper<Xcx_2Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Xcx_2Category::getSortName, xcx_2Category.getSortName());
        // 根据sortName作为条件构造器删除
        xcx_2CategoryService.remove(wrapper);
        return R.success("分类信息删除成功");
    }
}
