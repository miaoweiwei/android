package com.shnuedu.customControl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 自定义的 ViewPager可以设置是否可以左右滑动
 * 还可以设置切换页面的时候是否要有滑动的动画
 */
public class CustomViewPager extends ViewPager {
    private boolean isCanScroll = true;
    private boolean isScrollAnimation = true;

    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    //region  设置其是否能滑动换页的相关代码

    /**
     * 设置其是否能滑动换页
     *
     * @param isCanScroll false 不能换页， true 可以滑动换页
     */
    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onTouchEvent(ev);
    }
    //endregion

    //region 设置切换页面的时候是否有动画的相关代码

    /**
     * 设置切换页面时是否有动画
     *
     * @param isScrollAnimation
     */
    public void setNoScrollAnimation(boolean isScrollAnimation) {
        this.isScrollAnimation = isScrollAnimation;
    }

    @Override
    public void setCurrentItem(int item) {
        //super.setCurrentItem(item);
        //false 去除滚动效果
        //super.setCurrentItem(item, false);
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }
    //endregion
}
