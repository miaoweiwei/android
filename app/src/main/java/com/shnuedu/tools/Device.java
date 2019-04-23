package com.shnuedu.tools;

public class Device {
    //设备名字
    private String name;
    //设备的Ip地址
    private String ip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPowerBattery() {
        return powerBattery;
    }

    public void setPowerBattery(int powerBattery) {
        this.powerBattery = powerBattery;
    }

    //设备ID
    private String deviceId;
    //设备当前的状态
    private String state;
    //电源电量
    private int powerBattery;
}
