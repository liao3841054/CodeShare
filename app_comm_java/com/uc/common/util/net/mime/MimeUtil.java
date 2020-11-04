/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : MimeUtil.java
 * <p>
 * Description :
 * <p>
 * Creation    : 2012.9.20
 * Author      : caisq@ucweb.com
 * History     : Creation, 2012.9.20, Cai Shanqing, Create the file
 * ****************************************************************************
 **/

package com.uc.common.util.net.mime;

import com.uc.common.util.text.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MimeUtil {

    private static final MimeUtil mInstance = new MimeUtil();

    private HashMap<String, String> mMimeTypeToExtension = new HashMap<String, String>(512);
    private HashMap<String, String> mExtensionToMimeType = new HashMap<String, String>(512);

    public final static String MIMETYPE_UCT = "resource/uct";
    public final static String MIMETYPE_UCW = "resource/ucw";
    public final static String MIMETYPE_UPP = "resource/upp";
    public final static String MIMETYPE_LANG = "resource/ucl";
    public final static String MIMETYPE_UCS = "video/ucs";
    public final static String MIMETYPE_APK = "application/vnd.android.package-archive";
    public final static String MIMETYPE_MPEG = "video/mpeg";
    public final static String MIMETYPE_MP4 = "video/mp4";
    public final static String MIMETYPE_QUICKTIME = "video/quicktime";
    public final static String MIMETYPE_ASF = "video/x-ms-asf";
    public final static String MIMETYPE_WMV = "video/x-ms-wmv";
    public final static String MIMETYPE_AVI = "video/x-msvideo";
    public final static String MIMETYPE_3GP = "video/3gpp";
    public final static String MIMETYPE_MOIVE = "video/x-sgi-movie";
    public final static String MIMETYPE_TEXT = "text/plain";
    public final static String MIMETYPE_AUDIO = "audio/mpeg";
    public final static String MIMETYPE_WMA = "audio/x-ms-wma";
    public final static String MIMETYPE_REALAUDIO = "audio/x-pn-realaudio";
    public final static String MIMETYPE_WAV = "audio/x-wav";
    public final static String MIMETYPE_MIDI = "audio/midi";
    public final static String MIMETYPE_M3U8 = "application/vnd.apple.mpegurl";
    public final static String MIMETYPE_M3U82 = "application/x-mpegurl";
    public final static String MIMETYPE_FLV = "video/x-flv";
    public final static String MIMETYPE_RMVB = "video/vnd.rn-realvideo";
    public final static String MIMETYPE_3GP2 = "video/3gpp2";
    public final static String MIMETYPE_M4V = "video/x-m4v";
    public final static String MIMETYPE_H264 = "video/h264";
    public final static String MIMETYPE_H263 = "video/h263";
    public final static String MIMETYPE_TS = "video/MP2T";
    public final static String MIMETYPE_MKV = "video/x-matroska";
    public final static String MIMETYPE_XVID = "video/x-xvid";
    public final static String MIMETYPE_VP6 = "video/x-vp6";
    public final static String MIMETYPE_TP = "video/tp";
    public final static String MIMETYPE_F4V = "video/x-f4v";

    public final static String MIMETYPE_DOC = "application/msword";
    public final static String MIMETYPE_PPT = "application/vnd.ms-powerpoint";
    public final static String MIMETYPE_XLS = "application/vnd.ms-excel";
    public final static String MIMETYPE_PDF = "application/pdf";
    public final static String MIMETYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public final static String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public final static String MIMETYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    private final static List<String> SYSTEM_NORMAL_SUPPORTED_VIDEO_TYPE = new ArrayList<String>(
            Arrays.asList(MIMETYPE_MPEG, MIMETYPE_MP4, MIMETYPE_QUICKTIME,
                    MIMETYPE_ASF, MIMETYPE_WMV, MIMETYPE_AVI, MIMETYPE_3GP,
                    MIMETYPE_MOIVE));

    public static MimeUtil getInstance() {
        return mInstance;
    }

    public String getMimeTypeFromUrl(String string) {
        String mimeType = "";
        String extension = getFileExtensionFromUrl(string);
        mimeType = getMimeTypeFromExtension(extension);

        return mimeType == null ? "" : mimeType;
    }

    public static String getFileExtensionFromUrl(String url) {
        if (url != null && url.length() > 0) {
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            if (filename.length() > 0) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }

    public static String getFileExtensionFromFileName(String fileName) {
        if (fileName != null && fileName.length() > 0) {
            int dotPos = fileName.lastIndexOf('.');
            if (0 <= dotPos) {
                return fileName.substring(dotPos + 1);
            }
        }

        return "";
    }

    /**
     * 是否为音频格� （cda，WAV，AIFF，AU，MPEG，MP3，MIDI，WMA，RealAudio，VQF，OggVorbis，AMR�     * @param mimetype
     *
     * @return
     */
    public static boolean isAudioType(String mimetype) {
        if (!StringUtil.isEmptyWithTrim(mimetype)) {
            if (MimeUtil.MIMETYPE_WMA.equalsIgnoreCase(mimetype)
                    || MimeUtil.MIMETYPE_REALAUDIO.equalsIgnoreCase(mimetype)
                    || MimeUtil.MIMETYPE_WAV.equalsIgnoreCase(mimetype)
                    || MimeUtil.MIMETYPE_MIDI.equalsIgnoreCase(mimetype)
                    || MimeUtil.MIMETYPE_AUDIO.equalsIgnoreCase(mimetype)) {
                return true;
            }
        }

        return false;
    }


    public static boolean isOfficeFileType(String mimetype) {
        if (!StringUtil.isEmpty(mimetype)) {
            if (MIMETYPE_DOC.equalsIgnoreCase(mimetype)
                    || MIMETYPE_PPT.equalsIgnoreCase(mimetype)
                    || MIMETYPE_PPTX.equalsIgnoreCase(mimetype)
                    || MIMETYPE_XLS.equalsIgnoreCase(mimetype)
                    || MIMETYPE_XLSX.equalsIgnoreCase(mimetype)
                    || MIMETYPE_DOCX.equalsIgnoreCase(mimetype)
                    || MIMETYPE_PDF.equalsIgnoreCase(mimetype)) {
                return true;
            }
        }
        return false;

    }

    public static boolean isVideoType(String mimetype, String url) {
        if (!StringUtil.isEmptyWithTrim(mimetype) && mimetype.toLowerCase().contains("video/")) {
            return true;
        }

        if (!StringUtil.isEmptyWithTrim(url) && isVideoPath(url)) {
            return true;
        }

        return false;
    }


    public static boolean isImageType(String mimetype){
        if (!StringUtil.isEmptyWithTrim(mimetype) && mimetype.toLowerCase().contains("image/")) {
            return true;
        }
        return false;
    }

    /**
     * 由于android不同的机型支持的格式不一，没有统一的标准，这里列出系统一般支持较好的格式
     *
     * @param mimetype
     * @return
     */
    public static boolean isSystemNormalSupportedVideoType(String mimetype) {
        return (!StringUtil.isEmptyWithTrim(mimetype) && SYSTEM_NORMAL_SUPPORTED_VIDEO_TYPE.contains(mimetype));
    }

    public String getMimeTypeFromExtension(String extension) {
        String mimeType = "";
        if (extension != null && extension.length() > 0) {
            mimeType = mExtensionToMimeType.get(extension.toLowerCase(Locale.ENGLISH));
        }

        return mimeType == null ? "" : mimeType;
    }

    public HashSet<String> getExtensionFromMimeType(String mimeType) {
        HashSet<String> extension = new HashSet<String>();
        if (mimeType != null && mimeType.length() > 0) {
            Iterator<Map.Entry<String, String>> iter = mExtensionToMimeType.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                String key = entry.getKey();
                String val = entry.getValue();
                if (mimeType.equalsIgnoreCase(val)) {
                    extension.add(key);
                }
            }
        }

        return extension;
    }

    private void loadEntry(String mimeType, String extension) {
        if (!mMimeTypeToExtension.containsKey(mimeType)) {
            mMimeTypeToExtension.put(mimeType, extension);
        }
        mExtensionToMimeType.put(extension, mimeType);
    }

    public static boolean isMp3File(final String fileName) {
        if (StringUtil.isEmptyWithTrim(fileName)) {
            return false;
        }

        String extension = MimeUtil.getFileExtensionFromUrl(fileName);
        if (StringUtil.isNotEmptyWithTrim(extension) && extension.equalsIgnoreCase("mp3")) {
            return true;
        }
        return false;
    }

    public static boolean isApkFile(final String fileName) {
        if (StringUtil.isEmptyWithTrim(fileName)) {
            return false;
        }

        String extension = MimeUtil.getFileExtensionFromUrl(fileName);
        if (StringUtil.isNotEmptyWithTrim(extension) && extension.equalsIgnoreCase("apk")) {
            return true;
        }
        return false;
    }

    public static boolean isPdfFile(final String fileName) {
        if (StringUtil.isEmptyWithTrim(fileName)) {
            return false;
        }

        String extension = MimeUtil.getFileExtensionFromUrl(fileName);
        if (StringUtil.isNotEmptyWithTrim(extension) && extension.equalsIgnoreCase("pdf")) {
            return true;
        }
        return false;
    }

    public static boolean isAPKMimeType(final String mimeType) {
        return MIMETYPE_APK.equalsIgnoreCase(mimeType);
    }

    private MimeUtil() {
        loadEntry(MIMETYPE_UCS, "ucs");
        loadEntry(MIMETYPE_UCT, "uct");
        loadEntry(MIMETYPE_UCW, "ucw");
        loadEntry(MIMETYPE_LANG, "ucl"); // 国际版独有
        loadEntry(MIMETYPE_UPP, "upp");
        loadEntry(MIMETYPE_FLV, "flv");
        loadEntry("application/x-shockwave-flash", "swf");
        loadEntry("text/vnd.sun.j2me.app-descriptor", "jad");
        loadEntry("aplication/java-archive", "jar");
        loadEntry(MIMETYPE_DOC, "doc");
        loadEntry(MIMETYPE_DOC, "dot");
        loadEntry(MIMETYPE_XLS, "xls");
        loadEntry(MIMETYPE_PPT, "pps");
        loadEntry(MIMETYPE_PPT, "ppt");
        loadEntry(MIMETYPE_XLSX, "xlsx");
        loadEntry(MIMETYPE_DOCX, "docx");
        loadEntry(MIMETYPE_PPTX, "pptx");
        loadEntry("text/calendar", "ics");
        loadEntry("text/calendar", "icz");
        loadEntry("text/comma-separated-values", "csv");
        loadEntry("text/css", "css");
        loadEntry("text/h323", "323");
        loadEntry("text/iuls", "uls");
        loadEntry("text/mathml", "mml");
        loadEntry(MIMETYPE_TEXT, "txt");
        loadEntry(MIMETYPE_TEXT, "ini");
        loadEntry(MIMETYPE_TEXT, "asc");
        loadEntry(MIMETYPE_TEXT, "text");
        loadEntry(MIMETYPE_TEXT, "diff");
        loadEntry(MIMETYPE_TEXT, "log");
        loadEntry(MIMETYPE_TEXT, "ini");
        loadEntry(MIMETYPE_TEXT, "log");
        loadEntry(MIMETYPE_TEXT, "pot");
        loadEntry("application/umd", "umd");
        loadEntry("text/xml", "xml");
        loadEntry("text/html", "html");
        loadEntry("text/html", "xhtml");
        loadEntry("text/html", "htm");
        loadEntry("text/html", "asp");
        loadEntry("text/html", "php");
        loadEntry("text/html", "jsp");
        loadEntry("text/xml", "wml");
        loadEntry("text/richtext", "rtx");
        loadEntry("text/rtf", "rtf");
        loadEntry("text/texmacs", "ts");
        loadEntry("text/text", "phps");
        loadEntry("text/tab-separated-values", "tsv");
        loadEntry("text/x-bibtex", "bib");
        loadEntry("text/x-boo", "boo");
        loadEntry("text/x-c++hdr", "h++");
        loadEntry("text/x-c++hdr", "hpp");
        loadEntry("text/x-c++hdr", "hxx");
        loadEntry("text/x-c++hdr", "hh");
        loadEntry("text/x-c++src", "c++");
        loadEntry("text/x-c++src", "cpp");
        loadEntry("text/x-c++src", "cxx");
        loadEntry("text/x-chdr", "h");
        loadEntry("text/x-component", "htc");
        loadEntry("text/x-csh", "csh");
        loadEntry("text/x-csrc", "c");
        loadEntry("text/x-dsrc", "d");
        loadEntry("text/x-haskell", "hs");
        loadEntry("text/x-java", "java");
        loadEntry("text/x-literate-haskell", "lhs");
        loadEntry("text/x-moc", "moc");
        loadEntry("text/x-pascal", "p");
        loadEntry("text/x-pascal", "pas");
        loadEntry("text/x-pcs-gcd", "gcd");
        loadEntry("text/x-setext", "etx");
        loadEntry("text/x-tcl", "tcl");
        loadEntry("text/x-tex", "tex");
        loadEntry("text/x-tex", "ltx");
        loadEntry("text/x-tex", "sty");
        loadEntry("text/x-tex", "cls");
        loadEntry("text/x-vcalendar", "vcs");
        loadEntry("text/x-vcard", "vcf");
        loadEntry("application/andrew-inset", "ez");
        loadEntry("application/dsptype", "tsp");
        loadEntry("application/futuresplash", "spl");
        loadEntry("application/hta", "hta");
        loadEntry("application/mac-binhex40", "hqx");
        loadEntry("application/mac-compactpro", "cpt");
        loadEntry("application/mathematica", "nb");
        loadEntry("application/msaccess", "mdb");
        loadEntry("application/oda", "oda");
        loadEntry("application/ogg", "ogg");
        loadEntry(MIMETYPE_PDF, "pdf");
        loadEntry("application/pgp-keys", "key");
        loadEntry("application/pgp-signature", "pgp");
        loadEntry("application/pics-rules", "prf");
        loadEntry("application/rar", "rar");
        loadEntry("application/rdf+xml", "rdf");
        loadEntry("application/rss+xml", "rss");
        loadEntry("application/zip", "zip");
        loadEntry(MIMETYPE_APK, "apk");
        loadEntry("application/vnd.cinderella", "cdy");
        loadEntry("application/vnd.ms-pki.stl", "stl");
        loadEntry("application/vnd.oasis.opendocument.database", "odb");
        loadEntry("application/vnd.oasis.opendocument.formula", "odf");
        loadEntry("application/vnd.oasis.opendocument.graphics", "odg");
        loadEntry("application/vnd.oasis.opendocument.graphics-template", "otg");
        loadEntry("application/vnd.oasis.opendocument.image", "odi");
        loadEntry("application/vnd.oasis.opendocument.spreadsheet", "ods");
        loadEntry("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
        loadEntry("application/vnd.oasis.opendocument.text", "odt");
        loadEntry("application/vnd.oasis.opendocument.text-master", "odm");
        loadEntry("application/vnd.oasis.opendocument.text-template", "ott");
        loadEntry("application/vnd.oasis.opendocument.text-web", "oth");
        loadEntry("application/vnd.rim.cod", "cod");
        loadEntry("application/vnd.smaf", "mmf");
        loadEntry("application/vnd.stardivision.calc", "sdc");
        loadEntry("application/vnd.stardivision.draw", "sda");
        loadEntry("application/vnd.stardivision.impress", "sdd");
        loadEntry("application/vnd.stardivision.impress", "sdp");
        loadEntry("application/vnd.stardivision.math", "smf");
        loadEntry("application/vnd.stardivision.writer", "sdw");
        loadEntry("application/vnd.stardivision.writer", "vor");
        loadEntry("application/vnd.stardivision.writer-global", "sgl");
        loadEntry("application/vnd.sun.xml.calc", "sxc");
        loadEntry("application/vnd.sun.xml.calc.template", "stc");
        loadEntry("application/vnd.sun.xml.draw", "sxd");
        loadEntry("application/vnd.sun.xml.draw.template", "std");
        loadEntry("application/vnd.sun.xml.impress", "sxi");
        loadEntry("application/vnd.sun.xml.impress.template", "sti");
        loadEntry("application/vnd.sun.xml.math", "sxm");
        loadEntry("application/vnd.sun.xml.writer", "sxw");
        loadEntry("application/vnd.sun.xml.writer.global", "sxg");
        loadEntry("application/vnd.sun.xml.writer.template", "stw");
        loadEntry("application/vnd.visio", "vsd");
        loadEntry("application/x-abiword", "abw");
        loadEntry("application/x-apple-diskimage", "dmg");
        loadEntry("application/x-bcpio", "bcpio");
        loadEntry("application/x-bittorrent", "torrent");
        loadEntry("application/x-cdf", "cdf");
        loadEntry("application/x-cdlink", "vcd");
        loadEntry("application/x-chess-pgn", "pgn");
        loadEntry("application/x-cpio", "cpio");
        loadEntry("application/x-debian-package", "deb");
        loadEntry("application/x-debian-package", "udeb");
        loadEntry("application/x-director", "dcr");
        loadEntry("application/x-director", "dir");
        loadEntry("application/x-director", "dxr");
        loadEntry("application/x-dms", "dms");
        loadEntry("application/x-doom", "wad");
        loadEntry("application/x-dvi", "dvi");
        loadEntry("application/x-flac", "flac");
        loadEntry("application/x-font", "pfa");
        loadEntry("application/x-font", "pfb");
        loadEntry("application/x-font", "gsf");
        loadEntry("application/x-font", "pcf");
        loadEntry("application/x-font", "pcf.Z");
        loadEntry("application/x-freemind", "mm");
        loadEntry("application/x-futuresplash", "spl");
        loadEntry("application/x-gnumeric", "gnumeric");
        loadEntry("application/x-go-sgf", "sgf");
        loadEntry("application/x-graphing-calculator", "gcf");
        loadEntry("application/x-gtar", "gtar");
        loadEntry("application/x-gtar", "tgz");
        loadEntry("application/x-gtar", "taz");
        loadEntry("application/x-hdf", "hdf");
        loadEntry("application/x-ica", "ica");
        loadEntry("application/x-internet-signup", "ins");
        loadEntry("application/x-internet-signup", "isp");
        loadEntry("application/x-iphone", "iii");
        loadEntry("application/x-iso9660-image", "iso");
        loadEntry("application/x-jmol", "jmz");
        loadEntry("application/x-kchart", "chrt");
        loadEntry("application/x-killustrator", "kil");
        loadEntry("application/x-koan", "skp");
        loadEntry("application/x-koan", "skd");
        loadEntry("application/x-koan", "skt");
        loadEntry("application/x-koan", "skm");
        loadEntry("application/x-kpresenter", "kpr");
        loadEntry("application/x-kpresenter", "kpt");
        loadEntry("application/x-kspread", "ksp");
        loadEntry("application/x-kword", "kwd");
        loadEntry("application/x-kword", "kwt");
        loadEntry("application/x-latex", "latex");
        loadEntry("application/x-lha", "lha");
        loadEntry("application/x-lzh", "lzh");
        loadEntry("application/x-lzx", "lzx");
        loadEntry("application/x-maker", "frm");
        loadEntry("application/x-maker", "maker");
        loadEntry("application/x-maker", "frame");
        loadEntry("application/x-maker", "fb");
        loadEntry("application/x-maker", "book");
        loadEntry("application/x-maker", "fbdoc");
        loadEntry("application/x-mif", "mif");
        loadEntry("application/x-ms-wmd", "wmd");
        loadEntry("application/x-ms-wmz", "wmz");
        loadEntry("application/x-msi", "msi");
        loadEntry("application/x-ns-proxy-autoconfig", "pac");
        loadEntry("application/x-nwc", "nwc");
        loadEntry("application/x-object", "o");
        loadEntry("application/x-oz-application", "oza");
        loadEntry("application/x-pkcs7-certreqresp", "p7r");
        loadEntry("application/x-pkcs7-crl", "crl");
        loadEntry("application/x-quicktimeplayer", "qtl");
        loadEntry("application/x-shar", "shar");
        loadEntry("application/x-stuffit", "sit");
        loadEntry("application/x-sv4cpio", "sv4cpio");
        loadEntry("application/x-sv4crc", "sv4crc");
        loadEntry("application/x-tar", "tar");
        loadEntry("application/x-texinfo", "texinfo");
        loadEntry("application/x-texinfo", "texi");
        loadEntry("application/x-troff", "t");
        loadEntry("application/x-troff", "roff");
        loadEntry("application/x-troff-man", "man");
        loadEntry("application/x-ustar", "ustar");
        loadEntry("application/x-wais-source", "src");
        loadEntry("application/x-wingz", "wz");
        loadEntry("application/x-webarchive", "webarchive"); // added
        loadEntry("application/x-x509-ca-cert", "crt");
        loadEntry("application/x-xcf", "xcf");
        loadEntry("application/x-xfig", "fig");
        loadEntry("application/epub", "epub");
        loadEntry("audio/basic", "snd");
        loadEntry(MIMETYPE_MIDI, "mid");
        loadEntry(MIMETYPE_MIDI, "midi");
        loadEntry(MIMETYPE_MIDI, "kar");
        loadEntry(MIMETYPE_AUDIO, "mpga");
        loadEntry(MIMETYPE_AUDIO, "mpega");
        loadEntry(MIMETYPE_AUDIO, "mp2");
        loadEntry(MIMETYPE_AUDIO, "mp3");
        loadEntry(MIMETYPE_AUDIO, "apu");// 国际版独有
        loadEntry(MIMETYPE_AUDIO, "m4a");
        loadEntry("audio/mpegurl", "m3u");
        loadEntry("audio/prs.sid", "sid");
        loadEntry("audio/x-aiff", "aif");
        loadEntry("audio/x-aiff", "aiff");
        loadEntry("audio/x-aiff", "aifc");
        loadEntry("audio/x-gsm", "gsm");
        loadEntry("audio/x-mpegurl", "m3u");
        loadEntry(MIMETYPE_WMA, "wma");
        loadEntry("audio/x-ms-wax", "wax");
        loadEntry("audio/AMR", "amr");
        loadEntry(MIMETYPE_REALAUDIO, "ra");
        loadEntry(MIMETYPE_REALAUDIO, "rm");
        loadEntry(MIMETYPE_REALAUDIO, "ram");
        loadEntry("audio/x-realaudio", "ra");
        loadEntry("audio/x-scpls", "pls");
        loadEntry("audio/x-sd2", "sd2");
        loadEntry(MIMETYPE_WAV, "wav");
        loadEntry("image/bmp", "bmp"); // added
        loadEntry("image/gif", "gif");
        loadEntry("image/ico", "cur"); // added
        loadEntry("image/ico", "ico"); // added
        loadEntry("image/ief", "ief");
        loadEntry("image/jpeg", "jpeg");
        loadEntry("image/jpeg", "jpg");
        loadEntry("image/jpeg", "jpe");
        loadEntry("image/pcx", "pcx");
        loadEntry("image/png", "png");
        loadEntry("image/svg+xml", "svg");
        loadEntry("image/svg+xml", "svgz");
        loadEntry("image/tiff", "tiff");
        loadEntry("image/tiff", "tif");
        loadEntry("image/vnd.djvu", "djvu");
        loadEntry("image/vnd.djvu", "djv");
        loadEntry("image/vnd.wap.wbmp", "wbmp");
        loadEntry("image/x-cmu-raster", "ras");
        loadEntry("image/x-coreldraw", "cdr");
        loadEntry("image/x-coreldrawpattern", "pat");
        loadEntry("image/x-coreldrawtemplate", "cdt");
        loadEntry("image/x-corelphotopaint", "cpt");
        loadEntry("image/x-icon", "ico");
        loadEntry("image/x-jg", "art");
        loadEntry("image/x-jng", "jng");
        loadEntry("image/x-ms-bmp", "bmp");
        loadEntry("image/x-photoshop", "psd");
        loadEntry("image/x-portable-anymap", "pnm");
        loadEntry("image/x-portable-bitmap", "pbm");
        loadEntry("image/x-portable-graymap", "pgm");
        loadEntry("image/x-portable-pixmap", "ppm");
        loadEntry("image/x-rgb", "rgb");
        loadEntry("image/x-xbitmap", "xbm");
        loadEntry("image/x-xpixmap", "xpm");
        loadEntry("image/x-xwindowdump", "xwd");
        loadEntry("model/iges", "igs");
        loadEntry("model/iges", "iges");
        loadEntry("model/mesh", "msh");
        loadEntry("model/mesh", "mesh");
        loadEntry("model/mesh", "silo");
        loadEntry("text/calendar", "ics");
        loadEntry("text/calendar", "icz");
        loadEntry("text/comma-separated-values", "csv");
        loadEntry("text/css", "css");
        loadEntry("text/h323", "323");
        loadEntry("text/iuls", "uls");
        loadEntry("text/mathml", "mml");
        // add it first so it will be the default for ExtensionFromMimeType
        loadEntry(MIMETYPE_TEXT, "txt");
        loadEntry(MIMETYPE_TEXT, "asc");
        loadEntry(MIMETYPE_TEXT, "text");
        loadEntry(MIMETYPE_TEXT, "diff");
        loadEntry(MIMETYPE_TEXT, "pot");
        loadEntry(MIMETYPE_TEXT, "umd");
        loadEntry("text/richtext", "rtx");
        loadEntry("text/rtf", "rtf");
        loadEntry("text/texmacs", "ts");
        loadEntry("text/text", "phps");
        loadEntry("text/tab-separated-values", "tsv");
        loadEntry("text/x-bibtex", "bib");
        loadEntry("text/x-boo", "boo");
        loadEntry("text/x-c++hdr", "h++");
        loadEntry("text/x-c++hdr", "hpp");
        loadEntry("text/x-c++hdr", "hxx");
        loadEntry("text/x-c++hdr", "hh");
        loadEntry("text/x-c++src", "c++");
        loadEntry("text/x-c++src", "cpp");
        loadEntry("text/x-c++src", "cxx");
        loadEntry("text/x-chdr", "h");
        loadEntry("text/x-component", "htc");
        loadEntry("text/x-csh", "csh");
        loadEntry("text/x-csrc", "c");
        loadEntry("text/x-dsrc", "d");
        loadEntry("text/x-haskell", "hs");
        loadEntry("text/x-java", "java");
        loadEntry("text/x-literate-haskell", "lhs");
        loadEntry("text/x-moc", "moc");
        loadEntry("text/x-pascal", "p");
        loadEntry("text/x-pascal", "pas");
        loadEntry("text/x-pcs-gcd", "gcd");
        loadEntry("text/x-setext", "etx");
        loadEntry("text/x-tcl", "tcl");
        loadEntry("text/x-tex", "tex");
        loadEntry("text/x-tex", "ltx");
        loadEntry("text/x-tex", "sty");
        loadEntry("text/x-tex", "cls");
        loadEntry("text/x-vcalendar", "vcs");
        loadEntry("text/x-vcard", "vcf");
        loadEntry(MIMETYPE_3GP, "3gp");
        loadEntry(MIMETYPE_3GP, "3g2");
        loadEntry("video/dl", "dl");
        loadEntry("video/dv", "dif");
        loadEntry("video/dv", "dv");
        loadEntry("video/fli", "fli");
        loadEntry(MIMETYPE_MPEG, "mpeg");
        loadEntry(MIMETYPE_MPEG, "mpg");
        loadEntry(MIMETYPE_MPEG, "mpe");
        loadEntry(MIMETYPE_MPEG, "VOB");
        loadEntry(MIMETYPE_MP4, "mp4");
        /* 离线视频专用格式 */
        loadEntry(MIMETYPE_MP4, "vdat");
        loadEntry(MIMETYPE_QUICKTIME, "qt");
        loadEntry(MIMETYPE_QUICKTIME, "mov");
        loadEntry("video/vnd.mpegurl", "mxu");
        loadEntry("video/x-la-asf", "lsf");
        loadEntry("video/x-la-asf", "lsx");
        loadEntry("video/x-mng", "mng");
        loadEntry(MIMETYPE_ASF, "asf");
        loadEntry(MIMETYPE_ASF, "asx");
        loadEntry("video/x-ms-wm", "wm");
        loadEntry(MIMETYPE_WMV, "wmv");
        loadEntry("video/x-ms-wmx", "wmx");
        loadEntry("video/x-ms-wvx", "wvx");
        loadEntry(MIMETYPE_AVI, "avi");
        loadEntry(MIMETYPE_MOIVE, "movie");
        loadEntry("x-conference/x-cooltalk", "ice");
        loadEntry("x-epoc/x-sisx-app", "sisx");
        loadEntry("application/vnd.apple.mpegurl", "m3u8");
        loadEntry(MIMETYPE_RMVB, "rmvb");
        loadEntry(MIMETYPE_RMVB, "rm");
        loadEntry("video/x-matroska", "mkv");
        loadEntry("video/x-f4v", "f4v");
        loadEntry("audio/aac", "aac");
    }

    public static final HashSet<String> VIDEO_EXTENSIONS = new HashSet<String>(64);

    static {
        VIDEO_EXTENSIONS.add("m1v");
        VIDEO_EXTENSIONS.add("mp2");
        VIDEO_EXTENSIONS.add("mpe");
        VIDEO_EXTENSIONS.add("mpeg");
        VIDEO_EXTENSIONS.add("mp4");
        VIDEO_EXTENSIONS.add("m4v");
        VIDEO_EXTENSIONS.add("3gp");
        VIDEO_EXTENSIONS.add("3gpp");
        VIDEO_EXTENSIONS.add("3g2");
        VIDEO_EXTENSIONS.add("3gpp2");
        VIDEO_EXTENSIONS.add("mkv");
        VIDEO_EXTENSIONS.add("webm");
        VIDEO_EXTENSIONS.add("mts");
        VIDEO_EXTENSIONS.add("ts");
        VIDEO_EXTENSIONS.add("tp");
        VIDEO_EXTENSIONS.add("wmv");
        VIDEO_EXTENSIONS.add("asf");
        VIDEO_EXTENSIONS.add("flv");
        VIDEO_EXTENSIONS.add("asx");
        VIDEO_EXTENSIONS.add("f4v");
        VIDEO_EXTENSIONS.add("hlv");
        VIDEO_EXTENSIONS.add("mov");
        VIDEO_EXTENSIONS.add("qt");
        VIDEO_EXTENSIONS.add("rm");
        VIDEO_EXTENSIONS.add("rmvb");
        VIDEO_EXTENSIONS.add("vob");
        VIDEO_EXTENSIONS.add("avi");
        VIDEO_EXTENSIONS.add("ogv");
        VIDEO_EXTENSIONS.add("ogg");
        VIDEO_EXTENSIONS.add("viv");
        VIDEO_EXTENSIONS.add("vivo");
        VIDEO_EXTENSIONS.add("wtv");
        VIDEO_EXTENSIONS.add("avs");
        VIDEO_EXTENSIONS.add("yuv");
        VIDEO_EXTENSIONS.add("m3u8");
        VIDEO_EXTENSIONS.add("m3u");
        VIDEO_EXTENSIONS.add("bdv");
        VIDEO_EXTENSIONS.add("vdat");
        VIDEO_EXTENSIONS.add("m4a");
        VIDEO_EXTENSIONS.add("mj2");
        VIDEO_EXTENSIONS.add("mpg");
        VIDEO_EXTENSIONS.add("vobsub");
        VIDEO_EXTENSIONS.add("evo");
        VIDEO_EXTENSIONS.add("m2ts");
        VIDEO_EXTENSIONS.add("ssif");
        VIDEO_EXTENSIONS.add("mpegts");
        VIDEO_EXTENSIONS.add("h264");
        VIDEO_EXTENSIONS.add("h263");
        VIDEO_EXTENSIONS.add("m2v");
    }

    public static boolean isVideoPath(String path) {
        if (StringUtil.isEmptyWithTrim(path)) {
            return false;
        }

        int index = path.indexOf("?");
        if (index > 0) {
            path = path.substring(0, index);
        }
        int lastDot = path.lastIndexOf(".");
        if (lastDot <= 0) {
            return false;
        }

        return VIDEO_EXTENSIONS.contains(path.substring(lastDot + 1).toLowerCase(Locale.ENGLISH));
    }

    public static boolean isVideoExtension(String extension) {
        if (StringUtil.isEmpty(extension)) {
            return false;
        }
        return VIDEO_EXTENSIONS.contains(extension.toLowerCase(Locale.ENGLISH));
    }
}
