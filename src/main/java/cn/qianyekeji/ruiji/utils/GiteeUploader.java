package cn.qianyekeji.ruiji.utils;

import cn.hutool.core.codec.Base64;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author liangshuai
 * @date 2023/2/18
 */
@Component
public class GiteeUploader {

    /**
     * 码云私人令牌
     */
    public static final String ACCESS_TOKEN = "b30efcb8e26a732287c3e77811fd7e80";  //这里不展示我自己的了，需要你自己补充

    /**
     * 码云个人空间名
     */
    public static final String OWNER = "liangdachui";

    /**
     * 上传指定仓库
     */
    public static final String REPO = "tu_tu";


    /**
     * 上传时指定存放图片路径
     */
    public static final String PATH = "/uploadimg/"+ DateUtils.getYearMonth()+"/"; //使用到了日期工具类


    /**
     * 用于提交描述
     */
    public static final String ADD_MESSAGE = "add img";
    public static final String DEL_MESSAGE = "DEL img";

    //API
    /**
     * 新建(POST)、获取(GET)、删除(DELETE)文件：()中指的是使用对应的请求方式
     * %s =>仓库所属空间地址(企业、组织或个人的地址path)  (owner)
     * %s => 仓库路径(repo)
     * %s => 文件的路径(path)
     */
    public static final String API_CREATE_POST = "https://gitee.com/api/v5/repos/%s/%s/contents/%s";


    /**
     * 生成创建(获取、删除)的指定文件路径
     * @param originalFilename
     * @return
     */
    public String createUploadFileUrl(String originalFilename){
        //获取文件后缀
        String suffix = FileUtils.getFileSuffix(originalFilename);//使用到了自己编写的FileUtils工具类
        //拼接存储的图片名称
        String fileName = System.currentTimeMillis()+"_"+ UUID.randomUUID().toString()+suffix;
        //填充请求路径
        String url = String.format(GiteeUploader.API_CREATE_POST,
                GiteeUploader.OWNER,
                GiteeUploader.REPO,
                GiteeUploader.PATH+fileName);
        return url;
    }

    /**
     * 获取创建文件的请求体map集合：access_token、message、content
     * @param multipartFile 文件字节数组
     * @return 封装成map的请求体集合
     */
    public Map<String,Object> getUploadBodyMap(byte[] multipartFile){
        HashMap<String, Object> bodyMap = new HashMap<>(3);
        bodyMap.put("access_token",GiteeUploader.ACCESS_TOKEN);
        bodyMap.put("message", GiteeUploader.ADD_MESSAGE);
        bodyMap.put("content", Base64.encode(multipartFile));
        return bodyMap;
    }

}
