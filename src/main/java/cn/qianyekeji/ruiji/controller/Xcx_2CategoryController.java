package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.*;
import cn.qianyekeji.ruiji.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xcx_2")
@Slf4j
public class Xcx_2CategoryController {
    @Autowired
    private Xcx_2CategoryService xcx_2CategoryService;
    @Autowired
    private Xcx_2GoodsService xcx_2GoodsService;
    @Autowired
    private Xcx_2SkuDataService xcx_2SkuDataService;
    @Autowired
    private Xcx_2BannerService xcx_2BannerService;
    @Autowired
    private Xcx_2SeckillService xcx_2SeckillService;
    @Autowired
    private Xcx_2VideoCommentService xcx_2VideoCommentService;
    @Autowired
    private Xcx_2UserInfoService xcx_2UserInfoService;

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

    @PostMapping("/category/deleteCategoryQuantity")
    public void deleteCategoryQuantity(String category){
        System.out.println(category);
        //根据传递过来的category数据，我们需要让分类表里面的quantity-1，然后商品表里面的是否上架变成false
//        1.1分类表里面的quantity-1
        Xcx_2Category sort_name = xcx_2CategoryService.getOne(new QueryWrapper<Xcx_2Category>().eq("sort_name", category));
        System.out.println(sort_name);
        // 如果找到了记录
        if (category != null) {
            // 修改 quantity 字段的值
            int newQuantity = Integer.parseInt(sort_name.getQuantity()) - 1;
            sort_name.setQuantity(String.valueOf(newQuantity));
            // 构建更新条件
            UpdateWrapper<Xcx_2Category> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("sort_name", sort_name.getSortName());
            // 根据条件更新
            xcx_2CategoryService.update(sort_name, updateWrapper);
        }
//        1.2商品表里面的是否上架shelves字段变成false
        List<Xcx_2Goods> list = xcx_2GoodsService.list(new QueryWrapper<Xcx_2Goods>().eq("category", category));
        if (category != null) {
            for (int i = 0; i < list.size(); i++) {
                // 修改 shelves 字段的值
                list.get(i).setShelves("false");
                // 构建更新条件
                UpdateWrapper<Xcx_2Goods> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("category", category);
                // 根据条件更新
                xcx_2GoodsService.update(list.get(i), updateWrapper);
            }
        }
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
    @GetMapping("/getCategoryOther")
    public R<List<Xcx_2Category>> getCategoryOther() {
        LambdaQueryWrapper<Xcx_2Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Xcx_2Category::getQuantity,0);
        List<Xcx_2Category> list = xcx_2CategoryService.list(wrapper);
        return R.success(list);
    }
    @PostMapping("/addGoods")
    public String addGoods(Xcx_2Goods xcx_2Goods){
        System.out.println(xcx_2Goods);

        Xcx_2Goods xcx_2Goods1 = new Xcx_2Goods();
        xcx_2Goods1.setGoodsTitle(xcx_2Goods.getGoodsTitle());
        xcx_2Goods1.setGoodsBanner(xcx_2Goods.getGoodsBanner());
        xcx_2Goods1.setGoodsCover(xcx_2Goods.getGoodsCover());
        xcx_2Goods1.setVideoUrl(xcx_2Goods.getVideoUrl());
        xcx_2Goods1.setCategory(xcx_2Goods.getCategory());
        xcx_2Goods1.setGoodsPrice(xcx_2Goods.getGoodsPrice());
        xcx_2Goods1.setStock(xcx_2Goods.getStock());
        xcx_2Goods1.setSku(xcx_2Goods.getSku());
        xcx_2Goods1.setGoodsDetails(xcx_2Goods.getGoodsDetails());
        xcx_2Goods1.setSold(xcx_2Goods.getSold());
        xcx_2Goods1.setShelves(xcx_2Goods.getShelves());
        xcx_2Goods1.setSeckill(xcx_2Goods.getSeckill());
        // 插入数据
        boolean success =xcx_2GoodsService.save(xcx_2Goods1);
        // 获取插入的数据的ID
        Long insertedId = xcx_2Goods1.getId();
        System.out.println("Inserted ID: " + insertedId);
        if (success) {
            System.out.println("插入数据成功");
            //获取分类下的quantity的数量
            LambdaQueryWrapper<Xcx_2Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Xcx_2Category::getSortName, xcx_2Goods1.getCategory());
            Xcx_2Category one = xcx_2CategoryService.getOne(wrapper);
            if (one==null){
                return "该分类已被删除，请重新添加分类";
            }
            String quantity = one.getQuantity();
            //进行对quantity的数量的更新
            UpdateWrapper<Xcx_2Category> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("sort_name", xcx_2Goods.getCategory()); // 设置更新的条件
            Xcx_2Category user = new Xcx_2Category();

            System.out.println(quantity);
            System.out.println(String.valueOf(Integer.parseInt(quantity)+1));// 设置要更新的字段的新值
            if (quantity!=null) {
                user.setQuantity(String.valueOf(Integer.parseInt(quantity)+1)); // 设置要更新的字段的新值
            }else{
                user.setQuantity("1");
            }
            xcx_2CategoryService.update(user, updateWrapper); // 根据条件更新数据
        } else {
            System.out.println("插入数据失败");
        }

        return String.valueOf(insertedId);
    }

    //直接查询当前分类下的所有商品有一点效率不高，这段注释了，用分页查询来做
//    @PostMapping("/selectGoods")
//    public R<List<Xcx_2Goods>> selectGoods(Xcx_2Goods xcx_2Goods){
//        String category = xcx_2Goods.getCategory();
//        System.out.println(category);
//        LambdaQueryWrapper<Xcx_2Goods> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Xcx_2Goods::getCategory,category);
//        List<Xcx_2Goods> list = xcx_2GoodsService.list(wrapper);
//        System.out.println(list);
//        return R.success(list);
//    }


    @GetMapping("/selectGoods")
    public R<Page> selectGoods(int page, int pageSize,Xcx_2Goods xcx_2Goods){
        String category = xcx_2Goods.getCategory();
        System.out.println("------"+category+"------");
        String videoUrl = xcx_2Goods.getVideoUrl();
        System.out.println("------"+videoUrl+"------");
        //分页构造器
        Page<Xcx_2Goods> pageInfo = new Page<>(page,pageSize);
        if (category!=null){
            System.out.println("不等于null");
        LambdaQueryWrapper<Xcx_2Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Xcx_2Goods::getCategory,category);
        xcx_2GoodsService.page(pageInfo,wrapper);
        }else if(videoUrl!=null){
            QueryWrapper<Xcx_2Goods> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("goods_title", videoUrl)
                    .or()
                    .like("category", videoUrl);
            xcx_2GoodsService.page(pageInfo,queryWrapper);
        }else{
            System.out.println("等于null");
            QueryWrapper<Xcx_2Goods> wrapper = new QueryWrapper<>();
            wrapper.eq("shelves","true").orderByDesc("sold"); // 添加根据 sold 字段进行降序排序条件;
        xcx_2GoodsService.page(pageInfo,wrapper);
        }
        return R.success(pageInfo);
    }

    /**
     * 存储商品的规格信息
     * @param sku
     * @param sku_id
     * @return
     */
    @PostMapping("/addSkudata")
    public String addSkudata(String sku,String sku_id){
        System.out.println(sku_id);
        System.out.println(sku);
        System.out.println(sku.length());
        System.out.println(sku==null);
        if (sku.length()==2) {
            System.out.println("进来了1");
            return "";
        }
        System.out.println("没进来");

//        JSONArray jsonArray = JSONUtil.parseArray(sku);
//        // 遍历 JSON 数组
//        for (Object obj : jsonArray) {
//             将每个 JSON 对象转换为 Map
//            Map map = JSONUtil.parseObj(obj);
//            System.out.println("map: " + map);
//
//            String image = (String)map.get("image");
//            System.out.println("image="+image);
//            BigDecimal price = (BigDecimal)map.get("price");
//            System.out.println("price="+price);
//            String stock = String.valueOf(map.get("stock"));
//            System.out.println("stock="+stock);
//            String title = String.valueOf(map.get("title"));
//            System.out.println("title="+title);
//            Object att_data = map.get("att_data");
//            System.out.println(att_data);

//            Xcx_2SkuData xcx_2SkuData = new Xcx_2SkuData();
//            xcx_2SkuData.setId(Integer.parseInt(sku_id));
//            xcx_2SkuData.setPrice(price);
//            xcx_2SkuData.setStock(stock);
//            xcx_2SkuData.setTitle(title);
//            xcx_2SkuData.setImage(image);
//            xcx_2SkuDataService.save(xcx_2SkuData);

            // 获取 att_data
//            JSONArray attDataArray = (JSONArray) map.get("att_data");
//            for (Object attDataObj : attDataArray) {
////                JSONObject attDataJson = (JSONObject) attDataObj;
//                Map attDataJson = JSONUtil.parseObj(attDataObj);
////                String attName = attDataJson.getStr("att_name");
//                String attName = (String) attDataJson.get("att_name");
////                String attVal = attDataJson.getStr("att_val");
//                String attVal = (String) attDataJson.get("att_val");
//                System.out.println(attName+"----"+attVal);
//
//                Xcx_2SkuDataAttdata xcx_2SkuDataAttdata = new Xcx_2SkuDataAttdata();
//                xcx_2SkuDataAttdata.setAtt_name(attName);
//                xcx_2SkuDataAttdata.setAtt_val(attVal);
//                xcx_2SkuDataAttdataService.save(xcx_2SkuDataAttdata);
//            }
//        }

        //上面那部分到时候用户端取数据的时候参考，这会我直接把整个存进去
        Xcx_2SkuData xcx_2SkuData = new Xcx_2SkuData();
        xcx_2SkuData.setId(Integer.parseInt(sku_id));
        xcx_2SkuData.setSku(sku);
        xcx_2SkuDataService.save(xcx_2SkuData);
        return null;
    }

    @GetMapping("/selectBannerAll")
    public R<List<Xcx_2Banner>> selectBannerAll(){
        List<Xcx_2Banner> list = xcx_2BannerService.list();
        System.out.println(list);
        return R.success(list);
    }

    @GetMapping("/addBanner")
    public void addBanner(String banner_cover,Integer goods_id,String video_url){
        Xcx_2Banner xcx_2Banner = new Xcx_2Banner();
        xcx_2Banner.setId(goods_id);
        xcx_2Banner.setBannerCover(banner_cover);
        xcx_2Banner.setVideoUrl(video_url);
        // 插入数据
        boolean success =xcx_2BannerService.save(xcx_2Banner);
        System.out.println(success);
    }

    @GetMapping("/deleteBanner")
    public void deleteBanner(String id){
        // 删除数据
        xcx_2BannerService.removeById(id);
    }

    @GetMapping("/selectSeckillAll")
    public R<List<Xcx_2Seckill>> selectSeckillAll(){
        List<Xcx_2Seckill> list = xcx_2SeckillService.list();
        System.out.println(list);
        return R.success(list);
    }

    @PostMapping("/saveSeckill")
    public void saveSeckill(int id,String obj){
        System.out.println(id);
        System.out.println(obj);
        //秒杀数据存入秒杀数据库表中
        Xcx_2Seckill xcx_2Seckill = new Xcx_2Seckill();
        xcx_2Seckill.setId(id);
        xcx_2Seckill.setSeckillData(obj);
        xcx_2SeckillService.save(xcx_2Seckill);
        //去商品表里面给这个秒杀关联的商品的秒杀属性改成true
//        select * from 表名 where id=方法传入的id set seckill=true
        Xcx_2Goods id1 = xcx_2GoodsService.getOne(new QueryWrapper<Xcx_2Goods>().eq("id", id));
        id1.setSeckill("true");
        // 构建更新条件
        UpdateWrapper<Xcx_2Goods> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        // 根据条件更新
        xcx_2GoodsService.update(id1, updateWrapper);
    }
    @GetMapping("/deleteSeckill")
    public void deleteSeckill(String id){
        // 删除数据哦打视频都怕
        xcx_2SeckillService.removeById(id);
    }


    @PostMapping("/wxLogin")
    public R<String> access_token(HttpServletRequest request) {

        System.out.println("点击按钮获取用户openid");
        String appid="wx902a8a53a4554f9a";
        String secret="e207e88f7b7117c02852b1a9a757a6b7";
        String code = request.getParameter("code");
        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
        // 发送GET请求
        HttpResponse response = HttpUtil.createGet(url).execute();
        if (response.isOk()) {
            String responseBody = response.body();
            System.out.println("responseBody==="+responseBody);
            Map<String, Object> map1 = JSONUtil.parseObj(responseBody);
            System.out.println("map1===,"+map1);
            String openid = (String) map1.get("openid");
            System.out.println("openid==="+openid);
            return R.success(openid);
        } else {
            // 处理错误
            System.err.println("Failed.............." + response.getStatus());
        }
        return null;
    }

    @GetMapping("/selectOneGood")
    public R<Xcx_2Goods> selectOneGood(Xcx_2Goods xcx_2Goods){

        Xcx_2Goods byId = xcx_2GoodsService.getById(xcx_2Goods.getId());
        return R.success(byId);
    }

    @GetMapping("/selectCommentNumber")
    public R<Integer> selectCommentNumber(Xcx_2Goods xcx_2Goods){
        Long id = xcx_2Goods.getId();
        QueryWrapper<Xcx_2VideoComment> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id).isNull("eav_image");
        int size = xcx_2VideoCommentService.list(objectQueryWrapper).size();
        return R.success(size);
    }

    @GetMapping("/collectOr")
    public Boolean collectOr(Xcx_2UserInfo xcx_2UserInfo){
        System.out.println("判断是否收藏");
        Integer id = xcx_2UserInfo.getId();
//        String openid = xcx_2UserInfo.getOpenid();
        String openid = "oYU2I5RKQRZcSHGwIBC1l9Yt34Iw";
        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id).eq("openid",openid);
        Boolean collect = null;
        try {
            collect = xcx_2UserInfoService.getOne(objectQueryWrapper).getCollect();
        } catch (Exception e) {
            return false;
        }
        System.out.println(collect);
        return collect;
    }

    //如果有秒杀价的话就不显示原价了
    @GetMapping("/selectSeckillPrice")
    public R<Xcx_2Seckill>  selectSeckillPrice(Xcx_2Goods xcx_2Goods){
        System.out.println("查看秒杀价");
        return R.success(xcx_2SeckillService.getById(xcx_2Goods.getId()));
    }

    @GetMapping("/addUserInfo")
    public void  addUserInfo(Xcx_2UserInfo xcx_2UserInfo){
        System.out.println("添加用户信息");

        Integer id = xcx_2UserInfo.getId();
        String openid = xcx_2UserInfo.getOpenid();
        String avatarUrl = xcx_2UserInfo.getAvatarUrl();
        String nickName = xcx_2UserInfo.getNickName();
        Boolean collect = xcx_2UserInfo.getCollect();
        System.out.println(id+","+openid+","+avatarUrl+","+nickName+","+collect);

        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid).eq("id",id);
        Xcx_2UserInfo one = xcx_2UserInfoService.getOne(objectQueryWrapper);
        if (one==null){
            //这里不应该直接存，应该在存之前先取一下，存在的话我们给收藏字段取反就好了
            xcx_2UserInfoService.save(xcx_2UserInfo);
            return;
        }
        System.out.println("hahaha");
        one.setCollect(collect);
        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper1 = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid).eq("id",id);
        xcx_2UserInfoService.update(one,objectQueryWrapper1);
    }

    @GetMapping("/addVideoComment")
    public void  addVideoComment(Xcx_2VideoComment xcx_2VideoComment){
        System.out.println("添加用户的评论");
        System.out.println(xcx_2VideoComment);
        xcx_2VideoCommentService.save(xcx_2VideoComment);

    }

//    @GetMapping("/selectVideoComment")
//    public R<List<Xcx_2VideoComment>>  selectVideoComment(Xcx_2VideoComment xcx_2VideoComment){
//        System.out.println("加载用户的评论");
//        System.out.println(xcx_2VideoComment);
//        QueryWrapper<Xcx_2VideoComment> objectQueryWrapper = new QueryWrapper<>();
//        objectQueryWrapper.eq("id",xcx_2VideoComment.getId());
//        List<Xcx_2VideoComment> list = xcx_2VideoCommentService.list(objectQueryWrapper);
//        return R.success(list);
//
//    }

    @GetMapping("/selectVideoComment")
    public R<Page>  selectVideoComment(int page, int pageSize,Xcx_2VideoComment xcx_2VideoComment){
        System.out.println("分页加载用户的评论");
        Page<Xcx_2VideoComment> pageInfo = new Page<>(page,pageSize);

        QueryWrapper<Xcx_2VideoComment> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",xcx_2VideoComment.getId()).isNull("eav_image");
        xcx_2VideoCommentService.page(pageInfo,objectQueryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/selectOneGoods")
    public R<Xcx_2Goods> selectOneGoods(Xcx_2Goods xcx_2Goods){
        System.out.println("根据id查询单个商品");
        Xcx_2Goods byId = xcx_2GoodsService.getById(xcx_2Goods.getId());
        return R.success(byId);
    }

    @GetMapping("/selectOneSeckill")
    public R<Xcx_2Seckill>  selectOneSeckill(Xcx_2Seckill xcx_2Seckill){
        System.out.println("根据id查询秒杀商品");
        return R.success(xcx_2SeckillService.getById(xcx_2Seckill.getId()));
    }

}
