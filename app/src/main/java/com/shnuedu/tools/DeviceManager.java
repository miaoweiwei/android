package com.shnuedu.tools;

public class DeviceManager {

    private Device device;
    //region 单例
    private DeviceManager() {
    }

    /* volatile关键字的一个作用是禁止指令重排，把instance声明为volatile之后，
     * 对它的写操作就会有一个内存屏障（什么是内存屏障？），
     * 这样，在它的赋值完成之前，就不用会调用读操作。
     */
    private static volatile DeviceManager instance = null;

    /**
     * 用单例模式获取NetworkHelp类的一个实例
     *
     * @return
     */
    public static DeviceManager getInstance() {
        if (instance == null) {
            synchronized (NetworkHelp.class) {
                if (instance == null) {
                    instance = new DeviceManager();
                }
            }
        }
        return instance;
    }
    //endregion
}
