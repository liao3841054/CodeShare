/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uc.common.util.net;

import android.net.Uri;
import android.text.TextUtils;

import com.uc.common.util.lang.NumberUtil;
import com.uc.common.util.text.StringUtil;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URLUtil {
    private static final String ASSET_BASE = "file:///android_asset/";
    private static final String FILE_BASE = "file://";
    private static final String PROXY_BASE = "file:///cookieless_proxy/";

    private static final String LOCAL_FILE_URL_PREFIX = "file:///data/data/";

    private static final String PATTERN_END = "(:\\d{1,5})?" + // maybe port
            "(/|\\?|$)"; // query, path or nothing

    private static final Pattern URL_WITH_PROTOCOL_PATTERN = Pattern.compile("[a-zA-Z0-9]{2,}://" + // protocol
            "[a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+)*" + // host name elements
            PATTERN_END);
    private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern
            .compile("([a-zA-Z0-9\\-]+\\.)+[a-zA-Z0-9\\-]{2,}" + // host name elements
                    PATTERN_END);

    /**
     * @return True iff the url is a local file.
     */
    public static boolean isFileUrl(String url) {
        return (!StringUtil.isEmptyWithTrim(url))
                && (url.startsWith(FILE_BASE) && !url.startsWith(ASSET_BASE) && !url.startsWith(PROXY_BASE));
    }

    /**
     * @return True iff the url is an http: url.
     */
    public static boolean isHttpUrl(String url) {
        return (!StringUtil.isEmptyWithTrim(url)) && (url.length() > 6) && url.substring(0, 7).equalsIgnoreCase("http://");
    }

    /**
     * @return True iff the url is an https: url.
     */
    public static boolean isHttpsUrl(String url) {
        return (!StringUtil.isEmptyWithTrim(url)) && (url.length() > 7) && url.substring(0, 8).equalsIgnoreCase("https://");
    }

    /**
     * @return True iff the url is a network url.
     */
    public static boolean isNetworkUrl(String url) {
        if (StringUtil.isEmptyWithTrim(url)) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }

    /**
     * Strips the url of the anchor.
     */
    public static String stripAnchor(String url) {
        int anchorIndex = url.indexOf('#');
        if (anchorIndex != -1) {
            return url.substring(0, anchorIndex);
        }
        return url;
    }

    /**
     * 从url中抓出host来，支持http,https,ftp等系统url支持的
     * url如果为null或者空串或者利用系统去取host时出异常，则返回""
     */
    public static String getHostFromUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return "";
        }
        //url中如果没有schema,则在前面加个http:// 避免如果不带schema返回为空
        int index = url.indexOf("://");
        if (index < 0) {
            url = "http://" + url;
        }
        try {
            URL uri = new URL(url);
            return uri.getHost();
        } catch (Throwable e) {
            // ExceptionHandler.processFatalException(e);
            return "";
        }
    }

    public static int getPortFromHttpAndHttpsUrl(String url) {
        if (null == url || url.length() == 0) {
            return -1;
        }

        int port = 80;
        int nextSearchStart = 0;
        String protocol = "http";
        int index1 = url.indexOf("://");
        if (index1 > 0) {
            protocol = url.substring(0, index1);
            nextSearchStart = index1 + 3;
        }

        if ("https".equalsIgnoreCase(protocol)) {
            port = 443;
        } else if (!"http".equalsIgnoreCase(protocol)) {
            return -1;
        }

        int index2 = url.indexOf(":", nextSearchStart);
        if (index2 > 0) {
            int index3 = url.indexOf('/', index2+1);
            if (index3 < 0) {
                port = NumberUtil.toInt(url.substring(index2+1), port);
            } else {
                port = NumberUtil.toInt(url.substring(index2+1, index3), port);
            }
        }
        return port;
    }

    /**
     * get schema
     * @param url
     * @return
     */
    public static String getSchemaFromUrl(final String url){
        if (null == url || url.length() == 0){
            return "";
        }
        int index = url.indexOf("://");
        if(index > 0){
            return url.substring(0, index);
        }        
        return "";        
    }

    /**
     * 从url中抓出host来，支持http,https,ftp等系统url支持的
     * url如果为null或者空串或者利用系统去取host时出异常，则返回""
     */
    public static String getRootHostFromUrl(final String url) {
        String host = getHostFromUrl(url);
        if (StringUtil.isNotEmptyWithTrim(host)) {
            String[] hostItem = host.split("\\.");
            if (hostItem != null && hostItem.length >= 2) {
                return StringUtil.merge(hostItem[hostItem.length - 2], ".", hostItem[hostItem.length - 1]);
            }
        }

        return host;
    }

    /**
     * 涉及jni　proguard 需要keep住名称<br/>
     * 从url中尝试获取下载任务文件名
     * @param sUrl
     * @return name,　取不到就用"index.html"
     */
    public static String getFileNameFromUrl(String sUrl) {
        String inputUrl = cutOffParams(sUrl);
        String result = URLUtil.getFileNameFromUrl(inputUrl, true);
        if (DEFAULT_NAME.equals(result)) {// 如果decode取不到正确的name,不decode试试
            return URLUtil.getFileNameFromUrl(inputUrl, false);
        }
        return result;
    }

    private static final String DEFAULT_NAME = "index.html";

    private static String cutOffParams(String url) {

        if (null == url || url.length() == 0) {
            return null;
        }

        int index = url.indexOf('?');
        if (index < 0) {
            return url;
        }

        if (index == 0) {
            return null;
        }

        return url.substring(0, index);
    }

    public static String getFileNameFromUrl(String inputUrl, boolean decode) {
        if (inputUrl != null) {
            inputUrl = inputUrl.trim();
            if (decode) {
                try {
                    inputUrl = URLDecoder.decode(inputUrl);
                } catch (Exception e) {
//                    ExceptionHandler.processFatalException(e);
                }
            }
            int length = inputUrl.length();
            if (length > 0) {
                int endIndex = inputUrl.lastIndexOf('/');
                if (endIndex != -1 && endIndex < length - 1) {
                    int startIndex = inputUrl.lastIndexOf('/', endIndex - 1);
                    if (startIndex != -1 && endIndex - startIndex > 0) {
                        // 根据8.7的表现：至少在最后/与/之前要有一个字符
                        String result = new String(inputUrl.substring(endIndex + 1));
                        startIndex = result.indexOf('?');
                        if (startIndex == -1) {
                            return result;
                        } else {// 裁出/到?之前的字符串
                            if (startIndex < length - 1) {
                                result = new String(result.substring(0, startIndex).trim());
                                if (result.length() > 0) {
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
        }

        return DEFAULT_NAME;
    }

    /**
     *
     * @param host 注意传的是host不是带scheme的url
     * @return
     */
    public static boolean isWapSite(String host) {
        if (null == host || 0 == host.length())
            return false;

        String tmpHost = host.toLowerCase(Locale.ENGLISH);

        String[] wapPrefix = { "wap.", "3g", "m.", "mobi." };
        for (int i = 0; i < wapPrefix.length; i++) {
            int pos = tmpHost.indexOf(wapPrefix[i]);
            if (pos == 0) {
                return true;
            } else if (pos > 0) {
                if (".".equals(tmpHost.substring(pos - 1, pos)))
                    return true;
            }
        }
        return false;

    }

    /**
     * 根据url地址get参数的key获取对应的value，如果没有的话就返回null
     * @param url
     * @param key
     * @return
     */
    public static String getParamFromUrl(String url, String key) {
        if (url != null) {
            url = url.substring(url.indexOf('?') + 1);
            String paramaters[] = url.split("&");
            for (String param : paramaters) {
                String values[] = param.split("=");
                if (values.length == 2) {
                    if (key.equalsIgnoreCase(values[0])) {
                        return values[1];
                    }
                }
            }
        }
        return null;
    }


    /**
     * 根据url地址get参数的key,移除相应的key=value
     * @param url
     * @param key
     * @return
     */
    public static String removeParamFromUrl(String url, String key) {
        if (url != null) {
            String value = getParamFromUrl(url, key);
            if (value != null) {
                String param = key + "=" + value;
                url = url.replace("&" + param , "");
                url = url.replace("?" + param , "?");
            }
        }
        return url;
    }

    /**
     * 根据url地址参数的key，替换相应key的value
     * 如果没有相应key，则什么都不做
     * @param url
     * @param key
     * @param value
     * @return
     */
    public static String replaceParamFromUrl(String url ,String key , String value) {
        String result = url;
        if (hasParam(url , key)) {
            result = removeParamFromUrl(url , key);
            result = addParamsToUrl(result , key , value);
        }
        return result;
    }

    /**
     * url中是否有key对应的param
     * @param url
     * @param key
     * @return
     */
    public static boolean hasParam(String url, String key) {
        if (StringUtil.isNotEmpty(url)) {
            url = url.substring(url.indexOf('?') + 1);
            String paramaters[] = url.split("&");
            for (String param : paramaters) {
                String values[] = param.split("=");
                if (values.length > 0 && key.equalsIgnoreCase(values[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 清理网址中存在的重复"."
     * 
     * @param input
     * @return
     */
    public static String cleanRepeatDot(String input) {
        StringBuilder output = new StringBuilder(input);
        char dot = '.';

        int currIndex = output.indexOf(String.valueOf(dot));
        int length = output.length();
        while (currIndex >= 0 && currIndex < length - 1) {
            if (currIndex >= 0 && output.charAt(currIndex) == dot
                    && output.charAt(currIndex) == output.charAt(currIndex + 1)) {
                output.deleteCharAt(currIndex);
            } else {
                currIndex += 1;
            }

            currIndex = output.indexOf(String.valueOf(dot), currIndex);
            length = output.length();
        }

        return output.toString();
    }

    /**
     * 往Url里面添加参数，注意 1. value如果需要encode，由使用者外部encode之后再传入， 2.此函数不做key的重复性检查
     * @param url
     * @param key
     * @param value
     * @return 新组装后的url
     */
    public static String addParamsToUrl(String url, String key, String value) {
        if (StringUtil.isEmptyWithTrim(url) || StringUtil.isEmptyWithTrim(key)) {
            return url;
        }
        //添加对于#(锚点)的判断
        String newUrl;
        String tempUrl = url;
        String hashUrl = null;
        boolean hashFlag = false;
        int hashPos = url.indexOf("#");
        if(hashPos > 0){
            hashFlag = true;
            tempUrl = url.substring(0, hashPos);
            hashUrl = url.substring(hashPos);
        }

        int pos = tempUrl.indexOf("?");
        if (pos < 0) {
            newUrl = tempUrl + "?" + key + "=" + (null != value ? value : "");
        } else {
            newUrl = tempUrl + "&" + key + "=" + (null != value ? value : "");
        }

        if (hashFlag) {
            newUrl += hashUrl;
        }

        return newUrl;
    }
    
    public static boolean isContainQueMark(String url){
        if(StringUtil.isEmptyWithTrim(url)){
            return false;
        }
        int pos = url.indexOf("?");
        if (pos < 0) {
            return false;
        }
        return true;
    }

    public static boolean isValideIP4Address(String str) {
        if (str == null)
            return false;
        str = str.trim();
        if (str.length() == 0)
            return false;

        boolean isIP4Address = false;
        try {
            if (str.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                String s[] = str.split("\\.");
                if (Integer.parseInt(s[0]) < 256)
                    if (Integer.parseInt(s[1]) < 256)
                        if (Integer.parseInt(s[2]) < 256)
                            if (Integer.parseInt(s[3]) < 256)
                                isIP4Address = true;
            }
        } catch (Throwable t) {
//            ExceptionHandler.processFatalException(t);
        }

        return isIP4Address;
    }

    public static boolean isBasicallyValidURI(CharSequence uri) {
        if (null == uri) {
            return false;
        }
        Matcher m = URL_WITH_PROTOCOL_PATTERN.matcher(uri);
        if (m.find() && m.start() == 0) { // match at start only
            return true;
        }
        m = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
        return m.find() && m.start() == 0;
    }

    public static boolean isLocalFileURI(String uri) {
        if (uri != null && uri.startsWith(LOCAL_FILE_URL_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * 去除url中重复“.”和空格，如果url为null，返回""
     */
    public static String cleanRepeatDotAndTrim(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return URLUtil.cleanRepeatDot(url).trim();
    }

    /**
     * 获取fragment
     * @param url
     * @return
     */
    public static String getFragmentFromUrl(final String url) {
        if (null == url || url.length() == 0) {
            return "";
        }
        int index = url.indexOf("#");
        if (index > 0) {
            return url.substring(index + 1, url.length());
        }
        return "";
    }

    public static String getSecondLevelDomain(String url) {
        if( TextUtils.isEmpty(url)) {
            return url;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if( TextUtils.isEmpty(host)) {
            return url;
        }
        String[] domains = TextUtils.split(host, "\\.");
        if( (null == domains) || ( domains.length == 0 )){
            return url;
        }

        int len = domains.length;
        String secondLevelDomain = null;
        final int SECOND_LEVEL_DOMAIN_LEN = 3;
        if( len > SECOND_LEVEL_DOMAIN_LEN) {
            for( int i = 0; i < SECOND_LEVEL_DOMAIN_LEN ; i ++) {
                String tmp = domains[len -i - 1];
                if( secondLevelDomain != null ) {
                    secondLevelDomain = tmp + "." +  secondLevelDomain;
                } else {
                    secondLevelDomain = tmp;
                }
            }
            return secondLevelDomain;
        } else {
            return host;
        }
    }

    public static String removeHttpsAndHttpPrefix(String url) {
        if (StringUtil.isEmptyWithTrim(url)) {
            return url;
        }
        
        if (url.startsWith("https://")) {
            url = url.substring("https://".length());
        } else if (url.startsWith("http://")) {
            url = url.substring("http://".length());
        }
        
        return url;
    }

    public static boolean isMarketURL(String aUrl){
        return StringUtil.isNotEmptyWithTrim(aUrl) && aUrl.toLowerCase().startsWith("market://");
    }
}
