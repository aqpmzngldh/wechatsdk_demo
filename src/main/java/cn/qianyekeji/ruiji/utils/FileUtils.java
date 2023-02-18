package cn.qianyekeji.ruiji.utils;

/**
 * @author liangshuai
 * @date 2023/2/18
 */
public class FileUtils {
    /**
     * 获取文件名的后缀，如：changlu.jpg => .jpg
     * @return 文件后缀名
     */
    public static String getFileSuffix(String fileName) {
        return fileName.contains(".") ? fileName.substring(fileName.indexOf('.')) : null;
    }
}
