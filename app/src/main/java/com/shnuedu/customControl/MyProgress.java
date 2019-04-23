package com.shnuedu.customControl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * Created by Amarao on 2018/6/23.
 */

public class MyProgress extends View {
    /*
     * 这个View分为背景，灰度圆圈，进度圆弧，文字
     * */
    private int mWidth;
    private int mHeihgt;

    private Paint grayArcPaint, proArcPaint, textPaint;

    //region 相关背景颜色设置
    public void setCircleBackgroundColor(int circleBackgroundColor) {
        this.circleBackgroundColor = circleBackgroundColor;
    }

    public void setArcBackgroundColor(int arcBackgroundColor) {
        this.arcBackgroundColor = arcBackgroundColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    //endregion

    private int circleBackgroundColor = ContextCompat.getColor(getContext(), android.R.color.darker_gray);//圆圈背景色
    private int arcBackgroundColor = ContextCompat.getColor(getContext(), android.R.color.holo_blue_light);//圆弧背景色
    private int textColor = ContextCompat.getColor(getContext(), android.R.color.darker_gray);//中间字体的颜色

    private float mCircleX;//圆心坐标
    private float mCircleY;//圆心坐标
    private float mLineWidth;//画笔宽度


    //    private float mCircleXY;         //圆心坐标
    private RectF mArcRectF;         //定义的圆弧的形状和大小的范围
    private float mSweepValue = 25;  //当前进度百分比0-100
    private float mSweepAngle;       //圆弧扫过的角度，顺时针方向，单位为度
    private String proText;
    private float showTextSize = 15;//字体的默认大小

    public MyProgress(Context context) {
        super(context);
    }

    public MyProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);// 防止onDraw方法不执行
    }

    //第一步，我们需要知道绘制图形的大小，这个过程在OnMeasure中进行,如果不重写OnMeasure方法，自定义的View会默认充满父布局
    //首先重写onMeasure，按住ctrl点击super.onMeasure查看，发现其实是调用的setMeasuredDimension(mWidth,mHeihgt);方法，此方法将测量的宽高穿进去从而完成测量工作
    //所以重写onMeasure方法，就是把参数传给setMeasuredDimension
    //对宽高重新进行定义
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //我们调用自己的自定义方法measureSize对宽高重新定义，参数宽和高是MeasureSpec对象
        //MeasureSpec对象有两个常用方法
        //MeasureSpec.getMode(measureSpec)  得到测量模式
        //MeasureSpec.getSize(measureSpec)  得到测量大小
        mWidth = measureSize(widthMeasureSpec);
        mHeihgt = measureSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeihgt);
    }

    //我们通过测量模式，给出不同的测量值
    //当specMode = EXACTLY时，直接指定specSize即可
    //当specMode != EXACTLY时，需要指出默认大小
    //当specMode = AT_MOST时，即指定了wrap_content属性时，需要取出我们指定大小和specSize中最小的一个为最后测量值
    private int measureSize(int measureSpec) {
        int result = 0;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 50;   //指定默认大小
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    public float getShowTextSize() {
        return showTextSize;
    }

    public void initView() {
        //设置圆的参数
        float length = Math.min(mWidth, mHeihgt);  //宽高的中较大的一个 //自定义View的宽度
        //定义的圆弧的形状和大小的范围
        // 圆心坐标
        mCircleX = mWidth / 2f;
        mCircleY = mHeihgt / 2f;
        //画圆的时候与矩形相切的部分有一半的圆边框宽不显示，所以半径要减去边框宽度的一半
        float radius = length / 2 - mLineWidth * 0.5f;

        //设置弧线
        /*
         * 参数：
         * left ：  矩形左边的X坐标
         * top ：   矩形顶部的Y坐标
         * right :  矩形右边的X坐标 = left + width
         * bottom： 矩形底部的Y坐标 = top + height
         *
         * (left,top)w为左上角坐标 (right,bottom)为右下角坐标
         * */
        mArcRectF = new RectF(
                (float) (mCircleX - radius),
                (float) (mCircleY - radius),
                (float) (mCircleX + radius),
                (float) (mCircleY + radius));

        //扫描过的角度，除以100表示圆圈进度为0-100，乘以360表示1%圆代表的度数
        mSweepAngle = (mSweepValue / 100f) * 360f;
        //设置文字信息,设置当前进度百分比
        proText = String.valueOf((int) mSweepValue) + "%";

        mLineWidth = length * 0.1f;//画笔宽度
        //圆圈画笔
        grayArcPaint = new Paint();      //画笔
        grayArcPaint.setAntiAlias(true); //平滑
        grayArcPaint.setColor(circleBackgroundColor);
        grayArcPaint.setStyle(Paint.Style.STROKE);   //空心
        grayArcPaint.setStrokeWidth(mLineWidth);   //画笔宽度
        //弧度画笔
        proArcPaint = new Paint();          //画笔
        proArcPaint.setAntiAlias(true);     //平滑
        proArcPaint.setColor(arcBackgroundColor);
        proArcPaint.setStyle(Paint.Style.STROKE);   //空心
        proArcPaint.setStrokeWidth(mLineWidth);   //画笔宽度
        //文字的画笔
        textPaint = new Paint();
        textPaint.setTextSize(showTextSize);         //文字尺寸
        textPaint.setTextAlign(Paint.Align.CENTER); //文字居中
        textPaint.setColor(textColor);
        textPaint.setStrokeWidth(mLineWidth);   //画笔宽度
        //this.invalidate();//刷新View，在UI线程中进行
        System.gc();
    }

    //绘制图像
    // 一般来说，对于绘图时，要在父类方法super.onDraw(canvas)前，重新调用画笔的设置等，这样才可以得到重绘效果
    // 比如画笔初始化函数放在onMeasure中
    // 当你在activity中要设置进度动态更新，文字重绘等方法，必须要重新调用myProgress.onMeasure()方法，自定义的控件才会更新
    // 但如果你画笔初始化函数放在onDraw中，要进行View的更新只需要调用myProgress.invalidate()方法即可，
    // 举个我写程序的错误
    // 当时我就把画笔初始化函数放在onMeasure方法中，结果我在activity中设置进度条以及文本更新时，发现自定义控件并没有变化，
    // 只有我在设置文字后调用myProgress.onMeasure()方法，控件才会更新
    // 此时我发现其实是我画笔初始化函数放置的位置不对，其实画笔初始化函数放在不同的地方只要使用相应的函数调用对应的方法就可以
    // 即画笔初始化函数放在onMeasure中，调用myProgress.onMeasure()方法
    // 即画笔初始化函数放在onDraw中，调用myProgress.invalidate()方法，此时也可以不调用
    @Override
    protected void onDraw(Canvas canvas) {
        //初始化画笔风格，图形参数，如圆圈的颜色，绘制的文字等
        initView();
        super.onDraw(canvas);       //父类方法
        //设置背景
        //setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAccent));
        //绘制弧线
        /**
         * 参数
         * mArcRectF : 指定圆弧的外轮廓矩形区域。
         * startAngle: 圆弧起始角度，单位为度。
         * sweepAngle: 圆弧扫过的角度，顺时针方向，单位为度。
         * useCenter: 如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形。
         * paint: 绘制圆弧的画板属性，如颜色，是否填充等。
         */
        //度数为顺时针：圆的最右边为0，顺时针依次偏移，正下方为90，正上方为-90或270度
        //绘制为顺时针画图
        //背景圆弧
        canvas.drawArc(mArcRectF, 0, 360, false, grayArcPaint);
        //进度圆弧
        canvas.drawArc(mArcRectF, -90, mSweepAngle, false, proArcPaint);
        //绘制文字
        /**
         * 参数
         * text :   文本
         * start :  要绘制的文本中第一个字符的索引
         * end :    要绘制的文本中最后一个字符的索引
         * x :      所绘制文本原点的x坐标
         * y :      所绘制文本的基线的y坐标
         * paint :  画笔，控制文字风格
         */
        /*
        这是一个确定圆心的十字架，绘图时作比较用的
        textPaint.setStrokeWidth(1f);   //画笔宽度
        canvas.drawLine(0,mHeihgt/2,mWidth,mHeihgt/2,textPaint);
        canvas.drawLine(mWidth/2,0,mWidth/2,mHeihgt,textPaint);*/

        //确定文字基线在圆心
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        //粗略计算：基线位置约等于mCircleXY + halfTextHeight
        float halfTextHeight = Math.abs(fm.bottom - fm.top) / 4;
        canvas.drawText(proText, 0, proText.length(), mCircleX, mCircleY + halfTextHeight, textPaint);
        //调用父类方法后，实现自己的逻辑
        canvas.restore();            //合并图像
        this.invalidate();//刷新View，在UI线程中进行
    }

    //设置不同弧度的状态值
    public void setSweepValue(float sweepValue) {
        if (sweepValue < 0) {
            mSweepValue = 0;
        } else if (sweepValue >= 0 && sweepValue <= 100) {
            mSweepValue = sweepValue;
        } else {
            mSweepValue = 100;
        }
        //通知View重绘
        this.invalidate();
    }

    //设置字体大小
    public void setShowTextSize(float proTextSize) {
        this.showTextSize = proTextSize;
    }
}