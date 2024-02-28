package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Xcx_2Category;
import cn.qianyekeji.ruiji.service.Xcx_2CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/xcx_2")
@Slf4j
public class Xcx_2CategoryController {
    @Autowired
    private Xcx_2CategoryService xcx_2CategoryService;
    @Value("${ruiji.path2}")
    private String basePath;

    @GetMapping("/category/addCategory")
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
    @GetMapping("/category/getCategory")
    public R<Page> page(int page, int pageSize){
        //分页构造器
        Page<Xcx_2Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Xcx_2Category> queryWrapper = new LambdaQueryWrapper<>();

        //分页查询
        xcx_2CategoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping("/category/deleteCategory")
    public R<String> delete(Xcx_2Category xcx_2Category){
        log.info("删除分类，分类为：{}",xcx_2Category.getSortName());
        System.out.println("删除的分类是"+xcx_2Category.getSortName());

        LambdaQueryWrapper<Xcx_2Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Xcx_2Category::getSortName, xcx_2Category.getSortName());
        // 根据sortName作为条件构造器删除
        xcx_2CategoryService.remove(wrapper);
        return R.success("分类信息删除成功");
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
//        String sss="F:\\www\\server\\img2\\";
        // 获取上传的文件
        if (file.isEmpty()) {
            System.out.println("上传的文件为空");
        }else{
            System.out.println("上传的文件不不不为空");
        }
        String originalFilename = file.getOriginalFilename();
        log.info("传递过来的文件名为，{}",originalFilename);

        // 创建目标文件对象
//        File dest = new File(sss + originalFilename);
        File dest = new File(basePath + originalFilename);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        try {
            // 将上传的文件保存到服务器
            file.transferTo(dest);
//            System.out.println("文件上传成功，保存路径为：" + sss + originalFilename);
            System.out.println("文件上传成功，保存路径为：" + basePath + originalFilename);
        } catch (IOException e) {
            System.out.println("文件上传失败: " + e.getMessage());
        }
        return "https://qianyekeji.cn/img2/"+originalFilename;
    }

    @GetMapping("/getCategoryAll")
    public R<List<Xcx_2Category>> getCategoryAll() {
        List<Xcx_2Category> list = xcx_2CategoryService.list();
        System.out.println(list);
        return R.success(list);
    }
//    @PostMapping("/addGoods")
////    public String addGoods(String goods_title,@RequestBody String[] goods_banner,String goods_cover,String video_url) {
//    public String addGoods(String obj) {
////        System.out.println(goods_title);
////        System.out.println(goods_banner);
////        System.out.println(goods_cover);
////        System.out.println(video_url);
//
//        System.out.println(obj);
//        return "333";
//    }
}
