package com.bophia.erik.ykxrider.utils;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bophia.erik.ykxrider.R;

/**
 * 自定义一刻行骑手Activity样式
 */
public class YKXRiderActivity extends AppCompatActivity{

    /**
     * 自定义actionBar
     * @param actionBarTitle actionBar标题
     * @param backIcon 是否显示返回箭头图标，true：显示；false：不显示
     * @param backName 返回箭头图标右侧的名称，null为不显示
     */
    protected void setCustomActionBar(String actionBarTitle, Boolean backIcon, String backName){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        TextView tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        tv_actionBarTitle.setText(actionBarTitle);

        //显示返回箭头图标
        if (backIcon){
            LinearLayout ll_back = mActionBarView.findViewById(R.id.ll_back);
            ll_back.setVisibility(View.VISIBLE);

            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        //返回箭头图标右侧的名称
        if (backName != null){

            TextView tv_backName = mActionBarView.findViewById(R.id.tv_backName);
            tv_backName.setText(backName);

        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setCustomView(mActionBarView,lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }



}
