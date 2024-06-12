package cn.qianyekeji.wechatsdk.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.qianyekeji.wechatsdk.service.CsdnNewsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class CsdnNewsServiceImpl implements CsdnNewsService {
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
        String fileURL = "https://qianyekeji.cn/img2/csdn.txt";
        StringBuilder resultString = new StringBuilder();
        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 使用BufferedReader读取输入流
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    // 逐行读取文件内容
                    while ((line = br.readLine()) != null) {
                        // 将读取的行拼接到结果字符串中，并添加换行符
                        resultString.append(line).append("\n\n");
                    }
                }
            } else {
                System.out.println("GET请求失败，响应代码：" + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString.toString();
    }
}
