package com.shnuedu.tools;

public class Message {
    /**
     * UPD搜索设备 消息ID
     */
    public static final int SearchDevice_MsgId = 01;
    /**
     * 扫描wifi 消息ID
     */
    public static final int ScanWifi_MsgId = 02;
    /**
     * 连接wifi
     */
    public static final int ConnectWifi_MsgId = 03;
    /**
     * tcp状态
     */
    public static final int TcpStatus_MsgId = 04;
    /**
     * 发送命令
     */
    public static final int Command_MsgId = 05;
    /**
     * 关闭tcp
     */
    public static final int CloseTcp_MsgId = 06;

    public static final String SearchDevice_Msg = "Mobile";
    public static final String TcpConnectStatus_Msg = "isIdle";
    public static final String ScanWifi_Msg = "scan";
    public static final String ConnectWifi_Msg = "success";
    public static final String CloseTcp_Msg = "close";

    public static final String TcpStatus_Msg_Idle = "idle";
    public static final String TcpStatus_Msg_Busy = "busy";

    public static final int Network_Mode_Station = 1;
    public static final int Network_Mode_Ap = 2;
    public static final int Network_Mode_Ap_Station = 3;
}
