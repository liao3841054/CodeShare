package com.uc.common.util.text;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/20.
 */

public class FilenameUtil {
    private static final String FILE_PROTOCOL_PREFIX = "file://";
    private static final int NOT_FOUND = -1;

    /**
     * Return the file name, if it is a directory, return empty.
     * Examples:
     * <blockquote><pre>
     * getName("/sdcard/backup/") returns ""
     * getName("/sdcard/backup/test") returns "test"
     * </pre></blockquote>
     *
     * @param filename File path.
     * @return File name. 当filename为null, 返回null, 其他情况至少返回""
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);

    }

    /**
     * Return the directory path. If given path is a file path, the result is its parents path.
     * If given path is a directory path, the result is itself.
     * <p>
     * Examples:
     * <blockquote><pre>
     * getPath("/sdcard/backup/") returns "/sdcard/backup/"
     * getPath("/sdcard/backup/test") returns "/sdcard/backup/"
     * </pre></blockquote>
     *
     * @param filename File or directory path.
     * @return filename==null时返回null, 其他至少会返回""
     */
    public static String getPath(String filename) {
        return doGetPath(filename, true);
    }

    /**
     * @param filename
     * @return filename==null时返回null, 其他至少会返回"", 路径后面不会带有"/"
     */
    public static String getPathNoEndSeparator(final String filename) {
        return doGetPath(filename, false);
    }

    private static String doGetPath(final String filename, final boolean withEndSeparator) {
        String path = null;
        if (filename != null) {
            int index = indexOfLastSeparator(filename);
            if (index == NOT_FOUND) {
                path = "";
            } else {
                path = filename.substring(0, index + (withEndSeparator ? 1 : 0));
            }
        }
        return path;
    }

    /**
     * Return the last slash index.
     *
     * @param filename
     * @return
     */
    public static int indexOfLastSeparator(String filename) {
        int index = NOT_FOUND;
        if (!StringUtil.isEmpty(filename)) {
            int unixSlash = filename.lastIndexOf('/');
            int windowsSlash = filename.lastIndexOf('\\');
            index = Math.max(unixSlash, windowsSlash);
        }
        return index;
    }

    public static String removeFileProtocolPrefix(String uri) {
        if (!StringUtil.isEmpty(uri)) {
            if (uri.length() > FILE_PROTOCOL_PREFIX.length()) {
                if (uri.startsWith(FILE_PROTOCOL_PREFIX)) {
                    uri = uri.substring(FILE_PROTOCOL_PREFIX.length());
                }
            }
        }
        return uri;
    }

    public static String addFileProtocolPrefix(String filename) {
        if (!filename.startsWith(FILE_PROTOCOL_PREFIX)) {
            filename = StringUtil.merge(FILE_PROTOCOL_PREFIX, filename);
        }
        return filename;
    }


    private static final String[] mCs = new String[]{"/", "\\", "?", "*", ":", "<", ">", "|", "\""};

    public static boolean isFilenameValid(String filename) {
        if (StringUtil.isEmpty(filename)) {
            return false;
        }

        filename = filename.trim();
        if (filename.length() == 0) {
            return false;
        }

        for (String c : mCs) {
            if (filename.contains(c)) {
                return false;
            }
        }
        if (containsSurrogateChar(filename)) {
            return false;
        }
        return true;
    }

    /**
     * 把文件名中不合法的字符删掉
     *
     * @param filename
     * @return
     */
    public static String fixFilename(final String filename) {
        if (null == filename) {
            return null;
        }
        String result = filename;
        for (String c : mCs) {
            result = result.replace(c, "");
        }
        if (containsSurrogateChar(result)) {
            result = removeSurrogateChars(result);
        }
        return result;
    }

    /**
     * 删除Unicode代理区字符
     * android java String 内部使用UTF-16编码,不能识别路径有这种字符的文件
     * 代理区代码:范围为 [0xD800, 0xDFFF] 用于表示UTF-16编码中Unicode编码大于等于0x10000范围的字符
     * 参考:http://zh.wikipedia.org/wiki/UTF-16
     */
    private static final char UNICODE_SURROGATE_START_CHAR = 0xD800;
    private static final char UNICODE_SURROGATE_END_CHAR = 0xDFFF;
    private static String removeSurrogateChars(String string) {
        if (StringUtil.isEmpty(string)) {
            return string;
        }
        int length = string.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c < UNICODE_SURROGATE_START_CHAR || c > UNICODE_SURROGATE_END_CHAR) {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    private static boolean containsSurrogateChar(String string) {
        if(StringUtil.isEmpty(string)) {
            return false;
        }
        int length = string.length();
        boolean hasSurrogateChar = false;
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (UNICODE_SURROGATE_START_CHAR <= c && c <= UNICODE_SURROGATE_END_CHAR) {
                hasSurrogateChar = true;
                break;
            }
        }
        return hasSurrogateChar;
    }

    public static String clipFilename(String fileName, int maxLen) {
        if (null == fileName || fileName.length() < maxLen) {
            return fileName;
        }

        if (maxLen <= 0) {
            return fileName;
        }

        String extension = getExtension(fileName);
        // 没有后缀名，直接从文件名中截取
        if (StringUtil.isEmpty(extension)) {
            return fileName.substring(0, maxLen);
        }

        int nameEndIndex = maxLen - extension.length() - 1;
        if (nameEndIndex < 0) {
            return fileName.substring(0, maxLen);
        }
        return fileName.substring(0, nameEndIndex) + "." + extension;
    }

    /**
     * 返回后缀, 不带"."
     * <pre>
     *     getExtension("a/b/c.jpg") = "jpg"
     *     getExtension("a/b.txt/c") = ""
     *     getExtension("a/b/c") = ""
     *     getExtension("a/b/.jpg") = "jpg"
     *     getExtension("a/b/c.") = ""
     *     getExtension(null) = null
     * </pre>
     *
     * @param filename
     * @return 如果filename==null, 则返回null
     */
    public static String getExtension(String filename) {
        String extension = null;
        if (filename != null) {
            final int dotIndex = indexOfExtension(filename);
            if (dotIndex == NOT_FOUND) {
                extension = "";
            } else {
                extension = filename.substring(dotIndex + 1);
            }
        }
        return extension;
    }

    public static int indexOfExtension(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int extensionPos = filename.lastIndexOf('.');
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    /**
     * a/b/c.txt  c
     * a.txt      a
     * a/b/c      c
     * a/b/c/     ""
     *
     * @param filename
     * @return filename==null时返回null
     */
    public static String getBaseName(final String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * 移除扩展名
     * foo.txt     foo
     * a\b\c.jpg   a\b\c
     * a\b\c       a\b\c
     * a.b\c       a.b\c
     *
     * @param filename
     * @return filename==null时返回null
     */
    public static String removeExtension(final String filename) {
        String result = null;
        if (filename != null) {
            final int index = indexOfExtension(filename);
            if (index == NOT_FOUND) {
                result = filename;
            } else {
                result = filename.substring(0, index);
            }
        }
        return result;
    }
}
