package com.shnuedu.tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkHelp {
    private List<DatagramPacket> udpRequestList = null;//UDP 请求返回的数据列表
    private ServerSocket tcpSocket = null;

    private int udpPort = 777;//执行搜索设备的udp 的端口 ESP8266 的端口
    private int tcpPort = 888;//执行控制设备的tcp 的端口 ESP8266 的端口

    private Thread thUdp = null;//执行搜索设备的线程
    private Thread thTcp = null;//执行控制设备的线性，与设备进行通信

    private int receiveTimeOut = 2000;//定时器

    private Timer ProcessingUdpRequestTimer = null;
    private TimerTask receiveRequestTask = null;
    private int delayTime = 3000;//udp接收的时间长度
    private long begintime;//启动时间

    //定义Udp接收事件
    private OnNetReceiveListener mListener;

    //region 单例
    private NetworkHelp() {
    }

    /* volatile关键字的一个作用是禁止指令重排，把instance声明为volatile之后，
     * 对它的写操作就会有一个内存屏障（什么是内存屏障？），
     * 这样，在它的赋值完成之前，就不用会调用读操作。
     */
    private static volatile NetworkHelp instance = null;

    /**
     * 用单例模式获取NetworkHelp类的一个实例
     *
     * @return
     */
    public static NetworkHelp getInstance() {
        if (instance == null) {
            synchronized (NetworkHelp.class) {
                if (instance == null) {
                    instance = new NetworkHelp();
                }
            }
        }
        return instance;
    }
    //endregion

    public interface OnNetReceiveListener {
        void onUdpReceiveListener(Device device);

        void onTcpReceiveListener(Device device);
    }

    private void OnUdpReceive(Device device) {
        if (mListener != null) {
            mListener.onUdpReceiveListener(device);
        }
    }

    private void OnTcpReceive(Device device) {
        if (mListener != null) {
            mListener.onTcpReceiveListener(device);
        }
    }

    /**
     * 开启设备搜索
     */
    public void startSearchDevice() {
        if (udpRequestList != null) udpRequestList.clear();
        startUdpTh();
        thUdp.start();//开始搜索
        if (ProcessingUdpRequestTimer == null)
            ProcessingUdpRequestTimer = new Timer();
        if (receiveRequestTask == null) { //处理udp返回的数据队列，添加新的设备
            receiveRequestTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (NetworkHelp.class) {
                        if (udpRequestList == null || udpRequestList.size() == 0) return;
                        while (udpRequestList.size() > 0) {
                            DatagramPacket request = udpRequestList.remove(0);//从队列中取出一个接收到的信息
                            Device device = new Device();
                            // 更新设备列表
                            device.setIp(request.getAddress().toString());
                            device.setName(request.getAddress().toString());
                            OnUdpReceive(device);
                        }
                    }
                }
            };
        }
        ProcessingUdpRequestTimer.schedule(receiveRequestTask, 100, 200);//在100毫秒后开始执行，每200毫秒执行一次，300毫秒处理一次队列
    }

    private void startUdpTh() {
        //初始化UDP搜索的线程,暂时不启动
        thUdp = new Thread(new Runnable() {
            @Override
            public void run() {
                threadSearch();
            }
        });
    }

    /**
     * 接收UDP返回的数据
     * 只接受2秒以内返回的数据
     *
     * @param socket
     */
    private void thUdpReceive(DatagramSocket socket) {
        // 接收数据
        DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
        while (true) {
            try {
                socket.receive(response);
                synchronized (NetworkHelp.class) {
                    if (udpRequestList == null)
                        udpRequestList = new ArrayList<>();
                    udpRequestList.add(response);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * UDP搜索设备
     */
    private void threadSearch() {
        //广播地址
//        String broadcastAddr = NetUtil.getBroadcastAddr();
        String broadcastAddr = "127.0.0.1";
        String myIp = NetUtil.getIp();
        try (DatagramSocket socket = new DatagramSocket(0)) { //这里的端口是指本机的端口，0表示任意端口
            //socket.setSoTimeout(receiveTimeOut); //对该socket的接收方法设置超时间
            InetAddress host = InetAddress.getByName(broadcastAddr);
            String msg = "Helo I am client!";//要发送的内容
            byte[] data = msg.getBytes("utf-8");
            DatagramPacket request = new DatagramPacket(data, data.length, host, udpPort); //这里的端口是指服务端的端口
            socket.send(request);//发送数据
            thUdpReceive(socket); //接收两秒钟的数据
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 停止搜索设备
     */
    public void stopSearchDevice() {
        thUdp.interrupt();//停止Udp的线程
        thUdp = null;
        //停止搜索
        if (ProcessingUdpRequestTimer != null) {
            ProcessingUdpRequestTimer.cancel();
            ProcessingUdpRequestTimer = null;
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (receiveRequestTask != null) {
            receiveRequestTask.cancel();
            receiveRequestTask = null;
        }
    }

    /**
     * 初始化TCP
     *
     * @throws IOException
     */
    private void initTcpService() throws IOException {
        tcpSocket = new ServerSocket(0);
        //TODO 初始化TCP服务
    }
}
