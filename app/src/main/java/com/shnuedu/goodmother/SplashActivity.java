package com.shnuedu.goodmother;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import static android.os.SystemClock.sleep;

public class SplashActivity extends AppCompatActivity {
    private static final int WHAT_DELAY = 0x11;// 启动页的延时跳转
    private static final int DELAY_TIME = 2000;// 延时时间
    //创建Handler对象，处理接收的消息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            System.out.println("handleMessage:" + Thread.currentThread().getId());
            switch (msg.what) {
                case WHAT_DELAY:// 延时3秒跳转
                    System.out.println("延时3秒跳转:" + Thread.currentThread().getId());
                    goHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 调用handler的sendEmptyMessageDelayed方法
        // 第一个参数类型是int，表示是什么消息；第二个参数的类型是long，表示消息发送延迟时间。
        //start2();
        // System.out.println("onCreate，handleMessage:" + Thread.currentThread().getId());
        start1();
    }

    /**
     * 启动页方式一
     */
    private void start2() {
        handler.sendEmptyMessageDelayed(WHAT_DELAY, DELAY_TIME);
    }

    /**
     * 启动页方式二
     */
    private void start1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(1000);//模拟耗时的任务
                //耗时任务，比如加载网络数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 这里可以睡几秒钟，如果要放广告的话
                        // sleep(3000);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }
                });
            }
        }).start();
    }

    /**
     * 跳转到主页面
     */
    private void goHome() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();// 销毁当前活动界面
    }
}
