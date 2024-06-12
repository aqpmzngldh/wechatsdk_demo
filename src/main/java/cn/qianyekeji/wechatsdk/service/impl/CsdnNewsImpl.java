package cn.qianyekeji.wechatsdk.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.qianyekeji.wechatsdk.service.CsdnNews;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Service
public class CsdnNewsImpl implements CsdnNews {
    @Override
    public String csdn()throws Exception {
        System.out.println("对csdn中的极客日报用户发的it新闻热点进行解析获取");
        try {
            String url = "https://rss.csdn.net/csdngeeknews/rss/map?spm=1001.2014.3001.5494";
            // 发送GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            if (response.isOk()) {
                String responseBody = response.body();
                int startIndex = responseBody.indexOf("<link>", responseBody.indexOf("<link>") + 1) + "<link>".length();
                int endIndex = responseBody.indexOf("</link>", responseBody.indexOf("</link>")+1);
                String firstLink = responseBody.substring(startIndex, endIndex);
                System.out.println("尝试查看提取的链接："+firstLink);

                String articleUrl = firstLink; // 替换为你感兴趣的文章链接
                try {
                    Document doc = Jsoup.connect(articleUrl).get();
                    // 提取新闻热点
                    StringBuilder sb = new StringBuilder();
                    Elements hotNewsElements = doc.select("#content_views > ul > li > p");
                    System.out.println("新闻热点:");
                    for (Element hotNewsElement : hotNewsElements) {
                        sb.append(hotNewsElement.text()).append(System.lineSeparator()); // 使用System.lineSeparator()获取当前系统换行符
                    }
                    System.out.println(sb.toString());
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 处理错误
                System.err.println("Failed to 获取新闻热点csdn" + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  "暂无今日新闻推荐";
    }



    public String csdn_to() throws Exception{
        System.out.println("对csdn中自己设置了自定义域名的文章进行解析获取");
        try {

            String url = "https://rss.csdn.net/weixin_46064585/rss/map?spm=1001.2014.3001.5494";

            // 发送GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            if (response.isOk()) {
                String responseBody = response.body();
                int startIndex = responseBody.indexOf("<link>", responseBody.indexOf("<link>") + 1) + "<link>".length();
                int endIndex = responseBody.indexOf("</link>", responseBody.indexOf("</link>")+1);
                String firstLink = responseBody.substring(startIndex, endIndex);
                String articleUrl = firstLink;
                String articleId = articleUrl.substring(articleUrl.lastIndexOf("/") + 1);
                articleUrl = "https://qianye.blog.csdn.net/article/details/"+articleId;
                try {
                    Document doc = Jsoup.connect(articleUrl).get();
                    // 提取新闻热点
                    StringBuilder sb = new StringBuilder();
                    Elements hotNewsElements = doc.select("#content_views > p");
                    for (Element hotNewsElement : hotNewsElements) {
                        sb.append(hotNewsElement.text()).append(System.lineSeparator());
                    }
                    System.out.println(sb.toString());
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 处理错误
                System.err.println("获取新闻热点csdn" + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  "";
    }
}
