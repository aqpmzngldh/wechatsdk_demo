package cn.qianyekeji.ruiji.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 * @param <T>
 */
@Data
/**
 *     类上面有R<T>，其中<T>表示出是一个泛型类，泛型类好处方便创建不同的对象,我们一般用在方法的返回值上，因为方法的返回值是某个类时候，需要的是
 *     该类的对象，比如 public static <T> R<T> success(T object) {中的R<T>，拿员工表举例子就是R<Employee>,也就是说当方法的返回值是R<Employee>
 *     的时候，再  R<T> r = new R<T>();  的对象r就是R<Employee>的对象
 *
 *     public static <T> R<T> success(T object) {其中第一个<T>和(T object)表示是一个泛型方法，泛型方法好处是在调用的时候方法形参里面可以扔int
 *     也可以扔对象，也可以扔string，兼容更高
 */
public class R<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据


    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
