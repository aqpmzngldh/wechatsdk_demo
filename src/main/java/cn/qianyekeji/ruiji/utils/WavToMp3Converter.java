package cn.qianyekeji.ruiji.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


public class WavToMp3Converter {

    public static void convertWavToMp3(String wavFilePath, String mp3FilePath) {
        try {
            // 读取 WAV 文件
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(wavFilePath));

            // 定义 MP3 编码参数
            AudioFormat sourceFormat = audioInputStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false);

            // 将音频流转换为目标格式
            AudioInputStream convertedAudioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);

            // 写入 MP3 文件
            AudioSystem.write(convertedAudioInputStream, AudioFileFormat.Type.WAVE, new File(mp3FilePath));

            // 关闭输入流
            convertedAudioInputStream.close();
            audioInputStream.close();

            System.out.println("转换完成！");
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 测试转换
        String wavFilePath = "C:\\Users\\qianye\\Desktop\\2222\\ce\\audio_1714785935768.wav";
        String mp3FilePath = "C:\\Users\\qianye\\Desktop\\2222\\ce\\audio_1714785935768.mp3";
        convertWavToMp3(wavFilePath, mp3FilePath);
    }
}
