package com.uc.common.util.compress;

import com.uc.common.util.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/5.
 */

public class DeflateUtil {
    /**
     * 使用算法解压数据，不涉及zip文件格式，与{@link DeflateUtil#deflateData(byte[])} 对应
     * 目前用于云同步
     * @param data
     * @return
     */
    public static byte[] inflateData(byte[] data) {
        if (null == data || 0 == data.length) {
            return null;
        }

        byte[] result = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        InflaterInputStream gis = null;
        try {
            baos = new ByteArrayOutputStream();
            bais = new ByteArrayInputStream(data);
            gis = new InflaterInputStream(bais);

            byte[] tmpBuf = new byte[4096];
            int readlen = 0;
            while ((readlen = gis.read(tmpBuf)) != -1) {
                baos.write(tmpBuf, 0, readlen);
            }

            gis.close();
            result = baos.toByteArray();

            bais.close();
            baos.close();
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtil.safeClose(gis);
            IOUtil.safeClose(bais);
            IOUtil.safeClose(baos);
        }

        return result;
    }

    /**
     * 使用算法压缩数据，不涉及文件格式，与{@link DeflateUtil#inflateData(byte[])} 对应
     * 目前用于云同步
     * @param data
     * @return
     */
    public static byte[] deflateData(byte[] data) {
        if(data == null || 0 == data.length){
            return null;
        }

        byte[] result = null;
        ByteArrayOutputStream bos = null;
        DeflaterOutputStream zip = null;
        try {
            bos = new ByteArrayOutputStream();
            zip = new DeflaterOutputStream(bos);
            zip.write(data);
            zip.close();
            result = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            // ignore
        } finally {
            IOUtil.safeClose(zip);
            IOUtil.safeClose(bos);
        }
        return result;
    }
}
