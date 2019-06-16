package com.shnuedu.tools;

public class Device {
    //设备名字
    private String name;
    //设备的Ip地址
    private String ip;
    //设备的TCP通信端口,命令的传输
    private int tcpPort = 888;
    //设备的UDP通信端口，设备的搜索
    private int udpPort = 777;
    //设备ID
    private String deviceId;
    //设备当前的状态
    private String state;
    //电源电量
    private int powerBattery;
    //运行模式
    private String operatingMode;
    //强度
    private int strength;

    /**
     * 获取设备名字
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置设备名字
     *
     * @return
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取设备的Ip地址
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * 设置设备的Ip地址
     *
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 获取设置的TCP通信端口
     *
     * @return
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * 设备的UDP通信端口，设备的搜索
     *
     * @return
     */
    public int getUdpPort() {
        return udpPort;
    }

    /**
     * 获取设备的ID
     *
     * @return
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备的ID
     *
     * @return
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 获取设备的状态
     *
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     * 设置设备的状态
     *
     * @param state
     */
    private void setState(String state) {
        this.state = state;
    }

    /**
     * 获取设备的电量
     *
     * @return
     */
    public int getPowerBattery() {
        return powerBattery;
    }

    /**
     * 设置设备的电量
     *
     * @param powerBattery
     */
    private void setPowerBattery(int powerBattery) {
        this.powerBattery = powerBattery;
    }
}
