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
    private List<Device> deviceList = null;//设备列表
    private List<DatagramPacket> udpRequestList = null;//UDP 请求列表
    private Timer receiveRequestTimer = null;
    private ServerSocket tcpSocket = null;
    private Thread thUdp = null;//执行搜索设备的线程
    private Thread thTcp = null;//执行控制设备的线性，与设备进行通信

    private NetworkHelp() {
        //初始化UDP搜索的线程
        thUdp = new Thread(new Runnable() {
            @Override
            public void run() {
                threadSearch();
            }
        });
    }

    //volatile关键字的一个作用是禁止指令重排，把instance声明为volatile之后，
    // 对它的写操作就会有一个内存屏障（什么是内存屏障？），
    // 这样，在它的赋值完成之前，就不用会调用读操作。
    private static volatile NetworkHelp instance = null;

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

    TimerTask receiveRequestTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (NetworkHelp.class) {
                if (udpRequestList == null || udpRequestList.size() == 0) return;
                while (udpRequestList.size() > 0) {
                    DatagramPacket request = udpRequestList.remove(0);
                    Device device = new Device();
                    // 更新设备列表
                    device.setIp(request.getAddress().toString());
                    device.setName(request.getAddress().toString());
                    addDevice(device);
                }
            }
        }
    };

    //向设备列表里添加一个新的设备，注意线程同步
    private void addDevice(Device device) {
        if (deviceList == null)
            deviceList = new ArrayList<>();
        deviceList.add(device);
    }

    //获取设备列表，用于更新UI，注意线程同步
    public List<Device> getDeviceList() {
        return deviceList;
    }


    private void thUdpReceive(DatagramSocket socket) {
        // 接收数据
        DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
        long begintime = System.nanoTime();
        long endtime;
        long costTime;
        do {
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
            endtime = System.nanoTime();
            costTime = (endtime - begintime) / 1000;//要换算为微秒，就除上1000，就可以
        } while (costTime / 1000 < 2000); //除以1000换算成毫秒,只等待两秒
    }

    //UDP搜索设备
    private void threadSearch() {
        int port = 1818;//ESP8266的端口
        //广播地址
        String broadcastAddr = NetUtil.getBroadcastAddr();
        String myIp = NetUtil.getIp();
        try (DatagramSocket socket = new DatagramSocket(0)) {
            socket.setSoTimeout(10000);
            InetAddress host = InetAddress.getByName(broadcastAddr);
            String msg = "Helo I am client!";//要发送的内容
            byte[] data = msg.getBytes("utf-8");
            DatagramPacket request = new DatagramPacket(data, data.length, host, port);
            while (true) {
                socket.send(request);//发送数据
                thUdpReceive(socket); //接收两秒钟的数据
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //开启设备搜索
    public void startSearchDevice() {
        if (this.deviceList != null) deviceList.clear();
        if (udpRequestList != null) udpRequestList.clear();
        thUdp.start();//开始搜索
        receiveRequestTimer = new Timer();
        receiveRequestTimer.schedule(receiveRequestTask, 100, 200);//在100毫秒后开始执行，每200毫秒执行一次
    }

    //停止搜索设备
    public void stopSearchDevice() {
        thUdp.stop();//停止搜索
        if (receiveRequestTimer != null) {
            receiveRequestTimer.cancel();
            receiveRequestTimer = null;
        }
        if (receiveRequestTask != null) {
            receiveRequestTask.cancel();
            receiveRequestTask = null;
        }
    }


    private void initTcpService() throws IOException {
        tcpSocket = new ServerSocket(0);
        //TODO 初始化TCP服务
    }
}
