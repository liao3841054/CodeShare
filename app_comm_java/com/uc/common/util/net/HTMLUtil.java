package com.uc.common.util.net;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/24.
 */

public class HTMLUtil {

    /**
     * 从XHTML2中取出body的内容
     * @param xhtml2
     * @return body的内容，不包含<body></body>标签
     */
    public static String getBodyContent(String xhtml2) {
        String bodyContent = null;
        //对传入的参数做判空处理
        if (xhtml2 != null) {
            String bodyStartTag = "<body>";
            String bodyEndTag = "</body>";

            int pos1 = xhtml2.indexOf(bodyStartTag);
            int pos2 = xhtml2.indexOf(bodyEndTag);

            if (pos1 > 0 && pos2 > 0 && pos2 > pos1) {
                bodyContent = new String(xhtml2.substring(pos1 + bodyStartTag.length(), pos2).trim());
            } else if (pos1 > 0 && pos2 == -1) {
                bodyContent = new String(xhtml2.substring(pos1 + bodyStartTag.length()).trim());
            } else if (pos1 == -1 && pos2 > 0) {
                bodyContent = new String(xhtml2.substring(0, pos2).trim());
            }
        }
        return bodyContent;
    }

}
