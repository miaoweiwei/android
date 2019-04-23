package com.shnuedu.goodmother;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.shnuedu.customControl.CustomViewPager;
import com.shnuedu.customControl.MyFragmentPagerAdapter;
import com.shnuedu.customControl.RoundProgress;
import com.shnuedu.fragmentpage.DeviceFragment;
import com.shnuedu.fragmentpage.FeaturesFragment;
import com.shnuedu.fragmentpage.SettingFragment;
import com.shnuedu.fragmentpage.StatisticsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        FeaturesFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener,
        DeviceFragment.OnFragmentInteractionListener {
    /**
     * 标题
     */
    private TextView mTitle;
    private ImageView titleConnectImage;
    private RoundProgress surplusBatteryRoundProgress;//剩余电量的圆形进度条

    private CustomViewPager mViewPager;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置没有标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //自定义标题
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        //设置标题为某个layout
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitlebar);
        mTitle = findViewById(R.id.custom_title_tv_id);
        titleConnectImage = findViewById(R.id.connect_state_iv_id);
        surplusBatteryRoundProgress = (RoundProgress) findViewById(R.id.surplusBattery_rp_id);
        surplusBatteryRoundProgress.setSweepValue(80);
        surplusBatteryRoundProgress.setShowTextSize(20f);
        initView();
    }

    private void initView() {
        // 绑定id
        mViewPager = findViewById(R.id.fragment_cvp_id);
        navigation = findViewById(R.id.navigation_id);
        // 注册监听 按钮点击事件
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //初始化页面
        mFragments = new ArrayList<>();
        mFragments.add(FeaturesFragment.newInstance("功能", "功能"));
        mFragments.add(StatisticsFragment.newInstance("统计", "统计"));
        mFragments.add(SettingFragment.newInstance("设置"));
        mFragments.add(DeviceFragment.newInstance("设备"));
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);

        // 注册监听 按钮点击事件
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mViewPager.setScanScroll(false);
        mViewPager.setNoScrollAnimation(false);

        mViewPager.setCurrentItem(2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    //region 底部导航视图 按钮点击事件 切换页面
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            boolean flag = true;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mViewPager.setCurrentItem(0);
                    titleConnectImage.setBackground(getResources().getDrawable(R.mipmap.wifi_3));
                    titleConnectImage.setVisibility(View.VISIBLE);//表示显示
                    break;
                case R.id.navigation_dashboard:
                    mViewPager.setCurrentItem(1);
//                    titleConnectImage.setVisibility(View.INVISIBLE);//表示隐藏
                    titleConnectImage.setVisibility(View.GONE);//表示view不存在,在该项目中与上一句的作用一样
                    break;
                case R.id.navigation_setting:
                    mViewPager.setCurrentItem(2);
                    titleConnectImage.setBackground(getResources().getDrawable(R.mipmap.wifi_3));
                    titleConnectImage.setVisibility(View.VISIBLE);//表示显示
                    break;
                default:
                    flag = false;
                    break;
            }
            setmTitleText(item.getTitle().toString());
            return flag;
        }
    };
    //endregion

    private CustomViewPager.OnPageChangeListener mPageChangeListener = new CustomViewPager.OnPageChangeListener() {
        /**
         * 在哪一个页面开始滑动
         * @param i
         * @param v
         * @param i1
         */
        @Override
        public void onPageScrolled(int i, float v, int i1) {
            if (i < 3) {
                System.out.println(String.format("由页面 %s 开始滑动", navigation.getMenu().getItem(i).getTitle()));
            }
        }

        /**
         * 滑动到哪个页面
         * @param i
         */
        @Override
        public void onPageSelected(int i) {
            if (i < 3) {
                navigation.setSelectedItemId(navigation.getMenu().getItem(i).getItemId());
                System.out.println(String.format("滑动到 %s 页面", navigation.getMenu().getItem(i).getTitle()));
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private void setmTitleText(String titleText) {
        mTitle.setText(titleText);
    }

    @Override
    public void onFeaturesFragmentInteraction(Uri uri) {

    }

    @Override
    public void onStatisticFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettingFragmentInteraction(String args) {
        switch (args) {
            case "吸乳器":
                mViewPager.setCurrentItem(3);
                break;
            case "使用说明":
                break;
            case "常见问题":
                break;
        }
    }

    @Override
    public void onDeviceFragmentInteraction(Uri uri) {

    }
}
