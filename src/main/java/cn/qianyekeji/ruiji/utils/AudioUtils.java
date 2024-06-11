package cn.qianyekeji.ruiji.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 音频格式转换
 *
 * @author sqd233
 */
public class AudioUtils {

    /**
     * 工具地址
     **/

//    static String path = "F:\\project\\ruiji\\src\\main\\java\\cn\\qianyekeji\\ruiji\\silk\\";
    static String path = "src/main/java/cn/qianyekeji/ruiji/silk/";


    public static void main(String[] args) {
        // TODO: mp3 转 silk
//        transferAudioSilk("C:\\Users\\qianye\\Desktop\\2222\\ce\\", "audio_1714785935768.mp3", false);
        // TODO: wav 转 silk
        transferAudioSilk("C:\\Users\\qianye\\Desktop\\2222\\", "9212.wav", false);
        // TODO: mp3转amr
//        transferMp3Amr("路径\\文件名.mp3", "路径\\文件名.amr");
    }

    /**
     * MP3/WAV转SILk格式
     *
     * @param filePath 例：D:\\file\\audio.mp3
     * @param isSource isSource 是否清空原文件
     * @return
     */
    public static String transferAudioSilk(String filePath, boolean isSource) {
        Integer index = filePath.lastIndexOf("\\") + 1;
        return transferAudioSilk(filePath.substring(0, index), filePath.substring(index, filePath.length()), isSource);
    }

    /**
     * MP3/WAV转SILk格式
     *
     * @param path     文件路径 例：D:\\file\\
     * @param name     文件名称 例：audio.mp3/audio.wav
     * @param isSource 是否清空原文件
     * @return silk文件路径
     * @throws Exception
     */
    public static String transferAudioSilk(String path, String name, boolean isSource) {
        try {
            // 判断后缀格式
            String suffix = name.split("\\.")[1];
            if (!suffix.toLowerCase().equals("mp3") && !suffix.toLowerCase().equals("wav")) {
                System.out.println("文件格式必须是mp3/wav");
            }
            String filePath = path + name;
            System.out.println("文件全名是" + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception("文件不存在！");
            }
            // 文件名时拼接
            SimpleDateFormat ttime = new SimpleDateFormat("yyyyMMddhhMMSS");
            String time = ttime.format(new Date());
            // 导出的pcm格式路径
            String pcmPath = path + "PCM_" + time + ".pcm";
            // 先将mp3/wav转换成pcm格式
            transferAudioPcm(filePath, pcmPath);
            // 导出的silk格式路径
//            String silkPath = path + "SILK_" + time + ".silk";
//            String silkPath = path + "SILK_" + time + ".sil";
            String fileName = name.substring(0, name.lastIndexOf("."));
            String silkPath = path + fileName + ".silk";
            System.out.println("文件名：" + silkPath);
            // 转换成silk格式
            transferPcmSilk(pcmPath, silkPath);
            // 删除pcm文件
            File pcmFile = new File(pcmPath);
            if (pcmFile.exists()) {
                pcmFile.delete();
            }
            if (isSource) {
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    audioFile.delete();
                }
            }
            return silkPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调用ffmpeg，wav转 pcm
     *
     * @param wavPath wav文件地址
     * @param target  转后文件地址
     */
    public static void transferWavPcm(String wavPath, String target) {
        // ffmpeg -i input.wav -f s16le -ar 44100 -acodec pcm_s16le output.raw
        transferAudioPcm(wavPath, target);
    }

    /**
     * 调用ffmpeg，mp3转 pcm
     *
     * @param mp3Path mp3文件地址
     * @param target  转后文件地址
     */
    public static void transferMp3Pcm(String mp3Path, String target) {
        //ffmpeg -y -i 源文件 -f s16le -ar 24000 -ac 1 转换后文件位置
        transferAudioPcm(mp3Path, target);
    }

    /**
     * mp3/wav 通用
     *
     * @param fpath
     * @param target
     */
    private static void transferAudioPcm(String fpath, String target) {
        List<String> commend = new ArrayList<String>();
        commend.add(path + "ffmpeg.exe");
        commend.add("-y");
        commend.add("-i");
        commend.add(fpath);
        commend.add("-f");
        commend.add("s16le");
        commend.add("-ar");
        commend.add("24000");
        commend.add("-ac");
        commend.add("-2");
        commend.add(target);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            Process p = builder.start();
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * silk_v3_encoder.exe，转成Silk格式
     *
     * @param pcmPath pcm 文件地址
     * @param target  转换后的silk地址
     *                silk_v3_encoder.exe 路径
     *                pcm文件地址
     *                silk输出地址
     *                -Fs_API <Hz>            : API sampling rate in Hz, default: 24000
     *                -Fs_maxInternal <Hz>    : Maximum internal sampling rate in Hz, default: 24000
     *                -packetlength <ms>      : Packet interval in ms, default: 20
     *                -rate <bps>            : Target bitrate;   default: 25000
     *                -loss <perc>          : Uplink loss estimate, in percent (0-100);  default: 0
     *                -complexity <comp>   : Set complexity, 0: low, 1: medium, 2: high; default: 2
     *                -DTX <flag>          : Enable DTX (0/1); default: 0
     *                -quiet               : Print only some basic values
     *                -tencent             : Compatible with QQ/Wechat
     */
    public static void transferPcmSilk(String pcmPath, String target) {
        Process process = null;
        try {
            /**
             // 1、这一节的，语音长度太长会使音频长度丢失
             List<String> commend = new ArrayList<>();
             // 指令，可参照方法注释， 请不要在commend.add()里同时写【-参数 值】
             commend.add(path + "silk_v3_encoder.exe");
             commend.add(pcmPath);
             commend.add(target);
             commend.add("-tencent");
             ProcessBuilder builder = new ProcessBuilder();
             builder.command(commend);
             process = builder.start();
             // 如果删除下班这行写process.waitFor() ，太长的语音会阻塞，BufferedReader 打印出来太长的语音也会阻塞
             process = Runtime.getRuntime().exec("taskkill -f -t -im silk_v3_encoder.exe");
             */
            // 方法2，除了会弹出弹窗，没什么问题 cmd /c 极为重要，执行完毕后会自动关闭
            process = Runtime.getRuntime().exec("cmd /c start " + path + "silk_v3_encoder.exe " + pcmPath + " " + target + " -tencent");
            process.waitFor();
            Thread.sleep(1000);
            // 有更好的方法会后续慢慢更新..
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }


    /**
     * mp3转amr（低质量qq语音）
     *
     * @param mp3Path MP3文件地址
     * @param target  转换后文件地址
     *                return
     */
    public static void transferMp3Amr(String mp3Path, String target) {
        // 被转换文件地址
        File source = new File(path);
        try {
            if (!source.exists()) {
                throw new Exception("文件不存在！");
            }
            List<String> commend = new ArrayList<String>();
            commend.add(path + "ffmpeg.exe");
            commend.add("-y");
            commend.add("-i");
            commend.add(mp3Path);
            commend.add("-ac");
            commend.add("1");
            commend.add("-ar");
            commend.add("8000");
            commend.add(target);
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(commend);
                Process p = builder.start();
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("mp3转amr异常" + e);
        }
    }

}

