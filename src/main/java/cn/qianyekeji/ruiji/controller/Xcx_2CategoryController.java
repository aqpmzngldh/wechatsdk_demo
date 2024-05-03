package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.*;
import cn.qianyekeji.ruiji.service.*;
import cn.qianyekeji.ruiji.utils.AudioUtils;
import cn.qianyekeji.ruiji.utils.AudioWavUtils;
import cn.qianyekeji.ruiji.utils.HttpUtils;
import cn.qianyekeji.ruiji.utils.WechatPay2ValidatorForRequest;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/xcx_2")
@Slf4j
public class Xcx_2CategoryController {
    public static final QueryWrapper<Object> OBJECT_QUERY_WRAPPER = new QueryWrapper<>();
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
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private Xcx_2XianService xcx_2XianService;

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

//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile file) {
////        String sss="F:\\www\\server\\img2\\";
//        // 获取上传的文件
//        if (file.isEmpty()) {
//            System.out.println("上传的文件为空");
//        }else{
//            System.out.println("上传的文件不不不为空");
//        }
//        String originalFilename = file.getOriginalFilename();
//        log.info("传递过来的文件名为，{}",originalFilename);
//
//        // 创建目标文件对象
////        File dest = new File(sss + originalFilename);
//        File dest = new File(basePath + originalFilename);
//        if (!dest.exists()) {
//            dest.mkdirs();
//        }
//        try {
//            // 将上传的文件保存到服务器
//            file.transferTo(dest);
////            System.out.println("文件上传成功，保存路径为：" + sss + originalFilename);
//            System.out.println("文件上传成功，保存路径为：" + basePath + originalFilename);
//        } catch (IOException e) {
//            System.out.println("文件上传失败: " + e.getMessage());
//        }
//        return "https://qianyekeji.cn/img2/"+originalFilename;
//    }

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
//        objectQueryWrapper.eq("id",id).isNull("eav_image");
        objectQueryWrapper.eq("id",id).isNull("biao");
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
//        objectQueryWrapper.eq("id",xcx_2VideoComment.getId()).isNull("eav_image");
        objectQueryWrapper.eq("id",xcx_2VideoComment.getId()).isNull("biao");
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
//        objectQueryWrapper.eq("id",id).isNotNull("eav_image");
        objectQueryWrapper.eq("id",id).isNotNull("biao");
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
//        queryWrapper.eq("id",id).isNotNull("eav_image");
        queryWrapper.eq("id",id).isNotNull("biao");

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
    public R<String> jsapiPay(@PathVariable String getNonceStr,@PathVariable String timestamp,@PathVariable Long productId,HttpServletRequest request,String xcxOrgongzhonghao,String openidOr,String name2,String out_trade_no,String id) throws Exception {
        log.info("发起支付请求 v3");

        // 使用 openidOr 作为 Redis Hash 的 key
        String hashKey = openidOr;
        // 从Redis中获取预支付单号
        // 从 Redis Hash 中获取预支付订单号
        String prepayId = (String) redisTemplate.opsForHash().get(hashKey, out_trade_no);
        System.out.println(prepayId);
        if (prepayId != null) {
            // 存在预支付订单号,直接返回
            log.info("从 Redis 获取预支付订单号: {}", prepayId);
            System.out.println("预支付订单号,"+prepayId);
            // 分别获取 bodyAsString 的起始位置
            int bodyAsStringIndex = prepayId.indexOf("bodyAsString=") + "bodyAsString=".length();
            int timestampIndex = prepayId.indexOf("timestamp=") + "timestamp=".length();
            int getNonceStrIndex = prepayId.indexOf("getNonceStr=") + "getNonceStr=".length();
            // 分别获取 paySign 的起始位置
            int paySignIndex = prepayId.indexOf("paySign=") + "paySign=".length();
            // 从 prepayId 字符串中提取 bodyAsString
            String bodyAsString = prepayId.substring(bodyAsStringIndex, prepayId.indexOf(",", bodyAsStringIndex));
            String timestamp1 = prepayId.substring(timestampIndex, prepayId.indexOf("}", timestampIndex));
            String getNonceStr1 = prepayId.substring(getNonceStrIndex, prepayId.indexOf(",", getNonceStrIndex));
            // 从 prepayId 字符串中提取 paySign
            String paySign = prepayId.substring(paySignIndex, prepayId.indexOf(",", paySignIndex));
            // 打印 bodyAsString
            System.out.println("bodyAsString: " + bodyAsString);
            // 打印 paySign
            System.out.println("paySign: " + paySign);
            return R.success("").add("bodyAsString",bodyAsString)
                    .add("timestamp",timestamp1).add("getNonceStr",getNonceStr1).add("paySign",paySign);
        }
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

    @PostMapping("/tuikuan")
    public R<String> refunds(Long id) throws Exception {

        log.info("申请退款");
        System.out.println(id);
        String refund = wxPayService.refund(id);
        return R.success(refund);
    }

    /**
     * 查询退款
     * @param
     * @return
     * @throws Exception
     */

    @GetMapping("/queryRefund")
    public R queryRefund(String id) throws Exception {

        log.info("查询退款");
        QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id);
        String outRefund = xcx_2OrderDataService.getOne(objectQueryWrapper).getOutRefund();

        String result = wxPayService.queryRefund(outRefund);
        return R.success(result);
    }

    /**
     * 退款结果通知
     * 退款状态改变后，微信会把相关退款结果发送给商户。
     */

    @PostMapping("/refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response){

        log.info("退款通知执行");
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();//应答对象

        try {
            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String)bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                    = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if(!wechatPay2ValidatorForRequest.validate(request)){

                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");

            //处理退款单
            wxPayService.processRefund(bodyMap);

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

        xcx_2OrderData.setOutRefund(getNo());

        boolean save = xcx_2OrderDataService.save(xcx_2OrderData);
        System.out.println("新增订单的结果是,"+save);

    }

    /**
     * 获取随机退款单编号
     * @return
     */
    public  String getNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = sdf.format(new Date());
        String result = "";
        Random random = new Random();
        for (int i = 0; i <9; i++) {
            result += random.nextInt(10);
        }
        return newDate + result;
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
        //TODO 现在的情况是：用户购物车下单后，如果用户没有支付，比如用户下单了两个商品，这时候我会把用户的预支付标识写到redis数据库，以防止用户下次继续支付
        //TODO 但是随之而来出现一个问题，单个商品是没什么问题的，如果是购物车多个商品的话，用户第一次没有支付，第二次继续支付的话，会先去redis取预支付标识，
        //TODO 取出来之后呢，如果用户点击订单界面中两个未支付的商品中的某个商品，他们都是购物车的商品的话，这时候支付金额是全部的金额，而不是单个金额，
        //TODO 为什么我要把用户的预支付标识存进redis，因为继续支付的话需要的就是原来的预支付标识，而预支付标识我们无法改变，是微信给出来的
        //TODO 现在暂时的解决方案是，判断是不是购物车来的订单数据，是的话，如果用户在下单购物车商品时候取消，直接更改订单状态为已取消
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("pay_success","not_pay").eq("payment","cart").set("pay_success","can_order");
        xcx_2OrderDataService.update(objectUpdateWrapper);

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

        // 根据数据库的order_time字段进行倒序排列
        queryWrapper.orderByDesc("order_time");

        // TODO 这里应该再加一个条件，因为用户如果进入支付输入密码界面的时候，这时候用户手机关机了
        // TODO 然后既不能微信通知中让我们改变其状态为未支付，也不能在前端取消支付时候也不会触发（也不能让我们改成未支付），这时候数据库
        // TODO 中的pay_success字段就是为null，而不是为待支付: not_pay字段，这会出现一个什么情况呢
        // TODO 就是说页面这种查全部可以查到，但是不属于其他四个任何一个分类，所以拼接pay_success字段不为null的值
        // TODO bububu，这里不拼接pay_success字段不为null的值了，直接给pay_success字段为null的值全部删掉即可

        //TODO 这块应该定时项目更新的时候删除，因为有可能用户下单了，但是微信通知还没有给订单状态改过来呢，结果就直接删掉了，，，
        //TODO 或者不删也行，反正也查不出来
//        QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
//        objectQueryWrapper.isNull("pay_success");
//        xcx_2OrderDataService.remove(objectQueryWrapper);

        //分页查询
        xcx_2OrderDataService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    //商家端订单管理选项，商家端不用拼接openid。所以重新写个吧
    @GetMapping("/selectOrder1")
    public R<Page> selectOrder1(String query,int page, int pageSize){
        System.out.println("商户端订单界面数据展示");
        System.out.println("接收到的query，"+query);
        System.out.println("接收到的page，"+page);
        System.out.println("接收到的pageSize，"+pageSize);
        //分页构造器
        Page<Xcx_2OrderData> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        QueryWrapper<Xcx_2OrderData> queryWrapper = new QueryWrapper<>();

        // 当 query 不为空时，拼接上额外的查询条件
        // 使用 Hutool 将 query 字符串解析为 Map
        Map<String, Object> queryMap = JSONUtil.toBean(query, Map.class);
        if (queryMap.get("biao")!=null){
            System.out.println(queryMap.get("biao"));
            System.out.println("这个用来判断是不是退款的两种状态之一");
            queryWrapper.eq("pay_success","success").eq("deliver","ref_pro").or().eq("deliver","ref_succ");
            queryWrapper.orderByDesc("order_time");
            //分页查询
            xcx_2OrderDataService.page(pageInfo,queryWrapper);
            return R.success(pageInfo);
        }

        // 遍历 Map,构造查询条件
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            queryWrapper.eq(key, value);
        }

        // 根据数据库的order_time字段进行倒序排列
        queryWrapper.orderByDesc("order_time");


        QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.isNull("pay_success");
        xcx_2OrderDataService.remove(objectQueryWrapper);

        //分页查询
        xcx_2OrderDataService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    //修改订单为未支付状态
    @PostMapping("/updateOrder")
    public void updateOrder(String out_trade_no,String openid) throws Exception {
        System.out.println("修改订单为未支付状态");
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("out_trade",out_trade_no).eq("openid",openid)
                .set("pay_success","not_pay");

        // TODO 修改订单为未支付的同时，把订单加入到redis，为什么要加入redis，如果用户调起支付界面但是没有支付，30分钟之内还没有支付，我们就修改
        //订单状态为已取消订单can_order，为什么要有个时间限制，因为预支付标识的时间限制是2小时，我们给预支付交易标识存进redis缓存了90分钟
        //所以肯定要小于90分钟，就设置30分钟好了，用redis中的zset集合，因为zset中有个score，可以根据他判断时间有咩有超过30分钟

        // 将 uuid 和时间戳信息存储到 Redis 中
        long timestamp = Instant.now().toEpochMilli(); // 获取当前时间的时间戳
        redisTemplate.opsForZSet().add("xcx_2quxiaoOr", openid + "_" + out_trade_no, timestamp);

        xcx_2OrderDataService.update(objectUpdateWrapper);
    }

    // 每隔3分钟执行一次的定时任务，误差在3分钟，不过关系不大
    @Scheduled(fixedDelay = 180000)
    public void removeExpiredElements() {
        // 获取当前时间戳
        long currentTime = Instant.now().toEpochMilli();

        // 获取 Redis 中所有的元素
        Set<ZSetOperations.TypedTuple<String>> elements = redisTemplate.opsForZSet().rangeWithScores("xcx_2quxiaoOr", 0, -1);

        // 遍历所有元素
        for (ZSetOperations.TypedTuple<String> element : elements) {
            String uuid = (String) element.getValue();
            long timestamp = element.getScore().longValue();

            // 如果当前时间与时间戳的差值大于30分钟，则删除该元素
            if ((currentTime - timestamp) > 1800000) {
                redisTemplate.opsForZSet().remove("xcx_2quxiaoOr", uuid);

                String[] parts = uuid.split("_");
                // 第一个部分。openid
                String part1 = parts[0];
                // 第二个部分商户订单号out_trade_no
                String part2 = parts[1];
                UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
                objectUpdateWrapper.eq("out_trade",part2).eq("openid",part1)
                        .set("pay_success","can_order");
                xcx_2OrderDataService.update(objectUpdateWrapper);

            }
        }
    }

    //修改订单为未支付状态
    @PostMapping("/cancelOrder")
    public void cancelOrder(String id) throws Exception {
        System.out.println("修改订单为取消状态");
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id",id).set("pay_success","can_order");

        xcx_2OrderDataService.update(objectUpdateWrapper);
    }

    //用户端退款
    @PostMapping("/tuiKuan")
    public void tuiKuan(String id,String ReReason) throws Exception {
        System.out.println("用户端退款");
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id",id).set("deliver","ref_pro").set("Re_reason",ReReason);

        xcx_2OrderDataService.update(objectUpdateWrapper);
    }

    //用户端收货
    @PostMapping("/shouHuo")
    public void shouHuo(String id) throws Exception {
        System.out.println("用户端收获");
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id",id).set("deliver","rece_goods");

        xcx_2OrderDataService.update(objectUpdateWrapper);
    }

    //修改订单评价字段为已评价
    @PostMapping("/updateOrderPingJia")
    public void updateOrderPingJia(String id) throws Exception {
        System.out.println("用户已评价");
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id",id).set("evaluate","true");

        xcx_2OrderDataService.update(objectUpdateWrapper);
    }

    @PostMapping("/getCartByOpenid")
    public R<List<Xcx_2Cart>> getCartByOpenid(String openid) throws Exception {
        System.out.println("查询购物车中的数据");
        QueryWrapper<Xcx_2Cart> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2Cart> list = xcx_2CartService.list(objectQueryWrapper);
        return R.success(list);
    }

    @PostMapping("/addDeleteGoodNumberCartByOpenid")
    public void addDeleteGoodNumberCartByOpenid(Long id,String buyAmount,String subtotal) throws Exception {
        System.out.println("添加或者减少某个人的购物车中的某个商品的数量和价格");
            UpdateWrapper<Xcx_2Cart> objectUpdateWrapper = new UpdateWrapper<>();
            objectUpdateWrapper.eq("id",id)
                    .set("buy_amount",buyAmount).set("subtotal",subtotal);
        xcx_2CartService.update(objectUpdateWrapper);

    }

    @PostMapping("/deleteCart")
    public void deleteCart(Long id) throws Exception {
        System.out.println("删除购物车中的某个商品");
        QueryWrapper<Xcx_2Cart> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id",id);
        xcx_2CartService.remove(objectQueryWrapper);
    }

    @GetMapping("/getOpenidCollect")
    public R<List<Xcx_2Goods>> getOpenidCollect(String openid){
        System.out.println("查看用户收藏");
        QueryWrapper<Xcx_2UserInfo> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("openid",openid);
        List<Xcx_2UserInfo> list = xcx_2UserInfoService.list(objectQueryWrapper);
        // 2. 创建一个列表存储商品信息
        List<Xcx_2Goods> resultList =new ArrayList<>();
        // 遍历 userInfoList，根据每个 userInfo 的 id 查询商品表
        for (Xcx_2UserInfo userInfo : list) {
            Integer id = userInfo.getId();// 获取 id
            // 根据 id 查询商品表，获取商品信息
            Xcx_2Goods byId = xcx_2GoodsService.getById(id);

            // 将商品信息添加到结果列表中
            resultList.add(byId);
        }
        return R.success(resultList);
    }



    @PostMapping("/zongShouyi")
    public R<BigDecimal> zongShouyi(String str,String time) throws Exception {
        System.out.println("商户端四个数据展示查询");
        BigDecimal bigDecimal = BigDecimal.ZERO; // 初始化 bigDecimal 为 0
       if("zong".equals(str)){
           System.out.println("计算累计收益");
           QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
           objectQueryWrapper.eq("pay_success","success").ne("deliver", "ref_pro").ne("deliver", "ref_succ");
           List<Xcx_2OrderData> list = xcx_2OrderDataService.list(objectQueryWrapper);
           for (int i = 0; i < list.size(); i++) {
               String subtotal = list.get(i).getSubtotal();
               // 将 subtotal 转换成 BigDecimal 并累加到 bigDecimal 中
               bigDecimal =bigDecimal.add(new BigDecimal(subtotal));
           }
           System.out.println("累计收益为，"+bigDecimal);
           return R.success(bigDecimal);
       }else if ("jinri".equals(str)){
           System.out.println("计算今日收益");
           QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
           objectQueryWrapper.eq("pay_success","success").eq("query_time",time).ne("deliver", "ref_pro").ne("deliver", "ref_succ");
           List<Xcx_2OrderData> list = xcx_2OrderDataService.list(objectQueryWrapper);
           for (int i = 0; i < list.size(); i++) {
               String subtotal = list.get(i).getSubtotal();
               // 将 subtotal 转换成 BigDecimal 并累加到 bigDecimal 中
               bigDecimal =bigDecimal.add(new BigDecimal(subtotal));
           }
           System.out.println("今日收益为，"+bigDecimal);
           return R.success(bigDecimal);
       }else if("jinridingdan".equals(str)){
           System.out.println("计算今日订单数量");
           QueryWrapper<Xcx_2OrderData> objectQueryWrapper = new QueryWrapper<>();
           objectQueryWrapper.eq("query_time",time);
           List<Xcx_2OrderData> list = xcx_2OrderDataService.list(objectQueryWrapper);

           System.out.println("今日订单数量，"+list.size());
           BigDecimal bigDecimal1 = new BigDecimal(list.size());
           return R.success(bigDecimal1);
       }else if ("leijidingdan".equals(str)){
           System.out.println("计算累计订单数量");
           int count = xcx_2OrderDataService.count();

           System.out.println("累计订单数量，"+count);
           BigDecimal bigDecimal11 = new BigDecimal(count);
           return R.success(bigDecimal11);
       }
      return null;
    }


    @PostMapping("/fahuo")
    public void fahuo(String id,String waybill_No) throws Exception {
        System.out.println("商户端填写运单号发货");
        System.out.println(id);
        System.out.println(waybill_No);
        UpdateWrapper<Xcx_2OrderData> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id",id).set("deliver","already").set("waybill_no",waybill_No);
        xcx_2OrderDataService.update(objectUpdateWrapper);
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        // 获取上传的文件
        if (file.isEmpty()) {
            System.out.println("上传的文件为空");
            throw new RuntimeException("上传的文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        log.info("传递过来的文件名为，{}",originalFilename);
        // 获取文件扩展名
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        try {
            // 判断文件类型
            if (isImageFile(fileExtension)) {
                System.out.println("使用七牛云存储");
                // 如果是图片文件,使用七牛云存储
                return uploadToQiniu(file, originalFilename);
            } else {
                System.out.println("用自己服务器存储");
                // 如果不是图片文件,使用本地服务器存储
                return uploadToServer(file, originalFilename);
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

    }

    private boolean isImageFile(String fileExtension) {
        String[] imageExtensions = {"png", "jpg", "jpeg", "gif", "bmp", "tiff", "webp", "svg", "tif", "raw", "psd"};
        return Arrays.asList(imageExtensions).contains(fileExtension.toLowerCase());
    }

    private String uploadToServer(MultipartFile file, String fileName) {
        // 服务器本地存储的逻辑
        File dest = new File(basePath + fileName);
        if (dest.exists()) {
            log.warn("文件 {} 已存在，不重复上传", fileName);
            return "https://qianyekeji.cn/img2/" + fileName;
        }

        try {
            dest.mkdirs();
            file.transferTo(dest);
            log.info("文件上传成功，保存路径为：{}", dest.getAbsolutePath());
            return "https://qianyekeji.cn/img2/" + fileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private String uploadToQiniu(MultipartFile file, String fileName) {
        Auth auth = Auth.create("lQAMZBIr7OW9QQ9dyrTUN1p5jsNfgRz-74VfCeCv", "pOnpbdKhTFfyRfksGMMg-LoJBqS3FbYbCzoTnzZg");
        String upToken = auth.uploadToken("qianyekeji");

        Configuration cfg = new Configuration(Zone.zone0());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            byte[] bytes = file.getBytes();
            uploadManager.put(bytes, fileName, upToken);
            return "http://im1g.qianyekeji.cn/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败: " + e.getMessage();
        }
    }

    @PostMapping("/addXianyu")
    public void addXianyu(Xcx_2Xian xcx_2Xian) {
        System.out.println("咸鱼之王用户提交的充值信息");
        System.out.println(xcx_2Xian);
        xcx_2XianService.save(xcx_2Xian);

    }

    @PostMapping("/a")
    public R<String> a() {
        System.out.println("测试获取图片");
        String url = "https://v2.api-m.com/api/heisi";
        // 发送GET请求
        HttpResponse response1 = HttpUtil.createGet(url).execute();
        if (response1.isOk()) {
            String responseBody = response1.body();
            Map<String, Object> map = JSONUtil.parseObj(responseBody);
            String result = (String)map.get("data");
            return R.success(result);
        } else {
            // 处理错误
            return R.error("请求图片出错");
        }
    }

    @PostMapping("/b")
    public R<String> b() {
        System.out.println("测试获取图片");
        String url = "https://api.qqsuu.cn/api/dm-littlesister?type=json";
        // 发送GET请求
        HttpResponse response1 = HttpUtil.createGet(url).execute();
        if (response1.isOk()) {
            String responseBody = response1.body();
            Map<String, Object> map = JSONUtil.parseObj(responseBody);
            String result = (String)map.get("img");
            return R.success(result);
        } else {
            // 处理错误
            return R.error("请求图片出错");
        }
    }



    @PostMapping("/c")
    public R<String> c() {
        System.out.println("测试获取视频");
        String url = "https://api.qqsuu.cn/api/dm-xjj?type=json&apiKey=b4bd29e2d83ea412fa368e2747c8ef41";
        // 发送GET请求
        HttpResponse response1 = HttpUtil.createGet(url).execute();
        if (response1.isOk()) {
            String responseBody = response1.body();
            Map<String, Object> map = JSONUtil.parseObj(responseBody);
            String result = (String)map.get("video");
            return R.success(result);
        } else {
            // 处理错误
            return R.error("请求图片出错");
        }
    }

    @PostMapping("/d")
    public R<Boolean> d(String id) {
        System.out.println("用户发送消息的时候去redis判断当前群聊的机器人是开启还是关闭状态");
        System.out.println("如果没获取到或者获取到的是on，则放行返回true，否则返回false");
        System.out.println("当前群聊的id是"+id);
        String prepayId = (String) redisTemplate.opsForHash().get("a_jiqiren", id);
        System.out.println("获取到prepayId值是"+prepayId);
        if ((prepayId == null || prepayId.isEmpty())||("on").equals(prepayId)) {
            System.out.println("无法获取到prepayId"+prepayId);
            return R.success(true);
        }else{
            return R.success(false);
        }
    }

    @PostMapping("/e")
    public void e(String id) {
        System.out.println("设置机器人状态为关闭");
        redisTemplate.opsForHash().put("a_jiqiren", id, "off");
    }

    @PostMapping("/f")
    public void f(String id) {
        System.out.println("设置机器人状态为开启");
        redisTemplate.opsForHash().put("a_jiqiren", id, "on");
    }

    @PostMapping("/g")
    public void g(String topic,String userNice,String command) {
        System.out.println("设置gpt回复每个人时候的前缀");
        redisTemplate.opsForHash().put(topic, userNice, command);
    }
    @PostMapping("/h")
    public R<String> h(String topic,String userNice) {
        System.out.println("获取gpt回复每个人时候的前缀");
        String value = (String) redisTemplate.opsForHash().get(topic, userNice);
        if (value==null){
            return R.success("");
        }
        return R.success(value+"，");
    }


    @PostMapping("/i")
    public R<Boolean> i(String id) {
        System.out.println("判断当前群是否有操作咸王的权利");
        System.out.println("当前群聊的房间名是"+id);
        String prepayId = (String) redisTemplate.opsForHash().get("a_quanli", id);
        System.out.println("获取到prepayId值是"+prepayId);
        if (prepayId == null || prepayId.isEmpty()) {
            System.out.println("无法获取到prepayId，说明没有权利操作咸王");
            return R.success(false);
        }else{
            return R.success(true);
        }
    }

    @PostMapping("/j")
    public R<String> j(String url) throws Exception{
        String localPath = "/www/server/yuyin/audio_" + System.currentTimeMillis() + ".wav";
        AudioWavUtils.downloadFile(url, localPath);
        // 获取本地文件的信息
        Long wavInfo = AudioWavUtils.getWavInfo(localPath);
        System.out.println("这个wav的秒数是："+wavInfo);
        return R.success(wavInfo+"");
    }

    @PostMapping("/k")
    public R<String> k(String url) throws Exception{
        String address="audio_" + System.currentTimeMillis() + ".wav";
        String localPath = "/www/server/yuyin/" + address;
        AudioWavUtils.downloadFile(url, localPath);
        // 获取本地文件的信息
        Long wavInfo = AudioWavUtils.getWavInfo(localPath);
        System.out.println("这个wav的秒数是："+wavInfo);
        String s = AudioUtils.transferAudioSilk("/www/server/yuyin/", address, false);


        return R.success("").add("second",wavInfo).add("sil",s);
    }
}
