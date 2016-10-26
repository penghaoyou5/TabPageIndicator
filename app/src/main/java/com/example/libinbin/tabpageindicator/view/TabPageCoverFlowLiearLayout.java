package com.example.libinbin.tabpageindicator.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.example.libinbin.tabpageindicator.R;


/**
 * Created by bjhl-penghaoyou on 16/10/20.
 */
public class TabPageCoverFlowLiearLayout extends LinearLayout {


    public TabPageCoverFlowLiearLayout(Context context) {
        super(context);
        init();
    }

    public TabPageCoverFlowLiearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TabPageCoverFlowLiearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TabPageCoverFlowLiearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        setWillNotDraw(false);// 防止onDraw方法不执行
        //启用子元素排序
        this.setChildrenDrawingOrderEnabled(true);
    }



    private int parentCenter;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //在绘画之前得到显示的View那个在中间
        //现在中心点距离
        parentCenter = ((View)getParent()).getScrollX() + ((View)getParent()).getMeasuredWidth() / 2;

        int juLi = Integer.MAX_VALUE;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {

            View tabView = getChildAt(i);

            int width = tabView.getMeasuredWidth();
            int left = tabView.getLeft();
            //距离最左侧距离
            int scrollWitch=  (left+width/2);
            int abs = Math.abs(parentCenter - scrollWitch);
            if(abs<juLi){
                juLi = abs;
                centerChild = i;
            }
        }

        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean b = super.drawChild(canvas, child, drawingTime);

        int cen = child.getLeft()+child.getMeasuredWidth()/2;

        //获取父类中心点距离
        int parentScrollX = ((View)getParent()).getScrollX()+ ((View)getParent()).getMeasuredWidth() / 2;

        int abs = Math.abs(cen - parentScrollX);

        if(abs==0)return b;

        float x = 1.0f * abs / 100;

        //现在距离
        double endDis = (-1*Math.pow(x,2) + 8 * x) * 10;

        //移动距离= 原始距离 - 现在距离
        double tracslateDis  = abs - endDis;

        //通过左右判断移动方向
        float distance  = parentScrollX - cen;
        if(distance<0){
            tracslateDis = -tracslateDis;
        }

        //平移
        TranslateAnimation translateAnimation = new TranslateAnimation((float) tracslateDis,(float) tracslateDis,0,0);


        //缩放
        float dou = 1 - (1.0f*abs / child.getWidth()) * 0.2f;

        ScaleAnimation scaleAnimation = new ScaleAnimation(dou, dou,dou, dou,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        AnimationSet animationSet  = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.start();

        //根据比例计算
        int singColor = (int) (0xff - (1-dou)*40);

        //按此算 rgba
        int argb = Color.argb(0xff, singColor, singColor, singColor);

        GradientDrawable shape = (GradientDrawable) getContext().getResources().getDrawable(R.drawable.bg_tabpagecoverflow_6);
        shape.setColor(argb);

        child.setBackgroundDrawable(shape);

        child.setAnimation(animationSet);

        return b;
    }

    private int centerChild=-1;

    /**
     * 重新确定View重绘的顺序
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if(centerChild==-1)
            return super.getChildDrawingOrder(childCount, i);

        int rez;
        //find drawIndex by centerChild
        if (i > centerChild) {
            //below center
            rez = (childCount - 1) - i + centerChild;
        } else if (i == centerChild) {
            //center row
            //draw it last
            rez = childCount - 1;
        } else {
            //above center - draw as always
            // i < centerChild
            rez = i;
        }
        return rez;

    }


}
