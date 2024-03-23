package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.*;
import cn.qianyekeji.ruiji.service.*;
import cn.qianyekeji.ruiji.utils.HttpUtils;
import cn.qianyekeji.ruiji.utils.WechatPay2ValidatorForRequest;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
    @Autowired
    private Xcx_2CartService xcx_2CartService;
    @Autowired
    private Xcx_2AddressService xcx_2AddressService;
    @Resource
    private WxPayService wxPayService;
    @Resource
    private Verifier verifier;
    @Autowired
    private Xcx_2OrderDataService xcx_2OrderDataService;

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

//    @GetMapping("/collectOr")
//    public Boolean collectOr(Xcx_2UserInfo xcx_2UserInfo){
//        System.out.println("判断是否收藏");
//        Integer id = xcx_2UserInfo.getId();
////        String openid = xcx_2UserInfo.getOpenid();
//        String openid = "oYU2I5RKQRZcSHGwIBC1l9Yt34Iw";
//        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper = new QueryWrapper<>();
//        objectQueryWrapper.eq("id",id).eq("openid",openid);
//        Boolean collect = null;
//        try {
//            collect = xcx_2UserInfoService.getOne(objectQueryWrapper).getCollect();
//        } catch (Exception e) {
//            return false;
//        }
//        System.out.println(collect);
//        return collect;
//    }

    @GetMapping("/collectOr")
    public R<Boolean> collectOr(Xcx_2UserInfo xcx_2UserInfo){
        System.out.println("判断是否收藏");
        Integer id = xcx_2UserInfo.getId();
        String openid = xcx_2UserInfo.getOpenid();
        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id).eq("openid",openid);
        Boolean collect = null;
        try {
            collect = xcx_2UserInfoService.getOne(objectQueryWrapper).getCollect();
        } catch (Exception e) {
            return R.success(false);
        }
        System.out.println(collect);
        return R.success(collect);
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
        objectQueryWrapper1.eq("openid",openid).eq("id",id);
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

    @GetMapping("/selectVideoCommentAll")
    public R<Integer> selectVideoCommentAll(Xcx_2VideoComment xcx_2VideoComment){
        System.out.println("查询id为xcx_2VideoComment.getId()的商品详情页的所有评论数量");
        Integer id = xcx_2VideoComment.getId();
        QueryWrapper<Xcx_2VideoComment> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id).isNotNull("eav_image");
        int size = xcx_2VideoCommentService.list(objectQueryWrapper).size();
        return R.success(size);
    }

    @GetMapping("/selectVideoCommentThree")
    public R<Page> selectVideoCommentThree(int page, int pageSize,Xcx_2VideoComment xcx_2VideoComment){
        System.out.println("评价前三条数据");
        Integer id = xcx_2VideoComment.getId();
        //分页构造器
        Page<Xcx_2VideoComment> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        QueryWrapper<Xcx_2VideoComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id).isNotNull("eav_image");

        //分页查询
        xcx_2VideoCommentService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/selectOneSkuData")
    public R<Xcx_2SkuData> selectOneSkuData(Xcx_2SkuData xcx_2SkuData){
        System.out.println("查询当前商品的sku数据");
        Xcx_2SkuData byId = xcx_2SkuDataService.getById(xcx_2SkuData.getId());
        return R.success(byId);
    }

    @GetMapping("/getCartAll")
    public R<Integer> getCartAll(Xcx_2Cart xcx_2Cart) {
        System.out.println("查询某人购物车全部条数");
        String openid = xcx_2Cart.getOpenid();
        QueryWrapper<Xcx_2Cart> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2Cart> list = xcx_2CartService.list(objectQueryWrapper);
        return R.success(list.size());
    }


    @PostMapping("/getCartSame")
    public R<Integer> getCartSame(Xcx_2Cart xcx_2Cart) {
        System.out.println("判断购物车数据库是否出现相同的数据");
        String goodsId = xcx_2Cart.getGoodsId();
        String specs = xcx_2Cart.getSpecs();
        String openid = xcx_2Cart.getOpenid();
        System.out.println("接收的数据为"+goodsId+","+specs+","+openid);

        QueryWrapper<Xcx_2Cart> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid).eq("specs",specs).eq("goods_id",goodsId);
        List<Xcx_2Cart> list = xcx_2CartService.list(objectQueryWrapper);
        System.out.println(list.size());
        return R.success(list.size());
    }

    @PostMapping("/addCart")
    public void addCart(String data,String openid) {
        System.out.println("判断数据到购物车");
        System.out.println(JSON.parse(data));
        Map parse = (Map) JSON.parse(data);

        Xcx_2Cart xcx_2Cart = new Xcx_2Cart();
        xcx_2Cart.setGoodsId((String) parse.get("goods_id"));
        xcx_2Cart.setBuyAmount(String.valueOf(parse.get("buy_amount")));
        xcx_2Cart.setGoodsImage((String) parse.get("goods_image"));
        xcx_2Cart.setGoodsPrice(String.valueOf(parse.get("goods_price")));
        xcx_2Cart.setGoodsTitle((String) parse.get("goods_title"));
        xcx_2Cart.setOpenid(openid);
        xcx_2Cart.setSelectOr(""+parse.get("select"));
        xcx_2Cart.setSpecs(""+parse.get("specs"));
        xcx_2Cart.setSubtotal(""+parse.get("subtotal"));

        xcx_2CartService.save(xcx_2Cart);

    }

    @PostMapping("/selectAddress")
    public R<List<Xcx_2Address>> addCart(String openid) {
        System.out.println("查询用户地址数据库");
        QueryWrapper<Xcx_2Address> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2Address> list = xcx_2AddressService.list(objectQueryWrapper);
        return R.success(list);

    }

    @PostMapping("/selectAddressMo")
    public R<List<Xcx_2Address>> selectAddressMo(String openid,String tacitly) {
        System.out.println("查询默认的用户地址数据库");
        QueryWrapper<Xcx_2Address> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid).eq("tacitly",tacitly);
        List<Xcx_2Address> list = xcx_2AddressService.list(objectQueryWrapper);
        return R.success(list);

    }

    @PostMapping("/addAddress")
    public void addAddress(String data,String openid) {
        System.out.println("新增用户收货地址");
        //在新增用户收货地址的时候，先给数据库中所有的都设置为false，因为新增的地址一般都是想用的
        QueryWrapper<Xcx_2Address> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2Address> list = xcx_2AddressService.list(objectQueryWrapper);
        for (Xcx_2Address xcx_2Address1 : list) {
            xcx_2Address1.setTacitly("false");
            xcx_2AddressService.updateById(xcx_2Address1);

        }


        Map parse = (Map) JSON.parse(data);

        Xcx_2Address xcx_2Address = new Xcx_2Address();
        xcx_2Address.setAddress(parse.get("address")+"");
        xcx_2Address.setDistrict(parse.get("district")+"");
        xcx_2Address.setMobile(parse.get("mobile")+"");
        xcx_2Address.setName(parse.get("name")+"");
        xcx_2Address.setOpenid(openid);
        xcx_2Address.setTacitly(parse.get("tacitly")+"");

        xcx_2AddressService.save(xcx_2Address);

    }

    @PostMapping("/deleteAddressById")
    public void deleteAddressById(String id) {
        System.out.println("删除用户收货地址");
        xcx_2AddressService.removeById(id);
    }

    @PostMapping("/updateAddress")
    public void updateAddress(String id,String data,String openid) {
        System.out.println("修改用户收货地址");
        Map parse = (Map) JSON.parse(data);

        Xcx_2Address xcx_2Address = new Xcx_2Address();
        xcx_2Address.setAddress(parse.get("address")+"");
        xcx_2Address.setDistrict(parse.get("district")+"");
        xcx_2Address.setMobile(parse.get("mobile")+"");
        xcx_2Address.setName(parse.get("name")+"");
        xcx_2Address.setOpenid(openid);
        xcx_2Address.setTacitly(parse.get("tacitly")+"");

        QueryWrapper<Xcx_2Address> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id);
        xcx_2AddressService.update(xcx_2Address,objectQueryWrapper);
    }

    @PostMapping("/updateAddressById")
    public void updateAddressById(String openid,String id) {
        System.out.println("设置默认用户收货地址");
        QueryWrapper<Xcx_2Address> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2Address> list = xcx_2AddressService.list(objectQueryWrapper);
        for (Xcx_2Address xcx_2Address : list) {

            if (!(xcx_2Address.getId()+"").equals(id)) {
                xcx_2Address.setTacitly("false");
                xcx_2AddressService.updateById(xcx_2Address);
            }else{
                xcx_2Address.setTacitly("true");
                xcx_2AddressService.updateById(xcx_2Address);
            }

        }

    }

    //发起支付
    @PostMapping("/jsapi/{getNonceStr}/{timestamp}/{productId}")
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId,HttpServletRequest request,String xcxOrgongzhonghao,String openidOr,String name2,String out_trade_no) throws Exception {

        log.info("发起支付请求 v3");
        System.out.println(xcxOrgongzhonghao);
        //返回支付二维码连接和订单号
        R<String> prepay_id= wxPayService.jsapiPay(getNonceStr,timestamp,productId,request,xcxOrgongzhonghao,openidOr,name2,out_trade_no);
        log.info("prepay_id={}",prepay_id);
        return prepay_id;
    }


    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     * 通知是微信主动给我们发的，我们也要进行验签，之前的签名和验签都封装在了httpclient调用excute中去了
     * 在这里我们把逻辑从之前的源码中拿出来，创建WechatPay2ValidatorForRequest并改写
     * 验签都差不多，只不过一个是响应的验签，这个是请求的验签
     */
    @PostMapping("/jsapi/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response){
        System.out.println("你有没有来呢呀呀呀");
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();//应答对象

        try {

            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String)bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);
            //log.info("支付通知的完整数据 ===> {}", body);
            //int a = 9 / 0;

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                    = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if(!wechatPay2ValidatorForRequest.validate(request)){
                System.out.println("验证签名失败啦");
                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");
            System.out.println("验证签名成功111啦");
            //验签成功了，确定是自己人了，接下来我们再从微信请求体里获取数据来处理订单
            wxPayService.processOrder1(bodyMap,request);

            //应答超时
            //模拟接收微信端的重复通知
//            TimeUnit.SECONDS.sleep(5);

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);

        } catch (Exception e) {
            e.printStackTrace();
            //失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }

    }


    //新增订单
    @PostMapping("/addOrderData")
    public void addOrderData(String order,String address,String time,String openid,String query_time,String out_trade_no,String type) throws Exception {
        System.out.println("1111111111");
        System.out.println(order);
        System.out.println(address);
        System.out.println(time);
        System.out.println(openid);
        System.out.println(query_time);
        System.out.println(out_trade_no);
        System.out.println("2222222222");
        //因为传递过来的是个数组，所以我们给数组的最外面[]去掉后再转换
//        String jsonObjStr = order.substring(1, order.length() - 1);
        //不转换的话这行报错
        Map parse = (Map) JSON.parse(order);
        String goods_id = parse.get("goods_id")+"";
        String goods_image = parse.get("goods_image")+"";
        String goods_title = parse.get("goods_title")+"";
        String goods_price = parse.get("goods_price")+"";
        String buy_amount = parse.get("buy_amount")+"";
        String specs = parse.get("specs")+"";
        String subtotal = parse.get("subtotal")+"";
        String select = parse.get("select")+"";
        String order_number = parse.get("order_number")+"";

        Xcx_2OrderData xcx_2OrderData = new Xcx_2OrderData();
        xcx_2OrderData.setGoodsId(goods_id);
        xcx_2OrderData.setGoodsImage(goods_image);
        xcx_2OrderData.setGoodsTitle(goods_title);
        xcx_2OrderData.setGoodsPrice(goods_price);
        xcx_2OrderData.setBuyAmount(buy_amount);
        xcx_2OrderData.setSpecs(specs);
        xcx_2OrderData.setSubtotal(subtotal);
        xcx_2OrderData.setSelectOr(select);
        xcx_2OrderData.setOrderNumber(order_number);

        xcx_2OrderData.setAddress(address);

        xcx_2OrderData.setOpenid(openid);
        xcx_2OrderData.setOrderTime(time);
        xcx_2OrderData.setQueryTime(query_time);
        xcx_2OrderData.setAddress(address);
        xcx_2OrderData.setOutTrade(out_trade_no);
        xcx_2OrderData.setPayment(type);

        xcx_2OrderDataService.save(xcx_2OrderData);

    }


    @PostMapping("/deleteCartById")
    public void deleteCartById(String openid,String id) throws Exception {
        System.out.println("删除购物车中的数据");
        QueryWrapper<Xcx_2Cart> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("goods_id",id).eq("openid",openid);
        xcx_2CartService.remove(objectQueryWrapper);

    }

    @GetMapping("/selectOrder")
    public R<Page> selectOrder(String openid,String query,int page, int pageSize){
        System.out.println("订单界面数据展示");
        System.out.println("接收到的openid，"+openid);
        System.out.println("接收到的query，"+query);
        System.out.println("接收到的page，"+page);
        System.out.println("接收到的pageSize，"+pageSize);
        //分页构造器
        Page<Xcx_2OrderData> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        QueryWrapper<Xcx_2OrderData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openid);
        // 当 query 不为空时，拼接上额外的查询条件
        // 使用 Hutool 将 query 字符串解析为 Map
        Map<String, Object> queryMap = JSONUtil.toBean(query, Map.class);

        // 遍历 Map,构造查询条件
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            queryWrapper.eq(key, value);
        }


        //分页查询
        xcx_2OrderDataService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


}
