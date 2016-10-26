package com.example.libinbin.tabpageindicator;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.libinbin.tabpageindicator.view.TabPageAdapter;
import com.example.libinbin.tabpageindicator.view.TabPageIndicator;

public class MainActivity extends FragmentActivity {

    private String[] titles={"上衣","裤子","衬衫","鞋子","外衣","背包","箱子","皮鞋","牛仔","鞋子","外衣","背包","箱子","皮鞋","牛仔"};

    private ViewPager mViewPager;
    private TabPageIndicator mIndicator;
    private int mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        updateUI();
    }

    private void initData() {
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth=metrics.widthPixels;
    }

    private void updateUI() {
        TestAdapter testAdapter = new TestAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(testAdapter);
        mIndicator.setViewPager(mViewPager,testAdapter);
    }

    private void initView() {
        mViewPager= (ViewPager) findViewById(R.id.vp);
        mIndicator= (TabPageIndicator) findViewById(R.id.indicator);
    }

    public class TestAdapter extends FragmentPagerAdapter implements TabPageAdapter{


        ViewGroup.MarginLayoutParams layoutParams;
        //这里的宽高要根据屏幕宽度决定

        public TestAdapter(FragmentManager fm) {
            super(fm);

            int windowsWidth = MainActivity.this.getWindowManager().getDefaultDisplay().getWidth();
            int itemWidth = (int) (windowsWidth*100f/375);
            int itemHeigh = (int) (windowsWidth*122f/375);

            layoutParams = new ViewGroup.MarginLayoutParams(itemWidth,itemHeigh);
        }

        @Override
        public Fragment getItem(int position) {
            return TestFragment.newPage(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public View getTabIndicatorItem(Context context, int position) {

            View view = View.inflate(context, R.layout.xxtt_toutiao_study_mark_item_view, null);

            view.setLayoutParams(layoutParams);

            ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);

            ivIcon.setImageResource(R.mipmap.ic_launcher);
            tvName.setText(getPageTitle(position));
            return view;
        }
    }


}
