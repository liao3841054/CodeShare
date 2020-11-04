package com.uc.common.util.lang;

import java.util.Locale;

/**
 * Created by wx107452@alibaba-inc.com on 2017/4/26.
 */

public class LocaleUtil {
    public static String getSystemCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * @return Returns the language code for this Locale or the empty string if no language was set
     * China is represented by zh
     * English is represented by en
     * French is represented by fr
     * German is represented by de
     * Italian is represented by it
     * Japanese is represented by ja
     * Russian is represented by ru
     * Spanish is represented by es
     * Swedish is represented by sv
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

}
