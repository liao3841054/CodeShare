package com.uc.common.util.device;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.text.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtil {

    private static final int NEW_API_LEVEL_FOR_STATFS = Build.VERSION_CODES.JELLY_BEAN_MR2;

    private static final String OLD_SDCARD_PATH = "/sdcard";

    public static class DiskInfo {
        public long mTotalSize;
        public long mAvailableSize;
    }

    private static volatile StorageUtil mInstance;

    private List<String> mListStorageAll = null;
    private List<String> mListStorageAvaliable = null;
    private List<String> mListStorageExternal = null;
    private List<String> mListStorageInternal = null;
    private String mStoragePrimary = null;
    private boolean mExternalStorageWritable = false;

    public static StorageUtil getInstance() {
        if (mInstance == null) {
            synchronized (StorageUtil.class) {
                if (mInstance == null) {
                    mInstance = new StorageUtil();
                }
            }

        }
        return mInstance;
    }

    private StorageUtil() {
        mListStorageAll = new ArrayList<String>();
        mListStorageAvaliable = new ArrayList<String>();
        mListStorageExternal = new ArrayList<String>();
        mListStorageInternal = new ArrayList<String>();
        mStoragePrimary = "";

        getSysStorage();
    }

    private void getSysStorage() {
        // 这是用常规sdk公开的方法获取存储空间，只能获取到一个
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            getSysStorageByReflect(ApplicationContext.get());
        } else {
            getSysStorageByNormalAPI();
        }
    }

    /**
     * 调用系统原生支持的API去获取当前默认的存储器
     */
    private void getSysStorageByNormalAPI() {
        setStoragePrimary();
        setPrimaryStorageIntoList();
    }

    /**
     * 通过反射方式去获取存储器列表
     */
    private void getSysStorageByReflect(Context context) {
        try {
            // 下面是用一些隐藏的api获取存储空间，可以获得多个存储返回
            // 这个类低版本的API就有，但是其中的getVolumeList方法是API Level 12以上才有
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Method methodGetVolumeList = null;
            // 这个方法是StorageManager中的@hide方法
            methodGetVolumeList = storageManager.getClass().getDeclaredMethod("getVolumeList");
            //            methodGetVolumeList = StorageManager.class.getMethod("getVolumeList");
            methodGetVolumeList.setAccessible(true);

            // 通过getVolumeList方法取得所有StorageVolume的可用扇区
            StorageVolume[] storageVolume = (StorageVolume[]) methodGetVolumeList.invoke(storageManager);

            //取得一个扇区存储路径的方法
            Method methodGetPath = StorageVolume.class.getMethod("getPath");
            methodGetPath.setAccessible(true);

            //取得一个扇区是否可移动的方法
            Method methodIsremoveable = StorageVolume.class.getMethod("isRemovable");
            methodIsremoveable.setAccessible(true);

            //输出结果
            for (int i = 0; i < storageVolume.length; i++) {
                String path = (String) methodGetPath.invoke(storageVolume[i]);
                boolean removeable = (Boolean) methodIsremoveable.invoke(storageVolume[i]);

                mListStorageAll.add(path);

                if (removeable) {
                    mListStorageExternal.add(path);
                } else {
                    mListStorageInternal.add(path);
                }

                if (Environment.MEDIA_MOUNTED.equals(getStorageState(storageVolume[i], path))) {
                    mListStorageAvaliable.add(path);
                }

                //Log.d("TAG", "Kenlai_getSysStorageByReflect() volume " + i + " path is :" + path + "  ,storageId is:" + Integer.toHexString(storageId) + ", isRemoveable:" + removeable + " ,isEmulated:" + emulated);
            }

            setStoragePrimary();
            setPrimaryStorageIntoList();
        }
        catch (Exception e) {
            e.printStackTrace();
            getSysStorageByNormalAPI();
        }
    }

    private static String getStorageState(StorageVolume volume, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4.2以后StorageVolume有getState的public方法
            try {
                Method methodGetState = StorageVolume.class.getDeclaredMethod("getState");
                methodGetState.setAccessible(true);
                return (String) methodGetState.invoke(volume);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getStorageState(path);
    }

    public static String getStorageState(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0以后有公开Environment.getExternalStorageState
            try {
                return Environment.getExternalStorageState(new File(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // hidden api
                // IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
                // return mountService.getVolumeState(path);
                Object mountService = getMountService();
                if (mountService != null) {
                    Method getVolumeState = mountService.getClass().getDeclaredMethod("getVolumeState", String.class);
                    getVolumeState.setAccessible(true);
                    return (String) getVolumeState.invoke(mountService, path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Environment.MEDIA_REMOVED;
    }

    private void setPrimaryStorageIntoList() {
        if (!(this.mStoragePrimary == null || "".equalsIgnoreCase(this.mStoragePrimary))) {
            if (!this.mListStorageAll.contains(this.mStoragePrimary)) {
                // all也补充上以统一数据
                this.mListStorageAll.add(0, this.mStoragePrimary);
            }

            if (!this.mListStorageAvaliable.contains(this.mStoragePrimary)) {
                // 确保旧的sdcard一定在available里．
                this.mListStorageAvaliable.add(0, this.mStoragePrimary);
            }

            if (!this.mListStorageExternal.contains(this.mStoragePrimary)) {
                // 加到外置列表里
                this.mListStorageExternal.add(0, this.mStoragePrimary);
            }

            if (this.mListStorageInternal.contains(this.mStoragePrimary)) {
                // 如果在内置列表里面存在,将其去除.
                this.mListStorageExternal.remove(this.mStoragePrimary);
            }
        }
    }
    /**
     * 重新刷新存储器状态
     */
    public void refreshStorageState() {
        mListStorageAll.clear();
        mListStorageAvaliable.clear();
        mListStorageExternal.clear();
        mListStorageInternal.clear();
        getSysStorage();
    }
    /**
     * 获取所有存储器列表(包含模拟存储器)
     *
     * @return
     */
    public List<String> getStorageListAll() {
        return mListStorageAll;
    }

    /**
     * 获取所有可用存储器列表(不包含模拟存储器)
     *
     * @return
     */
    public List<String> getStorageListAvailable() {
        return mListStorageAvaliable;
    }

    /**
     * 返回所有外置可移动存储器列表
     *
     * @return
     */
    public List<String> getStorageExternal() {
        return mListStorageExternal;
    }

    /**
     * 返回所有内置不可移动存储器列表
     *
     * @return
     */
    public List<String> getStorageInternal() {
        return mListStorageInternal;
    }

    /**
     * 获取系统默认主存储器
     *
     * @return
     */
    public String getStoragePrimary() {
        return mStoragePrimary;
    }

    /**
     * 设置系统默认存储器内容
     */
    private void setStoragePrimary() {
        String externalStorageState = null;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (Exception e) {
            // see bug 0311362, Environment.getExternalStorageState() may cause NPE
        }
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(externalStorageState)) {
            mExternalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(externalStorageState)) {
            mExternalStorageWritable = false;
        }

        mStoragePrimary = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 根据传入的Path, 获取其对应的SDCard存储器所属根路径
     * @return null,没有找到；否则找到．
     */
    public String getStorageCategory(String path) {
        if (path == null) {
            return null;
        }

        /**
         * TODO:由于Model层方法SettingModel.getValueByKey(SettingKeysDef.
         * DOWNLOAD_PATH)获取到的默认值为hardcode在C++层的值"/sdcard/UCDownloads/"
         * 如果path为"/sdcard/"开始，则返回默认SD卡路径。在Model层相应优化完成后，修改这里。
         */
        if (path.startsWith("/sdcard/")) {
            String sdPath = getExternalStorageDirectoryPath();
            return sdPath;
        }

        String temp = path.trim().toLowerCase() + File.separatorChar;
        for (String item : mListStorageAvaliable) {
            if (temp.startsWith(item.toLowerCase() + File.separatorChar)) {
                return item;
            }
        }

        return null;
    }

    /**
     * 判断系统默认主存储器是否可读可写
     *
     * @return
     */
    public boolean isDefaultStorageWritable() {
        return mExternalStorageWritable;
    }

    /**
     * sdcard是否已挂载
     */
    public static final boolean isExternalStorageMounted() {
        try {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否有可存储的介质
     */
    public static final boolean hasStorage() {
        return isExternalStorageMounted() || getInstance().getStorageListAvailable().size() > 1;
    }

    /**
     * 外部存储目录
     * @return
     */
    public static File getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * 外部存储目录, 注意返回值后面是没有"/"的
     * 比如/storage/emulated/0
     * @return
     */
    public static String getExternalStorageDirectoryPath() {
        return getExternalStorageDirectory().getAbsolutePath();
    }

    public static List<String> getSecondaryStorages() {
        List<String> available = getInstance().getStorageListAvailable();
        String storagePrimary = getInstance().getStoragePrimary();

        List<String> sndStorageList = new ArrayList<String>();
        for (String storage : available) {
            if (storage.equals(storagePrimary)) {
                continue;
            }
            sndStorageList.add(storage);
        }
        return sndStorageList;
    }

    /**
     *
     * @param path
     * @return 获取失败返回null, 一般是构造StatFs出了异常
     */
    public static DiskInfo getDiskInfo(String path) {
        DiskInfo diskInfo = null;
        try {
            StatFs statFs = new StatFs(path);
            diskInfo = new DiskInfo();
            if (Build.VERSION.SDK_INT >= NEW_API_LEVEL_FOR_STATFS) {
                diskInfo.mAvailableSize = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
                diskInfo.mTotalSize = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
            } else {
                //需要类型转换，防止溢出. 【下载】sdcard剩余容量超过2G时，若下载出错，会错误提示sdcard空间不足
                diskInfo.mAvailableSize = (long)statFs.getBlockSize() * statFs.getAvailableBlocks();
                diskInfo.mTotalSize = (long)statFs.getBlockSize() * statFs.getBlockCount();
            }
        } catch (Exception e) {
            // do nothing
        }

        return diskInfo;
    }
    public static DiskInfo getTotalDiskInfo() {
        DiskInfo diskInfo = new DiskInfo();
        List<String> storageList = getInstance().getStorageListAvailable();
        for (String path : storageList) {
            DiskInfo diskInfo2 = getDiskInfo(path);
            if (diskInfo2 != null) {
                diskInfo.mAvailableSize += diskInfo2.mAvailableSize;
                diskInfo.mTotalSize += diskInfo2.mTotalSize;
            }
        }
        return diskInfo;
    }

    public static List<String> getWritableSDCardList() {
        ArrayList<String> writableList = new ArrayList<String>();
        List<String> availableSDCardList = getInstance().getStorageListAvailable();
        for (String sdcardPath : availableSDCardList) {
            if (isStorageWritable(sdcardPath)) {
                writableList.add(sdcardPath);
            }
        }
        return writableList;
    }

    public static String getWritableInternalSDCard() {
        List<String> writableSDCardList = getWritableSDCardList();
        List<String> internalSDCardList = getInstance().getStorageInternal();

        if (writableSDCardList == null || writableSDCardList.size() == 0) {
            return null;
        }

        for (String internalSDCard : internalSDCardList) {
            if (writableSDCardList.contains(internalSDCard)) {
                return internalSDCard;
            }
        }

        return null;
    }

    public static String getWritableExternalSDCard() {
        List<String> writableSDCardList = getWritableSDCardList();
        List<String> internalSDCardList = getInstance().getStorageInternal();

        if (writableSDCardList == null || writableSDCardList.size() == 0) {
            return null;
        }

        if (internalSDCardList == null || internalSDCardList.size() == 0) {
            return writableSDCardList.get(0);
        }

        for (String writableSDCard : writableSDCardList) {
            if (!internalSDCardList.contains(writableSDCard)) {
                return writableSDCard;
            }
        }

        return null;
    }


    public final static boolean isStorageWritable(String path) {
        String state = getStorageState(path);
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state);
    }

    /**
     *
     * @param file
     * @return -1表示获取失败
     * @throws FileNotFoundException file为null或文件不存在
     */
    public static long getFileTotalSize(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        final DiskInfo diskInfo = getDiskInfo(file.getPath());
        return diskInfo != null ? diskInfo.mTotalSize : -1;
    }

    /**
     *
     * @param filePath
     * @return -1表示获取失败
     * @throws FileNotFoundException filePath为null, 或filePath为"", 或filePath文件不存在
     */
    public static long getFileTotalSize(String filePath) throws FileNotFoundException {
        if (StringUtil.isEmpty(filePath)) {
            throw new FileNotFoundException("File path illegal");
        }
        return getFileTotalSize(new File(filePath));
    }

    /**
     *
     * @param file
     * @return -1表示获取失败
     * @throws FileNotFoundException file为null或文件不存在
     */
    public final static long getFileAvailableSize(File file) throws FileNotFoundException {
        if (null == file || !file.exists()) {
            throw new FileNotFoundException();
        }
        final DiskInfo diskInfo = getDiskInfo(file.getPath());
        return diskInfo != null ? diskInfo.mAvailableSize : -1;
    }

    /**
     *
     * @param filePath
     * @return -1表示获取失败
     * @throws FileNotFoundException filePath为null, 或filePath为"", 或filePath文件不存在
     */
    public final static long getFileAvailableSize(String filePath) throws FileNotFoundException {
        if (StringUtil.isEmpty(filePath)) {
            throw new FileNotFoundException("File path illegal");
        }
        return getFileAvailableSize(new File(filePath));
    }

    /**
     *
     * @return -1表示获取失败
     * @throws FileNotFoundException SD卡不存在
     */
    public final static long getSDCardTotalSize() throws FileNotFoundException {
        if (!isExternalStorageMounted()) {
            throw new FileNotFoundException("SDCard not exists");
        }
        File sdcardDir = Environment.getExternalStorageDirectory();
        return getFileTotalSize(sdcardDir);
    }

    /**
     *
     * @return -1表示获取失败
     * @throws FileNotFoundException SD卡不存在
     */
    public final static long getSDCardAvailableSize() throws FileNotFoundException {
        if (!isExternalStorageMounted()) {
            throw new FileNotFoundException("SDCard not exists");
        }
        File sdcardDir = Environment.getExternalStorageDirectory();
        return getFileAvailableSize(sdcardDir);
    }

    /**
     *
     * @return -1表示获取失败
     * @throws FileNotFoundException system路径不存在
     */
    public final static long getSystemTotalSize() throws FileNotFoundException {
        File root = Environment.getRootDirectory();
        return getFileTotalSize(root);
    }

    /**
     *
     * @return -1表示获取失败
     * @throws FileNotFoundException system路径不存在
     */
    public final static long getSystemAvailableSize() throws FileNotFoundException {
        File root = Environment.getRootDirectory();
        return getFileAvailableSize(root);
    }


    /**
     * 将以"/sdcard"开始的文件路径转换成实际路径), 主要场景是旧代码中传过来的路径是hardcode的/sdcard,
     * 替换成Environment.getExternalStorageDirectory().getAbsolutePath()
     * @param filePath
     * @return
     */
    public static String convertOldSdcardPathToRealPath(String filePath) {
        if (StringUtil.isNotEmptyWithTrim(filePath) && filePath.startsWith(OLD_SDCARD_PATH)) {
            filePath = filePath.replaceFirst(OLD_SDCARD_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        return filePath;
    }

    public static String getAvailableStorage() {
        // 如果主存储可用，则使用主存储
        if (getInstance().isDefaultStorageWritable()) {
            return getInstance().getStoragePrimary();
        }

        // 主存储不可用，选中一个非主存储
        if (getInstance().getStorageListAvailable().size() > 0) {
            return getInstance().getStorageListAvailable().get(0);
        }

        // 没有可用的存储介质，还是返回主存储的路径
        return getInstance().getStoragePrimary();
    }

    private static Object getMountService() {
        try {
            // IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
            //利用反射得到ServiceManager类中的getService方法
            Method getService = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder binder = (IBinder) getService.invoke(null, "mount");

            Class<?> iMountService$Stub = Class.forName("android.os.storage.IMountService$Stub");
            Method asInterface = iMountService$Stub.getMethod("asInterface", new Class[]{IBinder.class});//获取Stub的asInterface(IBinder binder)方法，
            asInterface.setAccessible(true);
            Object iMountService = asInterface.invoke(null, new Object[]{binder});//通过asInterface(IBinder binder)方法获得IMountService类的一个实例对象mIMountService

            return iMountService;
        } catch (Exception e) {

        }
        return null;
    }

    public static final boolean checkPathAvailable(String path){
        boolean bAvailable = false;
        if(!TextUtils.isEmpty(path)) {
            //如果是系统的externalStorage存储路径
            File es = Environment.getExternalStorageDirectory();
            if (es != null) {
                if (path.contains(es.toString())) {
                    bAvailable = isExternalStorageMounted();
                    return bAvailable;
                }
            }

            List<String> storageList = getInstance().getStorageListAvailable();
            for (int i = 0; i < storageList.size(); i++) {
                String storage = storageList.get(i);
                if (path.contains(storage)) {
                    if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(getStorageState(storage))) {
                        bAvailable = true;
                    }
                }
            }
        }
        return bAvailable;
    }

    //=========================上面是中文版的StorageUtils====================
    /**
     * 判断是否应用私有目录
     * 先简单使用路径对比
     * TODO 要考虑内置外置SD卡都有的情况
     * @param path
     * @return
     */
    public static boolean isApplicationExternalDir(String path) {
        if (StringUtil.isEmptyWithTrim(path)) {
            return false;
        }
        // 替换历史写死的SD卡
        path = convertOldSdcardPathToRealPath(path);
        File dir = ApplicationContext.get().getExternalFilesDir(null);
        // 如果获取不到应用目录，认为非私有目录
        if (dir == null) {
            return false;
        }
        return path.startsWith(dir.getParent());
    }

    /**
     * 获取SD卡应用文件根目录
     * 一般不允许随意添加文件到根目录
     * @return
     */
    public static File getApplicationExternalFilesDir() {
        return getApplicationExternalFilesDir(null);
    }

    /**
     * 获取SD卡应用文件子目录
     * 用于存放需要落地的文件数据
     * 该目录无需权限
     * 用户在应用详情处清除数据会清空此目录
     * @param dir 子目录
     * @return
     */
    public static File getApplicationExternalFilesDir(String dir) {
        return ApplicationContext.get().getExternalFilesDir(dir);
    }

    public static File[] getApplicationExternalFilesDirs() {
        return getApplicationExternalFilesDirs(null);
    }

    public static File[] getApplicationExternalFilesDirs(String dir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return ApplicationContext.get().getExternalFilesDirs(dir);
        } else {
            return new File[] {getApplicationExternalFilesDir(dir)};
        }
    }

     /**
     * 获取存储路径
     * 如果由于SD卡挂载原因获取不到则会返回一个根据规则拼装的路径
     * 适用于内核初始化等必须设置路径的逻辑
     * @param dir
     * @return
     */
    public static String getApplicationExternalFilesDirPath(String dir) {
        File file = getApplicationExternalFilesDir(dir);
        if (file != null) {
            return file.getAbsolutePath();
        }
        // 如果获取不到，一般是SD卡未挂载
        // 返回一个拼装的路径
        String base = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data" + File.separator + ApplicationContext.get().getPackageName() + File.separator + "files";
        if (StringUtil.isEmptyWithTrim(dir)) {
            return base;
        }
        return base + File.separator + dir;
    }

    /**
     * 获取SD卡应用缓存根目录
     * 一般不允许随意添加文件到根目录
     * @return
     */
    public static File getApplicationExternalCacheDir() {
        return getApplicationExternalCacheDir(null);
    }

    /**
     * 获取SD卡应用缓存子目录
     * 用于存放缓存数据
     * 该目录无需权限
     * 用户在应用详情处清除缓存会清空此目录
     * @param dir
     * @return
     */
    public static File getApplicationExternalCacheDir(String dir) {
        File root = ApplicationContext.get().getExternalCacheDir();
        if (StringUtil.isEmptyWithTrim(dir)) {
            return root;
        }
        File file = new File(root, dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

}
