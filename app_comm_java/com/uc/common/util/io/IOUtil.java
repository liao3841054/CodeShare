/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : 2012-7-2
 * <p>
 * Description : IOUtil.java
 * <p>
 * Creation    : 2012-11-10
 * Author      : raorq@ucweb.com
 * History     : Creation, 2012-11-10, raorq, Create the file
 * ****************************************************************************
 */
package com.uc.common.util.io;

import android.database.Cursor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;


public final class IOUtil {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final String UTF_8 = "UTF-8";

    /**
     *
     * <p>从输入流中读取部份数据(字节数组)</p>
     *
     * <b>修改历史</b>
     * <ol>
     * <li>创建（Added by luogw on 2011-11-22）</li>
     * </ol>
     * @param aInput 输入流
     * @param aReadLength 读取的长度
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream aInput, int aReadLength) throws IOException {
        if (aInput == null || aReadLength <= 0) {
            return null;
        }
        byte[] sData = new byte[aReadLength];

        int sLength;
        for (int i = 0; i < aReadLength; ) {
            if (aReadLength - i < DEFAULT_BUFFER_SIZE)
                sLength = aInput.read(sData, i, aReadLength - i);
            else
                sLength = aInput.read(sData, i, DEFAULT_BUFFER_SIZE);

            if (sLength == -1)
                break;
            i += sLength;
        }

        return sData;
    }

    /**
     *
     * @param input 调用者注意关闭InputStream
     * @return
     */
    public static byte[] readBytes(InputStream input) {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        try {
            copy(input, baos, DEFAULT_BUFFER_SIZE);
            return baos.toByteArray();
        } catch (IOException e) {
            // ignore
        } finally {
            safeClose(baos);
        }
        return null;
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static void safeClose(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static void safeClose(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * stream copy
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        if (input == null || output == null) {
            return -1;
        }
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     *
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    public static long copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
        if (input == null || output == null) {
            return -1;
        }
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        output.flush();
        return count;
    }

    /**
     *
     * @param input 请调用者关闭流, 本方法不会关闭
     * @return 一定非null
     * @throws IOException
     */
    public static List<String> readLines(final InputStream input) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     *
     * @param lines
     * @param lineEnding null则使用"\n"
     * @param output 请调用者关闭
     * @throws IOException
     */
    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output) throws IOException {
        if (lines != null) {
            if (lineEnding == null) {
                lineEnding = "\n";
            }
            byte[] lineEndingBytes = lineEnding.getBytes(UTF_8);
            for (final Object line : lines) {
                if (line != null) {
                    output.write(line.toString().getBytes(UTF_8));
                }
                output.write(lineEndingBytes);
            }
        }
    }
}
