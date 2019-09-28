package com.bophia.erik.ykxrider.UI.mine.myEvaluation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bophia.erik.ykxrider.utils.MyViewPagerAdapter;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.SetView;

import java.util.ArrayList;
import java.util.List;

/**
 * “我的评价”界面Activity
 */
public class MyEvaluationActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{

    private String[] titles = new String[]{"用户评价","商家评价"};
    private List<Fragment> fragments=new ArrayList<>();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyViewPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //加载页面布局
        setContentView(R.layout.myevaluation_layout);

        //自定义ActionBar
        setCustomActionBar();

        //初始化两个fragment
        initFragment();



    }

    //重写Tableyout方法
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //初始化两个fragment
    private void initFragment(){

        tabLayout = findViewById(R.id.tl_evaluation);
        viewPager = findViewById(R.id.vp_evaluation);

        //设置TabLayout标签的显示方式
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        //循环注入标签
        for (String tab:titles){
            tabLayout.addTab(tabLayout.newTab().setText(tab));
        }

        //设置TabLayout点击事件
        tabLayout.setOnTabSelectedListener(this);

        fragments.clear();

        fragments.add(new UserEvaluationFragment());
        fragments.add(new BusinessEvaluationFragment());

        viewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager(),titles,fragments);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //设置TabLayout下划线宽度
        tabLayout.post(new Runnable() {
            @Override
            public void run() {

                SetView.setIndicator(tabLayout,50,50);
            }
        });

    }

    //设置自定义ActionBar
    private void setCustomActionBar(){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        TextView tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        tv_actionBarTitle.setText("我的评价");

        LinearLayout ll_back = mActionBarView.findViewById(R.id.ll_back);
        ll_back.setVisibility(View.VISIBLE);

        TextView tv_backName = mActionBarView.findViewById(R.id.tv_backName);
        tv_backName.setText("我的");

        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setCustomView(mActionBarView,lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}
