/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : 2012-7-2
 * <p>
 * Description : TrafficData.java
 * <p>
 * Creation    : 2012-11-13
 * Author      : raorq@ucweb.com
 * History     : Creation, 2012-11-13, raorq, Create the file
 * ****************************************************************************
 */
package com.uc.common.util.io;


import android.content.res.AssetManager;

import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.text.StringUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 * @author raorq
 * @version 1.0
 * 修改记录 zhanglk 把原来 fileoperator相关的代码移到这里
 */

public final class FileUtil {
    /**
     * 计算文件夹大小
     * @param file
     * @return
     */
    public static long sizeOf(final File file) {
        if (file == null || !file.exists()) {
            return 0L;
        }
        if (file.isDirectory()) {
            long totalSize = 0L;
            File[] fileList = file.listFiles();
            if (null != fileList) {
                for (File subitem : fileList) {
                    totalSize += sizeOf(subitem);
                }
            }
            return totalSize;
        } else {
            return file.length();
        }
    }

    /**
     * Reads a text file.
     *
     * @param fileName the name of the text file
     * @return the lines of the text file
     * @throws FileNotFoundException when the file was not found
     * @throws IOException when file could not be read.
     */
    public static List<String> readLines(String fileName) throws IOException {
        return readLines(new File(fileName));
    }

    /**
     * Reads a text file.
     *
     * @param file the text file
     * @return the lines of the text file
     * @throws IOException when file could not be read.
     */
    public static List<String> readLines(File file) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return IOUtil.readLines(new FileInputStream(file));
        } finally {
            IOUtil.safeClose(inputStream);
        }
    }

    /**
     * Writes (and creates) a text file.
     *
     * @param file the file to which the text should be written
     * @param lines the text lines of the file in a collection with String-values
     * @throws IOException when there is an input/output error during the saving
     */
    public static void writeLines(File file, Collection<?> lines) throws IOException {
        writeLines(file, lines, null, false);
    }

    /**
     * Writes (and creates) a text file.
     * @param file
     * @param lines
     * @param isAppend, use true to append lines to the end of the file, or false to overrwrite the file
     * @throws IOException
     */
    public static void writeLines(File file, Collection<?> lines, String lineEnding, boolean isAppend) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        BufferedOutputStream bufferedOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, isAppend);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            IOUtil.writeLines(lines, lineEnding, bufferedOutputStream);
            bufferedOutputStream.flush();
        } finally {
            IOUtil.safeClose(fileOutputStream);
            IOUtil.safeClose(bufferedOutputStream);
        }
    }

    /**
     * 将srcFile复制到destDir目录下, 如果已存在同名文件会覆盖
     * @param srcFile 不能传null
     * @param destDir 不能传null
     * @throws IOException
     */
    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        final File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile);
    }

    /**
     * Copies a file. 如果destFile已存在, 则覆盖
     *
     * @param srcFile 不能为目录
     * @param destFile 不能为目录
     * @throws IOException when there is an error while copying the file.
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        File parent = destFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);
            int read;
            final byte[] buffer = new byte[64 * 1024];
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            IOUtil.safeClose(inputStream);
            IOUtil.safeClose(outputStream);
        }
    }

    /**
     * 将srcDir文件夹复制到destDir
     * @param srcDir
     * @param destDir destDir是srcDir的新名字及位置
     * @throws IOException
     */
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new IOException("Source [" + srcDir + "] exists but is not a directory");
        }
        final File[] srcFiles = srcDir.listFiles();
        if (srcFiles != null) {
            if (destDir.exists()) {
                if (!destDir.isDirectory()) {
                    throw new IOException("Destination [" + destDir + "] exists but is not a directory");
                }
            } else {
                if (!destDir.mkdirs() && !destDir.isDirectory()) {
                    throw new IOException("Destination '" + destDir + "' directory cannot be created");
                }
            }
            for (final File srcFile : srcFiles) {
                final File destFile = new File(destDir, srcFile.getName());
                if (srcFile.isDirectory()) {
                    copyDirectory(srcFile, destFile);
                } else {
                    copyFile(srcFile, destFile);
                }
            }
        }
    }

    /**
     * Deletes a file or a directory.
     *
     * @param file the file or directory which should be deleted.
     * @return true when the file deleted
     */
    public static boolean delete(final File file) {
        boolean result = false;
        if (file != null) {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
            try {
                result = file.delete();
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
    }

    /**
     * 清空directory目录中的内容
     * @param directory
     */
    public static void cleanDirectory(final File directory) {
        if (directory != null && directory.isDirectory()) {
            final File[] files = directory.listFiles();
            if (files != null) {
                for (final File file : files) {
                    delete(file);
                }
            }
        }
    }

    /**
     * List all files in the given directory satisfy the filter.
     *
     * @param dir
     *          Directory to list.
     * @param filter
     *          A file filter.
     * @param recursive
     *          If true, all the sub directories will be listed too.
     * @return
     *          Array of files in the given directory and satisfy the filter.
     */
    public static List<File> listFiles(File dir, FileFilter filter, boolean recursive) {
        List<File> result = new ArrayList<File>();

        if (!dir.exists() || dir.isFile()) {
            return result;
        }

        File[] fArray = dir.listFiles(filter);

        if (fArray == null) {
            return result;
        }

        List<File> fList = Arrays.asList(fArray);

        if (!recursive) {
            return fList;
        }

        LinkedList<File> linkedList = new LinkedList<File>(fList);

        while (!linkedList.isEmpty()) {
            File f = linkedList.removeFirst();
            result.add(f);

            if (f.isDirectory()) {
                File[] array = f.listFiles(filter);

                if (array == null) {
                    continue;
                }

                for (int i = 0; i < array.length; i++) {
                    linkedList.addLast(array[i]);
                }
            }
        }

        return result;
    }

    public static boolean mkDirs(String filepath) {
        final File dir = new File(filepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.isDirectory();
    }

    public static boolean writeBytes(String filePath, String fileName, byte[] data) {
        if (data == null) {
            return false;
        }
        return writeBytes(filePath, fileName, data, 0, data.length);
    }

    /**
     * 请尽量将这个函数放入ThreadManager的后台线程执行，防止引入严重卡顿
     * @param filePath
     * @param fileName
     * @param headData
     * @param bodyData
     * @param bodyOffset
     * @param bodyLen
     * @param forceFlush 请慎重使用这个参数，如果设置为true可能导致严重卡顿，甚至ANR，如果不是极为重要的数据，请设置为false。
     * @return
     */
    public static boolean writeBytes(String filePath, String fileName, byte[] headData, byte[] bodyData,
                                     int bodyOffset, int bodyLen, boolean forceFlush) throws IOException {
        if (StringUtil.isEmpty(filePath) || StringUtil.isEmpty(fileName) || bodyData == null) {
            return false;
        }

        String tempFileName = System.currentTimeMillis() + fileName;

        File tempFile = createNewFile(filePath + tempFileName);

        boolean result = writeBytesBase(tempFile, headData, bodyData, bodyOffset, bodyLen, forceFlush);
        if (!result) {
            return false;
        }

        String srcPath = filePath + fileName;
        if (!rename(tempFile, srcPath)) {
            //rename srcPath到bakPath后再 delete bakPath，替代直接 delete srcPath
            String bakPath = filePath + ".bak";
            delete(bakPath);
            rename(new File(srcPath), bakPath);

            result = rename(tempFile, srcPath);
            if (!result) {
                return false;
            }

            delete(bakPath);
        }

        return true;
    }

    public static boolean writeBytes(String filePath, String fileName, byte[] data, int offset, int len) {
        try {
            return writeBytes(filePath, fileName, null, data, offset, len, false);
        } catch (FileNotFoundException e) {
//            ExceptionHandler.processFatalException(e);
        } catch (IOException e) {
//            ExceptionHandler.processFatalException(e);
        }
        return false;
    }

    /**
     * 请尽量将这个函数放入ThreadManager的后台线程执行，防止引入严重卡顿
     * @param file
     * @param headData
     * @param bodyData
     * @param bodyOffset
     * @param bodyLen
     * @param forceFlush 请慎重使用这个参数，如果设置为true可能导致严重卡顿，甚至ANR，如果不是极为重要的数据，请设置为false。
     * @return
     */
    private static boolean writeBytesBase(File file, byte[] headData, byte[] bodyData, int bodyOffset, int bodyLen,
             boolean forceFlush) throws IOException {
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(file);
            if (headData != null) {
                fileOutput.write(headData);
            }
            fileOutput.write(bodyData, bodyOffset, bodyLen);
            fileOutput.flush();
            if (forceFlush) {
                FileDescriptor fd = fileOutput.getFD();
                if (fd != null) {
                    fd.sync(); // 立刻刷新，保证文件可以正常写入;          
                }
            }
            return true;
        } finally {
            IOUtil.safeClose(fileOutput);
        }
    }

    /**
     * 请尽量将这个函数放入ThreadManager的后台线程执行，防止引入严重卡顿
     * @param file
     * @param headData
     * @param bodyData
     * @param bodyOffset
     * @param bodyLen
     * @param forceFlush 请慎重使用这个参数，如果设置为true可能导致严重卡顿，甚至ANR，如果不是极为重要的数据，请设置为false。
     * @return
     */
    public static boolean writeBytes(File file, byte[] headData, byte[] bodyData, int bodyOffset, int bodyLen, boolean forceFlush) {
        try {
            return writeBytesBase(file, headData, bodyData, bodyOffset, bodyLen, forceFlush);
        } catch (FileNotFoundException e) {
//            ExceptionHandler.processFatalException(e);
        } catch (IOException e) {
//            ExceptionHandler.processFatalException(e);
        }
        return false;
    }

    public static boolean writeBytes(File file, byte[] data, int offset, int len) {
        return writeBytes(file, null, data, offset, len, false);
    }

    public static File createNewFile(String path) {
        return createNewFile(path, false);
    }

    public static boolean rename(File file, String newName) {
        return file.renameTo(new File(newName));
    }

    public static boolean delete(String path) {
        return delete(new File(path));
    }

    /**
     *
     * @param path
     *            ：文件路径
     * @param append
     *            ：若存在是否插入原文件
     * @return File
     */
    public static File createNewFile(String path, boolean append) {
        File newFile = new File(path);
        if (!append) {
            if (newFile.exists()) {
                newFile.delete();
            }
        }
        if (!newFile.exists()) {
            try {
                File parent = newFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                newFile.createNewFile();
            } catch (Exception e) {
                //#if (debug == true)
//                ExceptionHandler.processFatalException(e);
                //#endif
            }
        }
        return newFile;
    }

    public static byte[] readBytes(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        return readBytes(new File(filePath));
    }

    public static byte[] readBytes(File file) {
        FileInputStream fileInput = null;
        try {
            if (file.exists()) {
                fileInput = new FileInputStream(file);
                return IOUtil.readBytes(fileInput);
            }
        } catch (Exception e) {
//            ExceptionHandler.processSilentException(e);
        } finally {
            IOUtil.safeClose(fileInput);
        }
        return null;
    }

    /**
     * 判读目录是否存在
     * @param directory 目录路径
     * @return
     */
    public static boolean isDirectoryExists(String directory) {
        if (!StringUtil.isEmpty(directory)) {
            try {
                if (new File(directory).isDirectory()) {
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    public static boolean isFileExists(String strFile) {
        if (StringUtil.isEmpty(strFile)) {
            return false;
        }
        try {
            File file = new File(strFile);
            return file.exists();
        } catch (Exception e) {
//                ExceptionHandler.processFatalException(e);
            return false;
        }
    }

    /*
     * 拷贝asset目录下文件到指定文件
     * @param  relativeAssetsFile 相对路径
     * @param  destFile 绝对路径
     */
    public static boolean copyAssetFile(String relativeAssetsFile, String destFile) {
        boolean ret = true;
        InputStream in = null;
        OutputStream out = null;

        File parent = new File(destFile).getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try {
            AssetManager mAssetMgr = ApplicationContext.getAssetManager();
            in = mAssetMgr.open(relativeAssetsFile);

            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[8096];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            ret = false;
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(out);
        }
        return ret;
    }

    /**
     * 传入路径，获取文件大小，不存在则文件大小为0
     * @param path
     * @return
     */
    public static long getFileSize(String path) {
        long size = 0;
        File file = new File(path);
        if (file.exists()) {
            size = file.length();
        }
        return size;
    }

    /**
     * 判断一个文件是否是一个软连接
     * @param filePath
     * @return
     */
    public static boolean isSymlink(String filePath) {
        if (StringUtil.isEmptyWithTrim(filePath)) {
            return false;
        }
        File file = new File(filePath);
        File canon = null;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir;
            try {
                canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            } catch (IOException e) {
//                ExceptionHandler.processSilentException(e);
            }
        }
        try {
            return canon != null && !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (IOException e) {
//            ExceptionHandler.processSilentException(e);
        }
        return false;
    }

    public static boolean isFileInDirectory(File file, File directory) {
        if (null == file || null == directory) {
            return false;
        }

        boolean ret = false;

        try {
            String destFilePath = file.getCanonicalPath();
            String destDirPath = directory.getCanonicalPath();
            if (destDirPath.endsWith("/") == false) {
                destDirPath = destDirPath + "/";
            }
            ret = destFilePath.startsWith(destDirPath);
        } catch (IOException e) {
            // ignore
        }

        return ret;
    }
} 
