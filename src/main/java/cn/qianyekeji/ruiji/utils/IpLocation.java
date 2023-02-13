package cn.qianyekeji.ruiji.utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class IpLocation {
    public static void main(String[] args) {
        String ip = getIpAddress();
        System.out.println("IP address: " + ip);
    }

    // 获取本机 IP 地址
    public static String getIpAddress() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            URLConnection conn = url.openConnection();
            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ipAddress = in.readLine();
            in.close();
            return ipAddress;
        } catch (IOException e) {
            return "Unknown";
        }
    }

    // 解析 IP 地址的详细地址信息
    private static IpInfo parseIpInfo(String json) {
        Gson gson = new Gson();
        IpInfo ipInfo = gson.fromJson(json, IpInfo.class);
        return ipInfo;
    }

    // 定义 IP 地址的详细地址信息类
    private static class IpInfo {
        private String ip;
        private String country;
        private String province;
        private String city;
        private String county;
        // 其他字段...

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        // 其他 getter 和 setter 方法...
    }
}

