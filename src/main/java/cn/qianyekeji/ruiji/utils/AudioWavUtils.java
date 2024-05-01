package cn.qianyekeji.ruiji.utils;
import org.jaudiotagger.audio.wav.util.WavInfoReader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/** @Author huyi @Date 2021/9/30 14:46 @Description: 音频工具类 */
public class AudioWavUtils {

    public static Long getWavInfo(String filePath) throws Exception {
        File file = new File(filePath);
        WavInfoReader wavInfoReader = new WavInfoReader();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long duration = (long) (wavInfoReader.read(raf).getPreciseLength() * 1000);
        int sampleRate = toInt(read(raf, 24, 4));
        System.out.println("duration -> " + duration + ",sampleRate -> " + sampleRate);
        raf.close();
        return duration;
    }

    public static int toInt(byte[] b) {
        return ((b[3] << 24) + (b[2] << 16) + (b[1] << 8) + (b[0]));
    }

    public static byte[] read(RandomAccessFile rdf, int pos, int length) throws IOException {
        rdf.seek(pos);
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = rdf.readByte();
        }
        return result;
    }
    public static void downloadFile(String remoteUrl, String localPath) throws IOException {
        URL url = new URL(remoteUrl);
        Files.copy(url.openStream(), Paths.get(localPath));
    }

    public static void main(String[] args) throws Exception {
        String localPath = "audio_" + System.currentTimeMillis() + ".wav";
        downloadFile("https://api.lolimi.cn/API/yyhc/lyy/7770.wav", localPath);
        // 获取本地文件的信息
        Long wavInfo = getWavInfo(localPath);
        System.out.println("这个wav的秒数是："+wavInfo);

    }
}
