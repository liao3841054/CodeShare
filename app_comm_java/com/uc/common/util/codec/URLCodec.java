package com.uc.common.util.codec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/5.
 */

public class URLCodec {
    private static final String UTF_8 = "UTF-8";

    public static String encode(String data) {
        String retData;
        try {
            retData = URLEncoder.encode(data, UTF_8);
        } catch (UnsupportedEncodingException e) {
            retData = URLEncoder.encode(data);
        }
        return retData;
    }

    public static String decode(String data) {
        String retData;
        try {
            retData = URLDecoder.decode(data, UTF_8);
        } catch (UnsupportedEncodingException e) {
            retData = URLEncoder.encode(data);
        }
        return retData;
    }
}
