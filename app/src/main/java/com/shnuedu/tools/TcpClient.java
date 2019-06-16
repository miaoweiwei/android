package com.shnuedu.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TCP通信
 */
public class TcpClient extends Thread {
    private String host;
    private int port;
    private Socket tcpSoccket = null;
    //定义Udp接收事件
    private TcpClient.OnTcpReceiveListener mListener;

    public interface OnTcpReceiveListener {
        void onTcpReceive(String msg);
    }

    /**
     * 用于事件的绑定
     *
     * @param listener
     */
    public void onAttach(TcpClient.OnTcpReceiveListener listener) {
        if (listener != null) {
            mListener = listener;
        } else {
            throw new RuntimeException(listener.toString() + " must implement TcpClient.OnTcpReceiveListener");
        }
    }

    private void OnTcpReceive(String msg) {
        if (mListener != null) {
            mListener.onTcpReceive(msg);
        }
    }

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