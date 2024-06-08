package cn.qianyekeji.ruiji.utils;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class BaiduMapGeocoding {
    private static final String API_URL = "http://api.map.baidu.com/geocoding/v3/";
    private static final String AK = "iNrukKkdcDcb2gmStdpyKAnn1Ivpsy9A"; // 你的AK

    /**
     * 将地址转换为经纬度坐标
     *
     * @param address 地址，如"杭州市江干区江干街道"
     * @return 包含经度和纬度的数组，格式为 [经度, 纬度]，失败时返回null
     */
    public static double[] addressToCoordinate(String address) {
        String encodedAddress = URLUtil.encode(address);
        String requestUrl = API_URL + "?address=" + encodedAddress + "&output=json&ak=" + AK;

        String response = HttpUtil.get(requestUrl);
        JSONObject json = JSONUtil.parseObj(response);

        if (json.getInt("status") == 0) {
            JSONObject location = json.getJSONObject("result").getJSONObject("location");
            double lng = location.getDouble("lng");
            double lat = location.getDouble("lat");
            return new double[]{lng, lat};
        } else {
            System.out.println("地理编码失败：" + json.getStr("message"));
            return null;
        }
    }

    // 测试方法
    public static void main(String[] args) {
        String address = "杭州市江干区江干街道";
        double[] coordinate = addressToCoordinate(address);

        if (coordinate != null) {
            System.out.println("地址 '" + address + "' 的经纬度坐标：");
            System.out.println("经度：" + coordinate[0]);
            System.out.println("纬度：" + coordinate[1]);
        } else {
            System.out.println("无法获取地址 '" + address + "' 的经纬度坐标。");
        }
    }
}