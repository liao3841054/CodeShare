package com.uc.common.util.os;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.uc.common.util.io.IOUtil;

import java.io.InputStream;

/**
 * Created by wx107452@alibaba-inc.com on 2017/4/28.
 */

public class UriUtil {

    /**
     * 通过Uri获取图片真正路径:比如 uri content://media/external/images/media/70020
     *
     * @param contentUri
     * @return
     */
    public static String getImageRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = ApplicationContext.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }

        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        } finally {
            IOUtil.safeClose(cursor);
        }
        return "";
    }

    /**
     * 彩信通过Uri获取图片Bitmap  彩信 content://mms/part/4
     * @param partURI
     * @return
     */
    public static Bitmap getMmsImageBitmap(Uri partURI) {
        InputStream in = null;
        Bitmap bitmap = null;
        try {
            in = ApplicationContext.getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Throwable e) {
//            ExceptionHandler.processFatalException(e);
        } finally {
            IOUtil.safeClose(in);
        }
        return bitmap;
    }

}
