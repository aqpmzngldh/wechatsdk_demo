package cn.qianyekeji.wechatsdk.service;

public interface CsdnNews {
    //这种方式用不了几天就被403了
    String csdn() throws Exception;

    //解决方案：https://qianye.blog.csdn.net/article/details/139062310?spm=1001.2014.3001.5502
    //手动维护解析
    String csdn_to() throws Exception;
}
