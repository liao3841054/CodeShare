/**
 *****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : 2012-7-2
 *
 * Description : StringUtil.java
 *
 * Creation    : 2012-11-9
 * Author      : raorq@ucweb.com
 * History     : Creation, 2012-11-9, raorq, Create the file
 *****************************************************************************
 */
package com.uc.common.util.text;

import com.uc.common.util.lang.ArrayUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * 字符串的工具类
 *
 * @author raorq
 * @version 1.0
 */
public final class StringUtil {
    private static final int NOT_FOUND = -1;
    private static final String EMPTY_STRING = "";
    /**
     * 检查text是null, 或者空字符串(""), 与TextUtils.isEmpty等价
     * <pre>
     *     StringUtil.isEmpty(null)      = true
     *     StringUtil.isEmpty("")        = true
     *     StringUtil.isEmpty(" ")       = false
     *     StringUtil.isEmpty("bob")     = false
     *     StringUtil.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param text
     * @return
     */
    public static boolean isEmpty(final String text) {
        return text == null || text.length() == 0;
    }

    /**
     * 返回值与{@link #isEmpty(String)}相反
     *
     * @param text
     * @return
     */
    public static boolean isNotEmpty(final String text) {
        return !isEmpty(text);
    }

    /**
     * <p>检查text是null, 或空字符串(""), 或trim后是字符串</p>
     * <p>兼容性接口, 注意与 {@link #isEmpty(String)}不等价, 效率比它低, 按需使用, 尽量用{@link #isEmpty(String)}</p>
     * <pre>
     *     StringUtil.isEmpty(null)      = true
     *     StringUtil.isEmpty("")        = true
     *     StringUtil.isEmpty(" ")       = true
     *     StringUtil.isEmpty("bob")     = false
     *     StringUtil.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param text
     * @return
     */
    @Deprecated
    public static boolean isEmptyWithTrim(final String text) {
        if (isEmpty(text)) {
            return true;
        }
        final int length = text.length();
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) > ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回值与 {@link #isEmptyWithTrim(String)}相反
     *
     * @param text
     * @return
     */
    @Deprecated
    public static boolean isNotEmptyWithTrim(final String text) {
        return !isEmptyWithTrim(text);
    }

    /**
     * 
     * <p>判断StringBuffer是否为空</p>
     *
     * <b>修改历史</b>
     * <ol>
     * <li>创建（Added by leiap on 2012-7-19）</li>
     * </ol>
     * @param aStringBuffer
     * @return
     */
    public static boolean isEmpty(StringBuffer aStringBuffer){
        return aStringBuffer == null || aStringBuffer.length() == 0;
    }

    /**
     * 将任意个字符串合并成一个字符串
     * @param mText 字符串
     * @return
     */
    public static String merge(CharSequence... mText) {
        if (mText != null) {
            int length = mText.length;
            StringBuilder mStringBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (mText[i] != null && mText[i].length() > 0 && !mText[i].toString().equals("null")) {
                    mStringBuilder.append(mText[i]);
                }
            }
            return mStringBuilder.toString();
        }
        return null;
    }

    /**
     * 
     * //TODO luogw自动生成javadoc 需要后续其它同学补充注释
     * <p>请具体描述stringSplit方法提供的功能</p>
     *
     * <b>修改历史</b>
     * <ol>
     * <li>创建（Added by luogw on 2011-11-16）</li>
     * </ol>
     * @param text
     * @param separator
     * @return
     */
    public static String[] split(String text, String separator) {
        return split(text,separator,true);
    }

    /**
     * 
     * //TODO luogw自动生成javadoc 需要后续其它同学补充注释
     * <p>请具体描述split方法提供的功能</p>
     *
     * <b>修改历史</b>
     * <ol>
     * <li>创建（Added by luogw on 2011-11-16）</li>
     * </ol>
     * @param text
     * @param separator
     * @param withEmptyString
     * @return
     */
    public static String[] split(String text, String separator, boolean withEmptyString) {
        if (isEmpty(text)) {
            return new String[0];
        }
        
        if( separator == null || separator.length() == 0){
            return  new String[]{ text };
        }
        
        String[] sTarget = null;
        int sTargetLength = 0;
        int sLength = text.length();
        int sStartIndex = 0;
        int sEndIndex = 0;

        //扫描字符串，确定目标字符串数组的长度
        for (sEndIndex = text.indexOf(separator, 0); sEndIndex != -1 && sEndIndex < sLength;
                         sEndIndex = text.indexOf(separator, sEndIndex)) {
            sTargetLength += (withEmptyString || sStartIndex != sEndIndex) ? 1 : 0;
            sStartIndex = sEndIndex += sEndIndex >= 0 ? separator.length() : 0;
        }

        //如果最后一个标记的位置非字符串的结尾，则需要处理结束串
        sTargetLength += withEmptyString || sStartIndex != sLength ? 1 : 0;

        //重置变量值，根据标记拆分字符串
        sTarget = new String[sTargetLength];
        int sIndex = 0;
        for (sIndex = 0, sEndIndex = text.indexOf(separator, 0), sStartIndex = 0;
                sEndIndex != -1 && sEndIndex < sLength;
                sEndIndex = text.indexOf(separator, sEndIndex)) {
            if (withEmptyString || sStartIndex != sEndIndex) {
                sTarget[sIndex] = text.substring(sStartIndex, sEndIndex);
                ++sIndex;
            }
            sStartIndex = sEndIndex += sEndIndex >= 0 ? separator.length() : 0;
        }

        //取结束的子串
        if (withEmptyString || sStartIndex != sLength) {
            sTarget[sTargetLength - 1] = text.substring(sStartIndex); ;
        }

        return sTarget;
    }

    public static String[] splitAndTrim(String value, String expr) {
        value = value.replace(" ", "");
        return value.split(expr);
    }

    /**
     * 比较两个字符串（大小写敏感）。
     * 
     * <pre>
     * StringUtil.equals(null, null) = true
     * StringUtil.equals(null, "abc") = false
     * StringUtil.equals("abc", null) = false
     * StringUtil.equals("abc", "abc") = true
     * StringUtil.equals("abc", "ABC") = false
     * </pre>
     * 
     * @param str1 要比较的字符串1
     * @param str2 要比较的字符串2
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    /**
     * <p>字符串比较，不考虑大小写。如果两个字符串的长度相同，并且其中的相应字符都相等（忽略大小写），则认为这两个字符串是相等的</p>
     * <p>
     * <pre>
     * StringUtil.equalsIgnoreCase(null, null)   = true
     * StringUtil.equalsIgnoreCase(null, "abc")  = false
     * StringUtil.equalsIgnoreCase("abc", null)  = false
     * StringUtil.equalsIgnoreCase("abc", "abc") = true
     * StringUtil.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param str1 需要被对比的字符串
     * @param str2 需要对比的字符串
     * @return 相等（忽略大小写），则返回 true；否则返回 false
     */
    public static boolean equalsIgnoreCase(final String str1, final String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * <p>测试此字符串是否以指定的前缀开始,注:不区分大小写</p>
     *
     * @param str 测试的字符串
     * @param prefix 指定的前缀
     * @return 如果参数表示的字符序列是此字符串表示的字符序列的前缀，则返回 true；否则返回 false
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return str == null && prefix == null;
        }
        if (prefix.length() > str.length()) {// 不用找了, 肯定不是
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static int indexOfIgnoreCase(String str, String searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * <p>返回指定子字符串在此字符串中第一次出现处的索引，从指定的索引开始</p>
     *
     * @param str 计算的字符串
     * @param searchStr         指定的子字符串
     * @param startIndex   指定的索引
     * @return 如果字符串参数作为一个子字符串在此对象中出现一次或多次, 则返回最后一个这种子字符串的第一个字符.如果它不作为一个子字符串出现, 则返回 -1
     */
    public static int indexOfIgnoreCase(String str, String searchStr, int startIndex) {
        if (str == null || searchStr == null) {
            return NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        final int endLimit = str.length() - searchStr.length() + 1;
        if (startIndex > endLimit) {// 没必要找, 肯定找不到
            return NOT_FOUND;
        }
        if (searchStr.length() == 0) {// searchStr为空字符串就直接找到了
            return startIndex;
        }
        for (int i = startIndex; i < endLimit; i++) {
            if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    /**
     * 以sepertor链接字符串
     * 
     * 
     * @param list
     * @param seperator
     * @return
     */
    public static String join(List<String> list, String seperator) {
        if (null == seperator) {
            return "";
        }
        if (null == list || list.isEmpty()) {
            return "";
        }
        
        int listSize = list.size();
        
        StringBuilder sb = new StringBuilder();
        if (listSize > 0) {
            sb.append(list.get(0));
            
            for (int i = 1; i < listSize; i++){
                sb.append(seperator);
                sb.append(list.get(i));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * @see {@link #join(List, String)}}
     * 
     * @param str
     * @param seperator
     * @return
     */
    public static List<String> unJoin(String str, String seperator) {
        if (isEmpty(str) || null == seperator) {
            return new ArrayList<String>();
        }
        
        String[] result = str.split(seperator);
        if (null == result) {
            return new ArrayList<String>();
        }
        
        ArrayList<String> list = new ArrayList<String>(result.length);
        for(String item : result) {
            list.add(item);
        }
        return list;
    }
    
    /**
     * 替换字符串中的 & nbsp 空格为半角空格
     *
     */
    public static String replaceNBSPSpace(String str) {
        if (str == null || str.length() == 0)
            return str;
        return str.replace('\u00A0', '\u0020');
    }

    public static boolean parseBoolean(String value, boolean aDefault){
        if(value == null) {
            return aDefault;
        }
        if("1".equals(value) || "true".equalsIgnoreCase(value)){
            return true;
        }
        
        return false;
    }

    public static boolean parseBoolean(String value){
        return parseBoolean(value, false);
    }

    public static byte[] getBytesUTF8(String value) {
        if (value == null) {
            return null;
        }
        byte[] data;
        try {
            data = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // impossible on android
            data = value.getBytes();
        }
        return data;
    }

    public static String newStringUTF8(byte[] bytes) {
        if (ArrayUtil.isEmpty(bytes)) {
            return "";
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes); // 理论上不会走到这里
        }
    }

    public static String newStringUTF8(byte[] bytes, int offset, int length) {
        if (ArrayUtil.isEmpty(bytes)) {
            return "";
        }
        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes, offset, length); // 理论上不会走到这里
        }
    }

    /**
     * 
     * <p>使用给定的 replacement 替换此字符串所有匹配给定的匹配的子字符串</p>
     *
     * <b>修改历史</b>
     * <ol>
     * <li>创建（Added by luogw on 2011-11-16）</li>
     * </ol>
     * @param srcString 源字符串
     * @param matchingString 匹配的字符串
     * @param replacement 替换的字符串
     * @param supportReplacementEmpty 替换的字符串是否可以为""或者null, null会转换成""
     * @return 返回替换操作后的字符串
     */
    public static String replaceAll(String srcString, String matchingString, String replacement, boolean supportReplacementEmpty) {
        boolean flag = isEmpty(replacement);
        if(supportReplacementEmpty){
            if(replacement == null){
                replacement = "";
            }
            flag = false;
        }
        if (isEmpty(srcString) || isEmpty(matchingString) || flag) {
            return null;
        }

        StringBuffer sResult = new StringBuffer();
        int sIndex = 0;
        int sMaxIndex = srcString.length() - 1;
        while ((sIndex = srcString.indexOf(matchingString)) != -1) {
            String sPreStr = srcString.substring(0, sIndex);
            sResult.append(sPreStr).append(replacement);
            srcString = (sIndex < sMaxIndex) ? srcString.substring(sIndex + matchingString.length()) : "";
        }
        sResult.append(srcString);

        return sResult.toString();
    }

    public static String replaceAll(String srcString, String matchingString, String replacement) {
        return replaceAll(srcString, matchingString, replacement, false);
    }
    
    /**
     * @return 是否包含中文
     */
    public static boolean containsChinese(String str) {
    	try{
    		Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
    		Matcher m = p.matcher(str);
    		if (m.find()) {
    			return true;
    		}
    	}catch(Exception e){
//    		ExceptionHandler.processFatalException(e);
    	}
    	return false;
    }

    /**
     * 传进一个字符串，如果字符串是null的，返回空字符串，不null就返回本身
     * @param aText
     * @return
     */
    public static String emptyIfNull(String aText) {
        if (aText == null) {
            return "";
        } else {
            return aText;
        }
    }

    /**
     * 如果对象是null的，返回空字符串,否则返回.toString()
     */
    public static String ensureObject2StringNotNull(Object object) {
        if (object == null) {
            return "";
        } else {
            return object.toString();
        }
    }

    /**
     * 去除字符串两端的空格
     * @param originalString 原始字符串
     * @return 返回去除两端的空格后的字符串
     */
    public static String trim(String originalString) {
        if (originalString == null) {
            return null;
        }
        return originalString.trim();
    }

    /**
     * <p>获取字符串的前缀，超过最大长度则截断</p>
     *
     * @param str  原始字符串
     * @param len 获取前缀的最大长度
     * @return 字符串前缀
     */
    public static String left(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY_STRING;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    public static String right(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY_STRING;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }

    public static String removeStartIgnoreCase(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (startsWithIgnoreCase(str, remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    /**
     *
     * @param str
     * @param start 负数等同于传0
     * @return
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY_STRING;
        }

        return str.substring(start);
    }

    /**
     * 判断是否由ASCII码组成的字符串
     *
     * @param text
     * @return
     */
    public static boolean isMadeUpOfAscii(String text) {
        if (isEmpty(text)) {
            return false;
        }
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c > 127) {
                return false;
            }
        }
        return true;
    }

    /**
     * get the string between string A and string B
     * @param str
     * @param beginStr
     * @param endStr
     * @return String, maybe null
     */
    public static String stringBetween(String str, String beginStr, String endStr ){
        if (str == null || beginStr == null || endStr == null) {
            return null;
        }
        final int start = str.indexOf(beginStr);
        if (start != NOT_FOUND) {
            final int end = str.indexOf(endStr, start + beginStr.length());
            if (end != NOT_FOUND) {
                return str.substring(start + beginStr.length(), end);
            }
        }
        return null;
    }

    public static String toUppercase(String input) {
        String ret = "";
        if (isNotEmpty(input)) {
            ret = input.toUpperCase(Locale.getDefault());
        }
        return ret;
    }

    /**
     * 判断一个字符是否为ascii码
     *
     * @param c
     * @return
     */
    public static boolean isAscii(char c) {
        return c <= '\u007f';
    }

    public static boolean detectGb2312(byte[] data) {
        if (null == data || 0 == data.length) {
            return false;
        }

        int indexStart = 0;
        int indexEnd = data.length - 1;

        while (true) {
            int srcCharHigh = 0xFF & data[indexStart];
            int srcCharLow;

            if (srcCharHigh < 0x80) { // 0x00~0x7F ASCII码
                if (indexStart >= indexEnd) {
                    break;
                }

                indexStart++;
            } else if (srcCharHigh < 0xA1) { // 0x80~0xA0 空码
                return false;
            } else if (srcCharHigh < 0xAA) { // 0xA1~0xA9 字符部分
                if (indexStart >= indexEnd) {
                    return false;
                }

                srcCharLow = 0xFF & data[indexStart + 1];

                // 低字节范围 0xA1~0xFE
                if (srcCharLow < 0xA1 || srcCharLow > 0xFE) {
                    return false;
                }

                if (indexStart >= indexEnd - 1) {
                    break;
                }

                indexStart += 2;
            } else if (srcCharHigh < 0xB0) { // 0xAA~0xAF 空码
                return false;
            } else if (srcCharHigh < 0xF8) { // 0xB0~0xF7 GB2312常见汉字表
                if (indexStart >= indexEnd) {
                    return false;
                }

                srcCharLow = 0xFF & data[indexStart + 1];

                // 低字节范围 0xA1~0xFE
                if (srcCharLow < 0xA1 || srcCharLow > 0xFE) {
                    return false;
                }

                if (indexStart >= indexEnd - 1) {
                    break;
                }

                indexStart += 2;
            } else { // 0xF8~0xFF 空码
                return false;
            }
        }

        return true;
    }

    // copy from TextUtils.cpp's detectUTFImpl()
    public static boolean detectUtf8(byte[] data) {
        if (null == data || 0 == data.length) {
            return false;
        }

        int checkLen = 0;
        int seqLen = 0;
        int index = 0;
        int oldIndex = 0;
        int checkChar = 0;
        int srcChar = 0;

        while (true) {
            srcChar = 0xFF & data[index];

            if ((srcChar & 0x80) == 0) {
                seqLen = 1;
            } else if ((srcChar & 0xC0) != 0xC0) {
                seqLen = 0;
            } else if ((srcChar & 0xE0) == 0xC0) {
                seqLen = 2;
            } else if ((srcChar & 0xF0) == 0xE0) {
                seqLen = 3;
            } else if ((srcChar & 0xF8) == 0xF0) {
                seqLen = 4;
            } else if ((srcChar & 0xFC) == 0xF8) {
                seqLen = 5;
            } else if ((srcChar & 0xFE) == 0xFC) {
                seqLen = 6;
            }

            if (0 == seqLen) {
                return false;
            }

            checkLen = seqLen;
            oldIndex = index;
            checkChar = 0;

            // 检查UTF格式
            index += seqLen;

            if (index > data.length) {
                return false;
            }

            // 6字节
            if (checkLen == 6) {
                checkChar = 0xFF & data[oldIndex + 5];
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false;
                }

                checkLen--;
            }

            // 5字节
            if (checkLen == 5) {
                checkChar = 0xFF & data[oldIndex + 4];
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false;
                }

                checkLen--;
            }

            // 4字节
            if (checkLen == 4) {
                checkChar = 0xFF & data[oldIndex + 3];
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false;
                }

                checkLen--;
            }

            // 3字节
            if (checkLen == 3) {
                checkChar = 0xFF & data[oldIndex + 2];
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false;
                }

                checkLen--;
            }

            // 2字节
            if (checkLen == 2) {
                checkChar = 0xFF & data[oldIndex + 1];
                if (checkChar > 0xBF) {
                    return false;
                }

                switch (srcChar) {
                    // // no fall-through in this inner switch
                    // case 0xE0: if (checkChar < 0xA0) return false;
                    // case 0xED: if (checkChar > 0x9F) return false;
                    // case 0xF0: if (checkChar < 0x90) return false;
                    // case 0xF4: if (checkChar > 0x8F) return false;
                    default:
                        if (checkChar < 0x80)
                            return false;
                }

                checkLen--;
            }

            // 1字节
            if (checkLen == 1) {
                if (srcChar >= 0x80 && srcChar < 0xC2) {
                    return false;
                }
            }

            // if (srcChar > 0xF4)
            // return false;

            if (index == data.length)
                return true;
        }
    }

    /**
     * Check a string is chinese character.
     *
     * @param chineseStr
     *            Checking string.
     * @return If string is chinese character return true, else return false.
     */
    public static boolean isChineseCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            // 是否是Unicode编码,除了"?"这个字符.这个字符要另外处理
            if ((charArray[i] >= '\u0000' && charArray[i] < '\uFFFD')
                    || ((charArray[i] > '\uFFFD' && charArray[i] < '\uFFFF'))) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean startsWith(String text, String prefix) {
        if (text == null || prefix == null) {
            return text == null && prefix == null;
        }
        if (prefix.length() > text.length()) {
            return false;
        }
        return text.startsWith(prefix);
    }
}
