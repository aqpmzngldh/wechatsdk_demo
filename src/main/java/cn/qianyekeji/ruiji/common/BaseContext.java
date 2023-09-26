package cn.qianyekeji.ruiji.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> openid = new ThreadLocal<>();

    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }


    /**
     * 设置值
     * @param id
     */
    public static void setOpenid(String id){
        openid.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static String getOpenid(){
        return openid.get();
    }
}