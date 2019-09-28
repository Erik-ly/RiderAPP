package com.bophia.erik.ykxrider.UI.distribution;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bophia.erik.ykxrider.utils.MyViewPagerAdapter;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.SetView;

import java.util.ArrayList;
import java.util.List;

/**
 * “配送”界面Fragment
 */
public class DistributionFragment extends Fragment implements TabLayout.OnTabSelectedListener{

    private String[] titles = new String[]{"待取货","配送中"};
    private List<Fragment> fragments=new ArrayList<>();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyViewPagerAdapter viewPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.distribution_layout,container,false);
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //初始化两个fragment
        initFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

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

        tabLayout = getActivity().findViewById(R.id.tl_distribution);
        viewPager = getActivity().findViewById(R.id.vp_distribution);

        //设置TabLayout标签的显示方式
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        //循环注入标签
        for (String tab:titles){
            tabLayout.addTab(tabLayout.newTab().setText(tab));
        }

        //设置TabLayout点击事件
        tabLayout.setOnTabSelectedListener(this);

        fragments.clear();

        fragments.add(new PickupFragment());
        fragments.add(new DistributingFragment());

        viewPagerAdapter = new MyViewPagerAdapter(getActivity().getSupportFragmentManager(),titles,fragments);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //设置TabLayout下划线宽度
        tabLayout.post(new Runnable() {
            @Override
            public void run() {

                SetView.setIndicator(tabLayout,60,60);
            }
        });
    }
}
