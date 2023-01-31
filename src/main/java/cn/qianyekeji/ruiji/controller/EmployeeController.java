package cn.qianyekeji.ruiji.controller;

import cn.qianyekeji.ruiji.common.R;
import cn.qianyekeji.ruiji.entity.Employee;
import cn.qianyekeji.ruiji.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author liangshuai
 * @date 2023/1/21
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
//        从employee中获取用户名和密码，根据用户名传递到sql中进行查询
//        如果查不到返回失败，查到的话返回的实体对象中获取密码，再进行密码的比对
//        密码比对失败返回失败，比对成功的话再根据实体里面用户禁用的字段再做个判断
//        禁用的用户也返回登录失败，否则登录成功，然后将用户的数据存储在session中


        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }
        //因为数据库里面存储的密码是md5加密后的，所以在密码比较之前先将提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //查到了，根据返回的实体中取出密码和表单提交的密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //到这里则密码比较成功，但是这时候还要对账号禁用的员工进行登录的限制
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //登录成功，将员工id存入Session并返回登录成功结果
        //这个存储了后会在过滤器里面用，看session中是否employee为空然后判断用户是否登录
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /**
     * 加密的时候因为是md5直接加密的密码，对于简单的加密MD5也是能反编译出来原来的密码的
     * 这时候可以表里面多设计一个字段，在新增用户的时候加进去随机数，然后默认的密码用12345+默认字段的随机数进行md5加密
     *稍微安全点这样做法
     *
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
//        想打印对象信息，employee.toString()
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*
        底下的内容直接在mybatisplus中的公共字段填充里面设置，所以注释掉了
        公共字段填充是为了解决，当很多个表都有相同字段要进行相同操作的时候，为了不让代码繁琐，抽取公共部分
        公共字段填充有两个步骤：
        1，在实体类上加对应的注解，对哪些字段进行公共字段填充
        2，写抽取的公共填充的那个类，记得实现metaObjecthandler接口（这个项目里面对应的是MyMetaObjecthandler）
        */
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //获得当前登录用户的id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        /*
        底下的内容直接在mybatisplus中的公共字段填充里面设置，所以注释掉了
        公共字段填充是为了解决，当很多个表都有相同字段要进行相同操作的时候，为了不让代码繁琐，抽取公共部分
        公共字段填充有两个步骤：
        1，在实体类上加对应的注解，对哪些字段进行公共字段填充
        2，写抽取的公共填充的那个类，记得实现metaObjecthandler接口（这个项目里面对应的是MyMetaObjecthandler）
        */

//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

}
