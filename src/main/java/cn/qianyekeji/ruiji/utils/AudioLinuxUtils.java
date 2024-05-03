package cn.qianyekeji.ruiji.utils;

import io.github.mzdluo123.silk4j.LameCoder;
import io.github.mzdluo123.silk4j.NativeLibLoader;
import io.github.mzdluo123.silk4j.SilkCoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AudioLinuxUtils {
    private static File tempDir;

    public static void main(String[] args) {
        // 初始化
        try {
            AudioLinuxUtils.init();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 测试MP3转SILK
        File mp3File = new File("C:\\Users\\qianye\\Desktop\\2222\\5270.mp3");
        try {
            File silkFile = AudioLinuxUtils.mp3ToSilk(mp3File);
            System.out.println("MP3转SILK成功，SILK文件路径：" + silkFile.getAbsolutePath());

            // 获取 MP3 文件名前缀
            String mp3FileName = mp3File.getName();
            int lastIndex = mp3FileName.lastIndexOf('.');
            String silkFileNamePrefix = lastIndex != -1 ? mp3FileName.substring(0, lastIndex) : mp3FileName;

            // 将生成的 SILK 文件复制到与 MP3 文件相同的目录下，并设置文件名前缀
            String mp3FileParentDirectory = mp3File.getParent();
            File copiedSilkFile = new File(mp3FileParentDirectory + File.separator + silkFileNamePrefix + ".silk");
            if (silkFile.renameTo(copiedSilkFile)) {
                System.out.println("已将生成的 SILK 文件复制到目录：" + copiedSilkFile.getParent());
            } else {
                System.err.println("无法将 SILK 文件移动到目录：" + mp3FileParentDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }




        // 测试SILK转MP3
//        File silkFile = new File("C:\\Users\\qianye\\Desktop\\2222\\5270.sil");
//        try {
//            File mp3FileConverted = AudioLinuxUtils.silkToMp3(silkFile);
//            System.out.println("SILK转MP3成功，MP3文件路径：" + mp3FileConverted.getAbsolutePath());
//
//            // 获取SILK文件所在目录路径和文件名前缀
//            String silkFilePath = silkFile.getAbsolutePath();
//            String directoryPath = silkFilePath.substring(0, silkFilePath.lastIndexOf(File.separator) + 1);
//            String silkFileName = silkFile.getName();
//            int lastIndex = silkFileName.lastIndexOf('.');
//            String mp3FileNamePrefix = lastIndex != -1 ? silkFileName.substring(0, lastIndex) : silkFileName;
//
//            // 将生成的 MP3 文件复制到 SILK 文件所在目录，并设置文件名前缀
//            File copiedMp3File = new File(directoryPath + File.separator + mp3FileNamePrefix + ".mp3");
//            Files.copy(mp3FileConverted.toPath(), copiedMp3File.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            System.out.println("已将生成的 MP3 文件复制到目录：" + copiedMp3File.getParent());
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }


    }

    /**
     * 初始化
     */
    public static void init() throws IOException {
        init(new File(System.getProperty("java.io.tmpdir")));
    }

    public static void init(File tmpDir) throws IOException {
        AudioLinuxUtils.tempDir = tmpDir;
        if (!tempDir.canWrite()) {
            throw new IOException("缓存目录无写入权限，请重试");
        }
        NativeLibLoader.load();
    }

    public static File mp3ToSilk(File mp3File, int bitRate) throws IOException {
        if (!mp3File.exists() || mp3File.length() == 0) {
            throw new IOException("文件不存在或为空");
        }
        File pcmFile = getTempFile("pcm");
        File silkFile = getTempFile("silk");
        int sampleRate = LameCoder.decode(mp3File.getAbsolutePath(), pcmFile.getAbsolutePath());
        SilkCoder.encode(pcmFile.getAbsolutePath(), silkFile.getAbsolutePath(), sampleRate, bitRate);
        pcmFile.delete();
        return silkFile;
    }

    public static File mp3ToSilk(File mp3File) throws IOException {
        return mp3ToSilk(mp3File,24000);
    }

    public static File mp3ToSilk(InputStream mp3FileStream, int bitRate) throws IOException {
        File mp3File = getTempFile("mp3");
        streamToTempFile(mp3FileStream, mp3File);
        return mp3ToSilk(mp3File, bitRate);
    }

    public static File mp3ToSilk(InputStream mp3FileStream) throws IOException {
        return mp3ToSilk(mp3FileStream,24000);
    }

    public static File silkToMp3(File silkFile, int bitrate) throws IOException {
        if (!silkFile.exists() || silkFile.length() == 0) {
            throw new IOException("文件不存在或为空");
        }
        File pcmFile = getTempFile("pcm");
        File mp3File = getTempFile("mp3");
        SilkCoder.decode(silkFile.getAbsolutePath(), pcmFile.getAbsolutePath());
        LameCoder.encode(pcmFile.getAbsolutePath(), mp3File.getAbsolutePath(), bitrate);
        pcmFile.delete();
        return mp3File;
    }

    public static File silkToMp3(File silkFile) throws IOException {
        return silkToMp3(silkFile, 24000);
    }

    public static File silkToMp3(InputStream silkFileStream, int bitrate) throws IOException {
        File mp3File = getTempFile("silk");
        streamToTempFile(silkFileStream, mp3File);
        return silkToMp3(mp3File, bitrate);
    }

    public static File silkToMp3(InputStream silkFileStream) throws IOException {
        return silkToMp3(silkFileStream, 24000);
    }

    static void streamToTempFile(InputStream inputStream, File tmpFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buf)) > 0) {
            fileOutputStream.write(buf, 0, bytesRead);
        }
        inputStream.close();
        fileOutputStream.close();
    }


    static File getTempFile(String type) {
        String fileName = "mirai_audio_" +
                type +
                "_" +
                System.currentTimeMillis() +
                "." +
                type;
        return new File(tempDir, fileName);
    }
}

