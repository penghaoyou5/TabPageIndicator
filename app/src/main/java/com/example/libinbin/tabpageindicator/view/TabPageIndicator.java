package com.example.libinbin.tabpageindicator.view;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import java.lang.reflect.Field;


public class TabPageIndicator extends HorizontalScrollView implements ViewPager.OnPageChangeListener {


    private int mCurrentPosition  = -1;

    private int mTabCount;

    private float mOffset;

    private TabPageAdapter tabPageAdapter;

    /**
     * 判断是否是OnClick主导发生的事件   这样不再返回来影响TabPageIndicator
     */
    private boolean isClick = false;


    /**
     * Horizontal LinearLayout;
     */
    private TabPageCoverFlowLiearLayout mLinearLayout;

    private ViewPager mPager;

    private ViewPager.OnPageChangeListener mListener;


    private OverScroller parentScroller;
    private int scaledTouchSlop;

    public TabPageIndicator(Context context) {
        this(context,null);
    }

    public TabPageIndicator(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public TabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFillViewport(true);//子控件不满全屏时，设置ScrollView 全屏
        setWillNotDraw(false);//取消重绘的Tag ？
        initView(context);

    }

    private void initView(Context context) {

        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mLinearLayout=new TabPageCoverFlowLiearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mLinearLayout.setGravity(Gravity.CENTER_VERTICAL);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int windowsWidth = windowManager.getDefaultDisplay().getWidth()/2;

        mLinearLayout.setPadding(windowsWidth,0,windowsWidth,0);

        addView(mLinearLayout);

        //反射获取 mScroller 来判断滑动是否停止
        Class<HorizontalScrollView> horizontalScrollViewClass = HorizontalScrollView.class;
        try {
            //获取私有字段
            Field mScroller = horizontalScrollViewClass.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            parentScroller = (OverScroller) mScroller.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * Indicator 添加ViewPager 关联
     * @param pager
     */
    public void setViewPager(ViewPager pager, TabPageAdapter adapter){
        this.mPager=pager;
        this.tabPageAdapter = adapter;

        if (mPager!=null) {
//            mPager.setOnPageChangeListener(this);
            mPager.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mPager.setTag(true);
                    mPager.setOnPageChangeListener(TabPageIndicator.this);
                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                    }
                    return false;
                }
            });
        }


        notifyDataSetChange();
    }


    /**
     * 新建Tab加入线性布局里面
     */
    private void notifyDataSetChange() {
        mLinearLayout.removeAllViews();
        PagerAdapter adapter = mPager.getAdapter();
        mTabCount = adapter.getCount();

        for (int i=0;i<mTabCount;i++){
            CharSequence pageTitle = adapter.getPageTitle(i);
            addTab(pageTitle,i);
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                mCurrentPosition = mPager.getCurrentItem();
                mOffset=0;
                scrollToChild();
            }
        });

    }

    private void addTab(CharSequence pageTitle, final int position) {
        View tabView = tabPageAdapter.getTabIndicatorItem(getContext(), position);
        mLinearLayout.addView(tabView,position);
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

//                if(position!=mCurrentPosition){
                    isClick = true;
                    mCurrentPosition=position;
                    mOffset=0;
                    scrollToChild();

//                }


                mPager.setCurrentItem(position);

            }
        });

    }



    /**
     * 在viewPager起作用的时候 本身Scroller不起作用
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        mCurrentPosition = position;
        mOffset = positionOffset;

        /**
         * 下方的两个方法注意顺序
         */
        scrollToChild(true);
    }

    @Override
    public void onPageSelected(int position) {
        if (mListener!=null){
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListener!=null){
            mListener.onPageScrollStateChanged(state);
        }
        if(state== ViewPager.SCROLL_STATE_IDLE){
            mPager.setOnPageChangeListener(null);
            mPager.setTag(false);
        }
    }



    private void scrollToChild(){
        scrollToChild(false);
    }

    /**
     * 滚动向目标位置
     * @param now true 立即滚动 false 默认延时生效
     */
    private void scrollToChild(boolean now) {
        int measuredWidth = this.getMeasuredWidth();
        if(mCurrentPosition==-1)return;
        if(mLinearLayout==null)return;
        View tabView = mLinearLayout.getChildAt(mCurrentPosition);
        if(tabView==null)return;
        int width = tabView.getMeasuredWidth();
        float offset = width * mOffset;
        int left = tabView.getLeft();
        int scrollWitch= (int) (left+ offset+width/2 -measuredWidth/2);



        //如果立即生效
        if(now){
            smoothScrollTo(scrollWitch,0);
            invalidate();
            return;
        }

        int scrollX = getScrollX();
        if(Math.abs(scrollWitch-scrollX)> scaledTouchSlop) {
            parentScroller.startScroll(scrollX, 0, scrollWitch - scrollX, 0, 200);
            invalidate();
        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mLinearLayout!=null&&mCurrentPosition!=-1)
            mLinearLayout.invalidate();


        Object tag = mPager.getTag();
        if(tag!=null){
            boolean ta = (boolean) tag;
            if(ta)
                return;
        }

        if(parentScroller.computeScrollOffset()){
            isStop = false;
        }else if(!isStop){
            //只有从 false 变为true才会调用一次
            isStop = true;

            preScrollTarget();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        isUp = false;
        if(ev.getAction()==MotionEvent.ACTION_UP){
            isUp = true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                preScrollTarget();
                break;
        }
        return super.onTouchEvent(ev);
    }

    boolean isUp =  true;
    boolean isStop = true;

    @Override
    public void fling(int velocityX) {
        //重写fling方法，将速度除以三，减缓其滑动速度
        super.fling(velocityX / 3);
    }


    private int waiIsLast;
    private int neiIsLast;
    /**
     * 判断停止比较难 也为了多一点顺滑 延时操作
     */
    private void preScrollTarget() {
        //控制只执行最后一次     两个变量控制能行
        if (isStop && isUp && mLinearLayout != null) {
            waiIsLast++;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    neiIsLast++;
                    if(neiIsLast>=waiIsLast){
                        scroolTarget();
                    }
                }
            }, 100);
        }
    }

    /**
     * 滑动到指定目标
     */
    private void scroolTarget(){
        if(isStop&&isUp&&mLinearLayout!=null){
            //当时符合条件 并且延时100ms之后也符合条件更加精准

            //现在中心点距离
            int center = getScrollX() + getMeasuredWidth() / 2;

            View targeChildrenView = null;
            int juLi = Integer.MAX_VALUE;

            int childCount = mLinearLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {

                View tabView = mLinearLayout.getChildAt(i);
                if(tabView==null)continue;
                int width = tabView.getMeasuredWidth();
                int left = tabView.getLeft();
                //距离最左侧距离
                int scrollWitch=  (left+width/2);

                int abs = Math.abs(center - scrollWitch);

                if(abs<juLi){
                    juLi = abs;
                    targeChildrenView = tabView;
                }
            }

            if(targeChildrenView!=null){
                targeChildrenView.callOnClick();
            }
        }
    }




}



