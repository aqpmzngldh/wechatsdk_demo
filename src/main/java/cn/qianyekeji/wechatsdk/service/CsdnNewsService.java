package cn.qianyekeji.wechatsdk.service;

public interface CsdnNewsService {
    //这种方式用不了几天就被403了
    String csdn() throws Exception;

    //手动维护解析
    String csdn_to() throws Exception;
}
