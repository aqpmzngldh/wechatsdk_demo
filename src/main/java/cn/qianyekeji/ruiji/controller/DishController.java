package cn.qianyekeji.ruiji.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.qianyekeji.ruiji.entity.DishFlavor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.dto.DishDto;
import cn.qianyekeji.ruiji.entity.Category;
import cn.qianyekeji.ruiji.entity.Dish;
import cn.qianyekeji.ruiji.service.CategoryService;
import cn.qianyekeji.ruiji.service.DishFlavorService;
import cn.qianyekeji.ruiji.service.DishService;
import com.sun.deploy.net.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        //清理了之后我觉得应该再访问一下这个url，要不然首次加载的时候还是很慢的
        //http://localhost:8089/dish/list?categoryId=1413341197421846529&status=1
        String URL = "http://localhost:8089/dish/list?categoryId="+dishDto.getCategoryId()+"&status=1";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(URL, String.class);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询,查询完了之后pageInfo里面的属性才赋值成功，这时候再进行对象的拷贝
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        //为什么要进行对象的拷贝，也就是属性的拷贝，因为在Dish里面没有category属性，前段菜品名称prop对应的这个属性
        //而DishDto里面有这个属性，所以我们新new了DishDto，再把除了records属性之外的进行属性拷贝
        //为什么要排除records属性，因为我们要对这个属性进行处理的，这个属性就是页面数据，具体可以打断点查看

        //底下这个意思是从pageInfo拷贝属性到dishDtoPage，除了records属性不拷贝，其他的都复制粘贴过去
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
//            第二次拷贝是因为如果不拷贝的话dishdto里面其他属性都是空的，注意他是继承了dish的
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据(这种比清理全部更合理一点)
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
//        清理了之后我觉得应该再访问一下这个url，要不然首次加载的时候还是很慢的
//        http://localhost:8089/dish/list?categoryId=1413341197421846529&status=1
        String URL = "http://localhost:8089/dish/list?categoryId="+dishDto.getCategoryId()+"&status=1";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(URL, String.class);


        return R.success("新增菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    //为什么在前段要注释掉这个，是因为在移动端我们进行数据展示的时候
    // 每个菜品显示的是+号还是选择规格就是看这个菜品有没有口味信息决定的，所以我们要把菜品的口味信息也返回
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询状态为1（起售状态）的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        //动态构造key
//        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844391040167938_1
//        这里不设置死的话在添加套餐功能中的添加菜品中传递的dish.getStatus()是null
        String key = "dish_" + dish.getCategoryId() + "_" + 1;//dish_1397844391040167938_1
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList != null){
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
//        redisTemplate.opsForValue().set(key,dishDtoList,1440, TimeUnit.MINUTES);
//        这里应该给他设置成永久存储我觉得好一些，因为如果设置了过期时间，这样子的话过期了用户再访问，速度又会慢很多
        redisTemplate.opsForValue().set(key,dishDtoList);


        return R.success(dishDtoList);
    }
}
