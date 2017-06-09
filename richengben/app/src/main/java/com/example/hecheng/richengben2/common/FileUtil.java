package com.example.hecheng.richengben2.common;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.sendtion.xrichtext.SDCardUtil.getPictureDir;

/**
 * Created by Administrator on 2017/5/10.
 */

public class FileUtil {
    public static void uploadBmobFile(String filePath, final BaseListener<String> listener) {
        final BmobFile bmobFile = new BmobFile(new File(filePath));
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.d("FileUtil", ""+ bmobFile.getFileUrl());
                    listener.getSuccess(bmobFile.getFileUrl());
                }else{
                    Log.d("FileUtil", "" + e.getMessage());
                }
            }

            @Override
            public void onProgress(Integer value) {
            }
        });
    }

    public static void downloadBmobFile(String fileName, String fileUrl, String imgLocalPath,final BaseListener<Exception> listener) {
        BmobFile bmobfile =new BmobFile(fileName,"",fileUrl);
        String savePath = imgLocalPath;
        bmobfile.download(new File(savePath), new DownloadFileListener() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    listener.getSuccess(null);
                    Log.d("FileUtil", ""+ s);
                }else{
                    listener.getFailure(e);
                    Log.d("FileUtil", "" + e.getMessage());
                }
            }

            @Override
            public void onProgress(Integer integer, long l) {

            }
        });
    }

    public static String saveToSdCard(Bitmap bitmap) {
        String imageUrl = getPictureDir() + System.currentTimeMillis() + ".jpg";
        File file = new File(imageUrl);

        try {
            FileOutputStream e = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, e)) {
                e.flush();
                e.close();
            }
        } catch (FileNotFoundException var4) {
        } catch (IOException var5) {
        }
        return file.getAbsolutePath();
    }
}
