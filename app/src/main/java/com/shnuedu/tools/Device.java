package com.shnuedu.tools;

public class Device {
    //设备名字
    public String Name;
    //设备的Ip地址
    public String Ip;
    //设备的TCP通信端口,命令的传输
    public int TcpPort = 888;
    //设备的UDP通信端口，设备的搜索
    public int UdpPort = 777;
    //设备ID
    public String DeviceId;
    //设备的网络模式
    public int NetworkMode;
    //电源电量
    public int PowerBattery;
    //运行模式
    public int Mode;
    public String Select;
    //强度
    public int Strength;
    //频率
    public int Frequency;
    //是否开机
    public boolean IsBoot;
}
