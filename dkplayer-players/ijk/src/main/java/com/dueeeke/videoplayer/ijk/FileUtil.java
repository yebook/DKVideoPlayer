package com.dueeeke.videoplayer.ijk;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Created by kermitye on 2019/8/20 14:04
public class FileUtil {

    public static ConcatBean createConcatFile(List<String> fileList) {
        ConcatBean concatBean = new ConcatBean();

        File cacheFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ijkCache/" + System.currentTimeMillis() + ".concat");
        concatBean.mConcatFile = cacheFile;
        if (!cacheFile.getParentFile().exists()) {
            cacheFile.getParentFile().mkdirs();
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile));
            out.write("ffconcat version 1.0\r\n");
            ArrayList<VideoInfoBean> videos = new ArrayList<>();
            for (String path : fileList) {
                out.write("file '" + path + "'\r\n");
                float duration = getLocalVideoDuration(path);
                out.write("duration " + new DecimalFormat(".0").format(duration / 1000) + "\r\n");
                videos.add(new VideoInfoBean(path, duration));
            }
            concatBean.mVideos = videos;
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return concatBean;
    }

    public static Float getLocalVideoDuration(String videoPath) {
        float duration;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Float.parseFloat(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION));
            Log.e("kermitye", "=====getLocalVideoDuration: " + videoPath + " / " + duration);
        } catch (Exception e) {
            e.printStackTrace();
            return 0f;
        }
        return duration;
    }

    public static void remvoeCache() {
        final File cacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ijkCache");
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteFile(cacheDir);
            }
        }).start();
    }


    public static void deleteFile(File file) {
        if (file == null || !file.exists())
            return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length - 1; i++) {
                deleteFile(files[i]);
            }
            file.delete();
        } else {
            file.delete();
        }
    }
    



}
