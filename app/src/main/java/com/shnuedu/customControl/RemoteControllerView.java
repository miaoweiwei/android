package com.shnuedu.customControl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class RemoteControllerView extends View {

    //region 变量
    private static final float DEF_VIEW_SIZE = 300;

    private float mCoreX;//中心点的坐标X
    private float mCoreY;//中心点的坐标Y
    private List<RoundMenu> roundMenus;//菜单列表
    private boolean isCoreMenu = false;//是否有中心按钮
    private int coreMenuColor;//中心按钮的默认背景--最好不要透明色
    private int coreMenuStrokeColor;//中心按钮描边颜色
    private int coreMenuStrokeSize;//中心按钮描边粗细
    private int coreMenuSelectColor;//中心按钮选中时的背景颜色
    private Bitmap coreBitmap;//OK图片
    private OnClickListener onCoreClickListener;//中心按钮的点击回调

    private float deviationDegree;//偏移角度

    private int onClickState = -2;//-2是无点击，-1是点击中心圆，其他是点击菜单
    private int roundRadius;//中心圆的半径
    private double radiusDistance;//半径的长度比（中心圆半径=大圆半径*radiusDistance）
//    private long touchTime;//按下时间，抬起的时候判定一下，超过300毫秒算点击
    //endregion

    public RemoteControllerView(Context context) {
        super(context);
    }

    public RemoteControllerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteControllerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCoreX = getWidth() / 2f;
        mCoreY = getHeight() / 2f;
        float length = Math.min(getWidth(), getHeight());   //大圆所在的正方形边长
        float outerRadius;

        if (roundMenus != null && roundMenus.size() > 0) {
            float mLineWidth = 0f;
            for (RoundMenu r : roundMenus) {
                if (r.strokeSize > mLineWidth) {    //找出最大的那个边长
                    mLineWidth = r.strokeSize;
                }
            }
            outerRadius = length / 2 - mLineWidth * 0.5f;
            RectF rect = new RectF(
                    (float) (mCoreX - outerRadius),
                    (float) (mCoreX - outerRadius),
                    (float) (mCoreY + outerRadius),
                    (float) (mCoreY + outerRadius));//大圆的矩形

            float sweepAngle = 360f / roundMenus.size();//每个弧形的角度
            deviationDegree = sweepAngle / 2;//其实的偏移角度，如果4个扇形的时候是X形状，而非+,设为0试试就知道什么意思了
            for (int i = 0; i < roundMenus.size(); i++) {
                RoundMenu roundMenu = roundMenus.get(i);
                //填充
                Paint paint = new Paint();
                paint.setAntiAlias(true);   //抗锯齿
                if (onClickState == i) { //选中 -2是无点击，-1是点击中心圆，其他是点击菜单
                    paint.setColor(roundMenu.selectSolidColor); //设置填充的颜色
                } else { //未选中
                    paint.setColor(roundMenu.solidColor);
                }
                canvas.drawArc(rect, deviationDegree + (i * sweepAngle), sweepAngle, true, paint);
                if (roundMenu.strokeSize > 0) {
                    //画描边
                    paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(roundMenu.strokeSize);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(roundMenu.strokeColor);
                    canvas.drawArc(rect, deviationDegree + (i * sweepAngle), sweepAngle, roundMenu.useCenter, paint);
                }
                //画图案
                Matrix matrix = new Matrix();
                matrix.postTranslate((float) ((mCoreX + getWidth() / 2 * roundMenu.iconDistance) - (roundMenu.icon.getWidth() / 2)), mCoreY - (roundMenu.icon.getHeight() / 2));
                matrix.postRotate(((i + 1) * sweepAngle), mCoreX, mCoreY);
                canvas.drawBitmap(roundMenu.icon, matrix, null);
            }
        }
        //计算中心圆圈半径 中心圆半径=大圆半径*radiusDistance
        roundRadius = (int) (getWidth() / 2 * radiusDistance);
        //画中心圆圈
        if (isCoreMenu) {
            //填充
            RectF rect1 = new RectF(mCoreX - roundRadius, mCoreY - roundRadius, mCoreX + roundRadius, mCoreY + roundRadius);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(coreMenuStrokeSize);
            if (onClickState == -1) {
                paint.setColor(coreMenuSelectColor);
            } else {
                paint.setColor(coreMenuColor);
            }
            canvas.drawArc(rect1, 0, 360, true, paint);

            //画描边
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(coreMenuStrokeSize);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(coreMenuStrokeColor);
            canvas.drawArc(rect1, 0, 360, true, paint);
            if (coreBitmap != null) {
                //画中心圆圈的“OK”图标
                canvas.drawBitmap(coreBitmap, mCoreX - coreBitmap.getWidth() / 2, mCoreY - coreBitmap.getHeight() / 2, null);//在 0，0坐标开始画入src
            }
        }
        canvas.restore();            //合并图像
        this.invalidate();//刷新View，在UI线程中进行
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                touchTime = new Date().getTime();
                onClickState = detectClick(event.getX(), event.getY(), mCoreX, mCoreY, roundMenus.size());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
//                if ((new Date().getTime() - touchTime) < 300) {
//                    //点击小于300毫秒算点击
//                }
                OnClickListener onClickListener = null;
                int clickIndex = detectClick(event.getX(), event.getY(), mCoreX, mCoreY, roundMenus.size());
                if (clickIndex == onClickState) {
                    if (onClickState == -1) {
                        onClickListener = onCoreClickListener;
                    } else if (onClickState >= 0 && onClickState < roundMenus.size()) {
                        onClickListener = roundMenus.get(onClickState).onClickListener;
                    }
                }

                if (onClickListener != null) {
                    onClickListener.onClick(this);
                }
                onClickState = -2;
                invalidate();
                break;
        }
        return true;
    }

    /***
     * 判断点击了哪一个扇形，返回索引
     * @param clickX
     * @param clickY
     * @return
     */
    private int detectClick(float clickX, float clickY, float coreX, float coreY, int roundMenuCount) {
        int distanceLine = (int) getDisForTwoSpot(coreX, coreY, clickX, clickY);//距离中心点之间的直线距离
        int clickIndex = -2;
        if (distanceLine <= roundRadius) {
            //点击的是中心圆；按下点到中心点的距离小于中心园半径，那就是点击中心园了
            clickIndex = -1;
        } else if (distanceLine <= getWidth() / 2) {
            //点击的是某个扇形；按下点到中心点的距离大于中心圆半径小于大圆半径，那就是点击某个扇形了
            float sweepAngle = 360 / roundMenuCount;//每个弧形的角度
            int angle = getRotationBetweenLines(coreX, coreY, clickX, clickY);
            //这个angle的角度是从正Y轴开始，而我们的扇形是从正X轴开始，再加上偏移角度，所以需要计算一下
            angle = (angle + 360 - 90 - (int) deviationDegree) % 360;
            clickIndex = (int) (angle / sweepAngle);//根据角度得出点击的是那个扇形
        } else {
            //点击了外面
            clickIndex = -2;
        }
        return clickIndex;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize;
        int heightSize;
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources().getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 添加中心菜单按钮
     *
     * @param coreMenuColor
     * @param coreMenuSelectColor
     * @param onClickListener
     */
    public void setCoreMenu(int coreMenuColor, int coreMenuSelectColor, int coreMenuStrokeColor,
                            int coreMenuStrokeSize, double radiusDistance, Bitmap bitmap, OnClickListener onClickListener) {
        isCoreMenu = true;
        this.coreMenuColor = coreMenuColor;
        this.radiusDistance = radiusDistance;
        this.coreMenuSelectColor = coreMenuSelectColor;
        this.coreMenuStrokeColor = coreMenuStrokeColor;
        this.coreMenuStrokeSize = coreMenuStrokeSize;
        coreBitmap = bitmap;
        this.onCoreClickListener = onClickListener;
        invalidate();
    }

    /**
     * 获取两条线的夹角
     *
     * @param centerX
     * @param centerY
     * @param xInView
     * @param yInView
     * @return
     */
    public static int getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        double rotation = 0;
        double k1 = (double) (centerY - centerY) / (centerX * 2 - centerX);
        double k2 = (double) (yInView - centerY) / (xInView - centerX);
        double tmpDegree = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;

        if (xInView > centerX && yInView < centerY) {  //第一象限
            rotation = 90 - tmpDegree;
        } else if (xInView > centerX && yInView > centerY) //第二象限
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) { //第三象限
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) { //第四象限
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }
        return (int) rotation;
    }

    /**
     * 求两个点之间的距离
     *
     * @return
     */
    public static double getDisForTwoSpot(float x1, float y1, float x2, float y2) {
        float width, height;
        if (x1 > x2) {
            width = x1 - x2;
        } else {
            width = x2 - x1;
        }

        if (y1 > y2) {
            height = y2 - y1;
        } else {
            height = y2 - y1;
        }
        return Math.sqrt((width * width) + (height * height));
    }

    /**
     * 添加菜单
     *
     * @param roundMenu
     */
    public void addRoundMenu(RoundMenu roundMenu) {
        if (roundMenu == null) {
            return;
        }
        if (roundMenus == null) {
            roundMenus = new ArrayList<>();
        }
        roundMenus.add(roundMenu);
        invalidate(); //重绘，让新添加的按钮显示在页面上
    }

    /**
     * 获取点击按钮的索引，-2是无点击，-1是点击中心圆，其他是点击菜单
     * @return
     */
    public int getOnClickIndex() {
        return onClickState;
    }

    /**
     * 扇形的对象类
     */
    public static class RoundMenu {
        public boolean useCenter = false;//扇形是否画连接中心点的直线
        public int solidColor = 0x00000000;//背景颜色,默认透明
        public int selectSolidColor = 0x00000000;//背景颜色,默认透明
        public int strokeColor = 0x00000000;//描边颜色,默认透明
        public int strokeSize = 0;//描边的宽度,默认0
        public Bitmap icon;//菜单的图片
        public OnClickListener onClickListener;//点击监听
        public double iconDistance = 0.7;//图标距离中心点的距离
        public Object tag;
    }
}
