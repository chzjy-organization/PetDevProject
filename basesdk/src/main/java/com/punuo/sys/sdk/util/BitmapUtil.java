package com.punuo.sys.sdk.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.AsyncTask;

import com.luck.picture.lib.config.PictureMimeType;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by han.chen.
 * Date on 2019-07-23.
 **/
public class BitmapUtil {

    public interface SaveCallback {
        void onSaveSuccess(String filePath);
        void onSaveFail();
    }

    public static boolean isJPEG(String path) {
        String type = PictureMimeType.getLastImgType(path);
        return type.equals(".jpg") || type.equals(".JPEG") || type.equals(".jpeg");
    }

    public static Bitmap createBitmap(byte[] bytes, int width, int height) {
        YuvImage image = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
        Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void saveBitmapAsync(final Context context, final Bitmap bitmap,
                                       final String parentDir, final String fileName, final SaveCallback callback) {
        new AsyncTask<Bitmap, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Bitmap... strings) {
                return saveBitmap(context, bitmap, parentDir, fileName);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success != null && success) {
                    callback.onSaveSuccess(parentDir + fileName);
                } else {
                    callback.onSaveFail();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bitmap);
    }

    public static boolean saveBitmap(Context context, Bitmap bitmap, String parentDir, String fileName) {
        boolean isSuccess = false;
        if (context == null || bitmap == null || bitmap.isRecycled()) {
            return false;
        }
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png")) {
            fileName = fileName + ".jpg";
        }
        File file = new File(parentDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSilently(fos);
        }
        return isSuccess;
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
