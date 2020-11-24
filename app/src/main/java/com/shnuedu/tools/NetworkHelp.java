package com.shnuedu.tools;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkHelp extends Thread {
    private static final int udpPort = 777;//执行搜索设备的udp 的端口 ESP8266 的端口
    private static final int tcpPort = 888;//执行控制设备的tcp 的端口 ESP8266 的端口
    private static final int heartRate = 10000; //心跳时间隔

    private UdpClient udpClient = null;
    private TcpClient tcpClient = null;
    private boolean isConnect = false;
    private Device device;
    private Gson gson = new Gson();

    //定义Udp接收事件
    private List<OnNetReceiveListener> mListenerList;

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

    //region 网络消息接收接口和有关函数

    /**
     * 用于事件的绑定
     *
     * @param listener
     */
    public void addOnNetReceiveListener(NetworkHelp.OnNetReceiveListener listener) {
        if (listener != null) {
            if (mListenerList == null)
                mListenerList = new ArrayList<>();
            mListenerList.add(listener);
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * 用取消绑定的事件
     *
     * @param listener
     */
    public void removeOnNetReceiveListener(NetworkHelp.OnNetReceiveListener listener) {
        if (mListenerList != null && listener != null) {
            if (mListenerList.contains(listener))
                mListenerList.remove(listener);
        } else {
            throw new NullPointerException();
        }
    }

    // 这里使用到啦观察者模式，让所有的需要接收网络信息的类都实现这个接口也就是作为观察者。当前的类为被观察者，当收到网络请求就通知所有的观察者
    public interface OnNetReceiveListener {
        void onUdpReceiveListener(Gson gson, NetMessage netMessage);

        void onTcpReceiveListener(Gson gson, NetMessage netMessage);
    }

    private void OnUdpReceive(String msg) {
        if (msg == null || msg.equals("")) return;
        NetMessage netMessage = gson.fromJson(msg, NetMessage.class);
        if (mListenerList != null) {
            for (int i = 0; i < mListenerList.size(); i++) {
                mListenerList.get(i).onUdpReceiveListener(gson, netMessage);
            }
        }
    }

    private void OnTcpReceive(String msg) {
        System.out.println(msg);
        if (msg == null || msg.equals("") || msg.equals("*")) return;
        NetMessage netMessage = gson.fromJson(msg, NetMessage.class);
        if (mListenerList != null) {
            for (int i = 0; i < mListenerList.size(); i++) {
                mListenerList.get(i).onTcpReceiveListener(gson, netMessage);
            }
        }
    }
    //endregion

    //region 搜索设备相关的函数

    /**
     * 是否正在搜索设备
     *
     * @return
     */
    public boolean isSearchDevice() {
        if (udpClient != null && !udpClient.isInterrupted()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 开启设备搜索
     */
    public void startSearchDevice() {
        udpClient = new UdpClient(udpPort, getUdpSearchMsg());
        udpClient.startSearchDevice();
    }

    /**
     * 停止搜索设备
     */
    public void stopSearchDevice() {
        udpClient.interrupt();//停止Udp的线程
        udpClient.stopSearchDevice();
        udpClient = null;
    }

    //获取搜索设备是广播的消息
    private String getUdpSearchMsg() {
        NetMessage msg = new NetMessage();
        msg.MsgId = Message.SearchDevice_MsgId;
        msg.MsgStatus = true;
        msg.MsgObj = "Mobile";
        return gson.toJson(msg);
    }
    //endregion

    /**
     * 获取当前连接的设备
     *
     * @return
     */
    public Device getCurrentConnectDevice() {
        if (isConnect)
            return this.device;
        else
            return null;
    }

    /**
     * 连接设备,连接之前需要判断是否已经连接
     *
     * @param device
     */
    public void connectDevice(Device device) {
        this.device = device;
        tcpClient = new TcpClient(device.Ip, tcpPort);
        tcpClient.start();
        this.isConnect = true;
    }

    /**
     * 关闭当前连接
     */
    public void closeTcpConnect() {
        if (isConnect) {
            sendMessageToDevice(getCloseConnectMsg());
            try {
                Thread.sleep(300); // 发送完关闭TCP的命令后休眠100毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tcpClient.Disable();
            tcpClient.interrupt();
            tcpClient = null;
            this.isConnect = false;
        }
    }

    //发送TCP命令
    public void sendMessageToDevice(int msgId, boolean msgStatus, Object msgObj) {
        NetMessage msg = new NetMessage();
        msg.MsgId = msgId;
        msg.MsgStatus = msgStatus;
        msg.MsgObj = msgObj;
        sendMessageToDevice(msg);
    }

    //发送TCP命令
    public void sendMessageToDevice(NetMessage msg) {
        if (!this.isConnect)
            return;
        if (tcpClient == null || tcpClient.isInterrupted()) { //Tcp被异常关闭尝试重连
            tcpClient = new TcpClient(device.Ip, tcpPort);
        }
        tcpClient.sendMessage(gson.toJson(msg));
    }

    //获取关闭TCP连接的消息
    private NetMessage getCloseConnectMsg() {
        NetMessage msg = new NetMessage();
        msg.MsgId = Message.CloseTcp_MsgId;
        msg.MsgStatus = true;
        msg.MsgObj = Message.CloseTcp_Msg;
        return msg;
    }

    //扫描wifi
    public void scanWifi() {
        NetMessage msg = new NetMessage();
        msg.MsgId = Message.ScanWifi_MsgId;
        msg.MsgStatus = true;
        msg.MsgObj = "scan";
        sendMessageToDevice(msg);
    }

    //命令设备选择WiFi
    public void connectWifi(String ssid, String password) {
        NetMessage msg = new NetMessage();
        msg.MsgId = 03;
        msg.MsgStatus = true;
        HashMap<String, String> wifiDic = new HashMap<>();
        wifiDic.put("Ssid", ssid);
        wifiDic.put("Password", password);
        msg.MsgObj = wifiDic;
        sendMessageToDevice(msg);
    }


    //发送控制命令
    //  up/down/right/left/core
    public void sendCommand(String command) {
        NetMessage msg = new NetMessage();
        msg.MsgId = 05;
        msg.MsgStatus = true;
        msg.MsgObj = command;
        sendMessageToDevice(msg);
    }

    /**
     * TCP通信
     */
    class TcpClient extends Thread {
        private String host;
        private int port;
        private Socket tcpSocket = null;
        private String message = "";
        private OutputStream os = null;
        private Object TcpSendSyncRoot = new Object();

        private Thread sendThread = null;

        private Timer timer = null;
        private TimerTask task = null;

        public TcpClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                //需要服务器的IP地址和端口号，才能获得正确的Socket对象
//                tcpSocket = new Socket(this.host, this.port);
                tcpSocket = new Socket();
                String ip = NetUtil.getIp();
                SocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
                tcpSocket.connect(socketAddress, 10000);
                initSendThread();
                initHeartbeat();
                // 读Sock里面的数据
                InputStream s = tcpSocket.getInputStream();
                byte[] buf = new byte[4096];
                int len;
                String msg;
                while ((len = s.read(buf)) != -1) {
                    msg = new String(buf, 0, len);
                    System.out.print(msg);
                    OnTcpReceive(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                isConnect = false;
            }
        }

        /**
         * 发送信息
         *
         * @param msg
         */
        public void sendMessage(String msg) {
            synchronized (TcpSendSyncRoot) {
                message = msg;
            }
        }

        /**
         * 发送线程初始化
         */
        private void initSendThread() {
            sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (message != null && !message.equals("")) {
                            try {
                                if (os == null)
                                    os = tcpSocket.getOutputStream();
                                os.write(("" + message).getBytes());
                                os.flush();
                                message = "";
                            } catch (Exception e) {
                                e.printStackTrace();
                                isConnect = false;
                            }
                        }
                    }
                }
            });
            sendThread.start();
        }

        /**
         * 初始化心跳
         */
        private void initHeartbeat() {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    if (isConnect)
                        sendMessage("*");
                }
            };
            timer.schedule(task, heartRate, heartRate);
        }

        /**
         * 释放资源
         */
        public void Disable() {
            try {
                task.cancel();
                timer.cancel();
                sendThread.interrupt();
                os.close();
                tcpSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            task = null;
            task = null;
            sendThread = null;
            os = null;
            tcpSocket = null;
        }
    }

    class UdpClient extends Thread {
        private int udpPort;
        private List<DatagramPacket> udpRequestList = null;//UDP 请求返回的数据列表
        private Timer processUdpTimer = null;
        private TimerTask processUdpTask = null;
        private Object UdpProcessSyncRoot = new Object();
        private Object UdpReceiveSyncRoot = new Object();

        private String msg = "Mobile";//要发送的内容 设备搜索命令

        public UdpClient(int port, String msg) {
            udpPort = port;
            this.msg = msg;
        }

        /**
         * 开启设备搜索
         */
        public void startSearchDevice() {
            if (udpRequestList != null) udpRequestList.clear();
            if (processUdpTimer == null)
                processUdpTimer = new Timer();
            //处理udp返回的数据队列，添加新的设备
            if (processUdpTask == null) processUdpTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (UdpProcessSyncRoot) {
                        if (udpRequestList == null || udpRequestList.size() == 0) return;
                        while (udpRequestList.size() > 0) {
                            DatagramPacket request = udpRequestList.remove(0);//从队列中取出一个接收到的信息
                            try {
                                String result = new String(request.getData(), 0, request.getLength(), "UTF-8");
                                //InetAddress adder = request.getAddress();
                                // assert 如果[boolean表达式]为true，则程序继续执行。
                                //如果为false，则程序抛出AssertionError，并终止执行。
                                //assert adder != null;
                                OnUdpReceive(result); // 通知UdpReceive事件 用于更新设备列表
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            processUdpTimer.schedule(processUdpTask, 200, 200);//在100毫秒后开始执行，每200毫秒执行一次，300毫秒处理一次队列
            this.start();//开始搜索
        }

        /**
         * 停止搜索设备
         */
        public void stopSearchDevice() {
            //停止搜索
            if (processUdpTimer != null) {
                processUdpTimer.cancel();
                processUdpTimer = null;
            }
            try {
                Thread.sleep(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (processUdpTask != null) {
                processUdpTask.cancel();
                processUdpTask = null;
            }
        }

        @Override
        public void run() {
            super.run();
            String broadcastAdder = NetUtil.getBroadcastAddress();//广播地址
            try (DatagramSocket socket = new DatagramSocket(0)) { //这里的端口是指本机的端口，0表示任意端口
                InetAddress host = InetAddress.getByName(broadcastAdder);
                byte[] data = msg.getBytes("utf-8");
                DatagramPacket request = new DatagramPacket(data, data.length, host, udpPort); //这里的端口是指服务端的端口
                socket.send(request);//发送数据
                // 接收数据
                while (true) {
                    DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(response);
                    synchronized (UdpReceiveSyncRoot) {
                        if (udpRequestList == null)
                            udpRequestList = new ArrayList<>();
                        udpRequestList.add(response);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
