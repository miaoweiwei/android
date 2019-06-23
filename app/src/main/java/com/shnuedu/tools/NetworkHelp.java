package com.shnuedu.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
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
    Device device;

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

    /**
     * 用于事件的绑定
     *
     * @param listener
     */
    public void onAttach(NetworkHelp.OnNetReceiveListener listener) {
        if (listener != null) {
            mListener = listener;
        } else {
            throw new RuntimeException(listener.toString() + " must implement OnNetReceiveListener");
        }
    }

    public interface OnNetReceiveListener {
        void onUdpReceiveListener(Device device);

        void onTcpReceiveListener(String msg);
    }

    private void OnUdpReceive(Device device) {
        if (mListener != null) {
            mListener.onUdpReceiveListener(device);
        }
    }

    private void OnTcpReceive(String msg) {
        if (mListener != null) {
            mListener.onTcpReceiveListener(msg);
        }
    }

    /**
     * 开启设备搜索
     */
    public void startSearchDevice() {
        udpClient = new UdpClient(udpPort);
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
        tcpClient = new TcpClient(device.getIp(), tcpPort);
        tcpClient.start();
        this.isConnect = true;
    }

    /**
     * 关闭当前连接
     */
    public void closeConnect() {
        if (isConnect) {
            tcpClient.Disable();
            tcpClient.interrupt();
            tcpClient = null;
            this.isConnect = false;
        }
    }

    public void sendMessageToDevice(String msg) {
        if (!this.isConnect)
            return;
        if (tcpClient == null || tcpClient.isInterrupted()) {
            tcpClient = new TcpClient(device.getIp(), tcpPort);
        }
        tcpClient.sendMessage(msg);
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
                tcpSocket = new Socket(this.host, this.port);
                initHeartbeat();
                initSendThread();
                // 读Sock里面的数据
                InputStream s = tcpSocket.getInputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = s.read(buf)) != -1) {
                    String msg = new String(buf, 0, len);
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
                        if (!(message.equals("") || message == null)) {
                            try {
                                if (os == null)
                                    os = tcpSocket.getOutputStream();
                                os.write(("" + message).getBytes());
                                os.flush();
                                message = "";
                                isConnect = true;
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

        public UdpClient(int port) {
            udpPort = port;
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
                        System.out.println("处理数据！");
                        while (udpRequestList.size() > 0) {
                            DatagramPacket request = udpRequestList.remove(0);//从队列中取出一个接收到的信息
                            try {
                                Device device = new Device();
                                String result = new String(request.getData(), 0, request.getLength(), "UTF-8");
                                device.setName(result);
                                // assert 如果[boolean表达式]为true，则程序继续执行。
                                // assert 如果[boolean表达式]为true，则程序继续执行。
                                //如果为false，则程序抛出AssertionError，并终止执行。
                                InetAddress adder = request.getAddress();
                                assert adder != null;
                                device.setIp(adder.getHostAddress());
                                OnUdpReceive(device); // 通知UdpReceive事件 用于更新设备列表
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
                Thread.sleep(500);
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
            String broadcastAdder = NetUtil.getBroadcastAdder();//广播地址
            try (DatagramSocket socket = new DatagramSocket(0)) { //这里的端口是指本机的端口，0表示任意端口
                InetAddress host = InetAddress.getByName(broadcastAdder);
                String msg = "Mobile";//要发送的内容 设备搜索命令
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
