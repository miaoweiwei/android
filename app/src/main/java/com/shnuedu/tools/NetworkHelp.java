package com.shnuedu.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkHelp extends Thread {
    TcpClient tcpClient = null;
    private int udpPort = 777;//执行搜索设备的udp 的端口 ESP8266 的端口
    private int tcpPort = 888;//执行控制设备的tcp 的端口 ESP8266 的端口

    private Thread thUdp = null;//执行搜索设备的线程
    private Thread thTcp = null;//执行控制设备的线性，与设备进行通信

    private static Object UdpProcessSyncRoot = new Object();
    private static Object UdpReceiveSyncRoot = new Object();

    private Timer processUdpTimer = null;
    private TimerTask processUdpTask = null;

    Device device;

    private List<DatagramPacket> udpRequestList = null;//UDP 请求返回的数据列表

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
        if (udpRequestList != null) udpRequestList.clear();
        if (processUdpTimer == null)
            processUdpTimer = new Timer();
        if (processUdpTask == null) { //处理udp返回的数据队列，添加新的设备
            processUdpTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (UdpProcessSyncRoot) {
                        if (udpRequestList == null || udpRequestList.size() == 0) return;
                        System.out.println("处理数据！");
                        while (udpRequestList.size() > 0) {
                            DatagramPacket request = udpRequestList.remove(0);//从队列中取出一个接收到的信息
                            Device device = new Device();
                            String result = null;
                            InetAddress addr = null;
                            try {
                                result = new String(request.getData(), 0, request.getLength(), "ASCII");
                                addr = request.getAddress();
                                System.out.println(addr.toString());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            device.setName(result);
                            device.setIp(addr.toString());
                            OnUdpReceive(device); // 通知UdpReceive事件 用于更新设备列表
                        }
                    }
                }
            };
        }
        processUdpTimer.schedule(processUdpTask, 500, 500);//在100毫秒后开始执行，每200毫秒执行一次，300毫秒处理一次队列
        initUdpTh();
        thUdp.start();//开始搜索
    }

    private void initUdpTh() {
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
        while (true) {
            try {
                DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                socket.receive(response);
                synchronized (UdpReceiveSyncRoot) {
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
        String broadcastAddr = NetUtil.getBroadcastAddr();
        //String broadcastAddr = "127.0.0.1";
        //String myIp = NetUtil.getIp();
        try (DatagramSocket socket = new DatagramSocket(0)) { //这里的端口是指本机的端口，0表示任意端口
            //socket.setSoTimeout(receiveTimeOut); //对该socket的接收方法设置超时间
            InetAddress host = InetAddress.getByName(broadcastAddr);
            String msg = "Helo I am client!";//要发送的内容
            byte[] data = msg.getBytes("utf-8");
            DatagramPacket request = new DatagramPacket(data, data.length, host, udpPort); //这里的端口是指服务端的端口
            socket.send(request);//发送数据
            thUdpReceive(socket);
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



    public void connectDevice(Device device) {
        this.device = device;
        tcpClient = new TcpClient(device.getIp(), tcpPort);
    }

    public void sendMessageToDevice(String msg) {
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
        private Socket tcpSoccket = null;

        public TcpClient(String host, int port) {
            this.host = host;
            this.port = port;
            initTcpClient();
        }

        private void initTcpClient() {
            try {
                //需要服务器的IP地址和端口号，才能获得正确的Socket对象
                tcpSoccket = new Socket(host, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                // 读Sock里面的数据
                InputStream s = tcpSoccket.getInputStream();
                byte[] buf = new byte[1024];
                int len;
                String msg = "";
                while ((len = s.read(buf)) != -1) {
                    msg += new String(buf, 0, len);
                }
                OnTcpReceive(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 发送信息
         *
         * @param msg
         */
        public void sendMessage(String msg) {
            OutputStream os = null;
            try {
                os = tcpSoccket.getOutputStream();
                os.write(("" + msg).getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
