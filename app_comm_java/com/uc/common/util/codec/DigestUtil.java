/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : StringUtil.java
 * <p>
 * Description : To convert a string to MD5/SHA1/etc.
 * <p>
 * Creation    : 2014-1-16
 * Author      : liaofei@ucweb.com
 * History     : Creation, 2014-1-16, liaofei, Create the file
 * ****************************************************************************
 */
package com.uc.common.util.codec;

import com.uc.common.util.io.IOUtil;
import com.uc.common.util.text.StringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


public class DigestUtil {
    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    public static MessageDigest getSha1Digest() {
        return getDigest("SHA1");
    }

    public static byte[] md5(byte[] data) {
        return getMd5Digest().digest(data);
    }

    public static byte[] md5(InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    public static byte[] md5(String data) {
        return md5(StringUtil.getBytesUTF8(data));
    }

    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    public static String md5Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(md5(data));
    }

    public static String md5Hex(String data) {
        return Hex.encodeHexString(md5(data));
    }

    public static byte[] sha1(final byte[] data) {
        return getSha1Digest().digest(data);
    }

    public static byte[] sha1(final InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    public static byte[] sha1(final String data) {
        return sha1(StringUtil.getBytesUTF8(data));
    }

    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    public static String sha1Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha1(data));
    }

    public static String sha1Hex(String data) {
        return Hex.encodeHexString(sha1(data));
    }

    public static byte[] digest(MessageDigest digest, InputStream data)
            throws IOException {
        return updateDigest(digest, data).digest();
    }

    public static MessageDigest updateDigest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];
        int read = data.read(buffer, 0, 1024);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, 1024);
        }

        return digest;
    }

    /**
     * 不建议有这个方法
     * @param file
     * @return
     * @throws IOException
     */
    public static String md5Hex(File file) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return md5Hex(fileInputStream);
        } finally {
            IOUtil.safeClose(fileInputStream);
        }
    }


    /**
     * 生成文件的md5校验值, 结果经过Hex.encodeHexString处理
     *
     * @param file
     * @param millisecondTimeout 计算超时，单位为毫秒，如果值小于等于0，则不做超时判断
     * @return
     * @throws IOException
     */
    public static String getFileMd5String(File file, long millisecondTimeout) throws IOException {
        long startTime = System.currentTimeMillis();

        String result = "";
        MessageDigest messagedigest = getMd5Digest();
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[16384];
            int numRead = 0;
            while ((numRead = bis.read(buffer)) > 0) {
                long currentTime = System.currentTimeMillis();
                if ((millisecondTimeout > 0) && (currentTime - startTime > millisecondTimeout)) {
                    // 若超出限制时间，则返回空串
                    messagedigest.reset();
                    return result;
                }

                messagedigest.update(buffer, 0, numRead);
            }

            byte[] s = messagedigest.digest();
            result = Hex.encodeHexString(s);
        } finally {
            IOUtil.safeClose(bis);
            IOUtil.safeClose(fis);
        }

        return result;
    }

    /**
     * 进行文件的md5校验
     *
     * @param file 需要校验md5的文件
     * @param md5 正确的md5
     * @param millisecondTimeout 计算超时，单位为毫秒，如果值小于等于0，则不做超时判断
     * @return
     */
    public static boolean checkFileMd5(File file, String md5, long millisecondTimeout) {
        if (file == null || !file.exists() || !file.isFile() || StringUtil.isEmpty(md5)) {
            return false;
        }

        String fileMd5 = "";
        try {
            fileMd5 = getFileMd5String(file, millisecondTimeout);
            if (StringUtil.isEmpty(fileMd5)) {
                return false;
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }

        return md5.trim().toLowerCase(Locale.ENGLISH).equals(fileMd5);
    }
}
