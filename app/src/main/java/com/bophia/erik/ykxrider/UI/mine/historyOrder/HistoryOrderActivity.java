package com.bophia.erik.ykxrider.UI.mine.historyOrder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.UI.neworder.OrderInfoActivity;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * “历史订单”详情Activity
 */
public class HistoryOrderActivity extends AppCompatActivity{

    //配送员信息
    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;
    private int relationSalepoint;

    private String salePointName;
    private String detailedAddress;
    private String salePointLongitude;
    private String salePointLatitude;

    //请求链接
    private String httpsUrl;

    private List<Order> list = new ArrayList<>();

    //加载动画
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.historyorder_layout);

        //自定义ActionBar
        setCustomActionBar();

        //加载动画
        avi = findViewById(R.id.avi_loading);

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");
        relationSalepoint = sp.getInt("relationSalepoint",-1);

        salePointName = sp.getString("salePointName","NULL");
        detailedAddress = sp.getString("detailedAddress","NULL");
        salePointLongitude = sp.getString("salePointLongitude","NULL");
        salePointLatitude = sp.getString("salePointLatitude","NULL");

        //获取链接
        httpsUrl = getString(R.string.httpsUrl);

        //获取历史订单数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                //显示加载动画
                avi.show();

                String doneOrderInfo = HttpRequest.sendGet(httpsUrl + "GetDoneDisOrderByDisPhone","distributorPhone=" + distributorPhone);

                //判断网络是否正常
                if (doneOrderInfo.equals("404")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HistoryOrderActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject getDoneDisOrderByDisPhone = new JSONObject(doneOrderInfo);

                    JSONArray content = getDoneDisOrderByDisPhone.optJSONArray("content");

                    for (int i=0; i<content.length(); i++){
                        JSONObject doneDisOrder = content.getJSONObject(i);

                        Order doneOrder = new Order();
                        doneOrder.setId(doneDisOrder.optInt("id"));
                        doneOrder.setOrderId(doneDisOrder.optString("orderId"));
                        doneOrder.setOrderType(doneDisOrder.optInt("orderType"));
                        doneOrder.setWhether(doneDisOrder.optInt("whether"));
                        doneOrder.setTime(doneDisOrder.optString("time"));
                        doneOrder.setUserName(doneDisOrder.optString("userName"));
                        doneOrder.setUserPhone(doneDisOrder.optString("userPhone"));
                        doneOrder.setUserSex(doneDisOrder.optInt("userSex"));
                        doneOrder.setUserSite(doneDisOrder.optString("userSite"));
                        doneOrder.setUserSiteDetails(doneDisOrder.optString("userSiteDetails"));
                        doneOrder.setRemark(doneDisOrder.optString("remark"));
                        doneOrder.setReceiptTime(doneDisOrder.optString("receiptTime"));
                        doneOrder.setOutBoundTime(doneDisOrder.optString("outboundTime"));
                        doneOrder.setDistributionStartTime(doneDisOrder.optString("distributionStartTime"));
                        doneOrder.setDistributionEndTime(doneDisOrder.optString("distributionEndTime"));
                        doneOrder.setDisTime(doneDisOrder.optString("disTime"));
                        doneOrder.setDisMoney(new BigDecimal(doneDisOrder.optInt("disMoney")));
                        doneOrder.setDisWarn(doneDisOrder.optInt("disWarn"));

                        list.add(doneOrder);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //显示历史订单
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //隐藏加载动画
                        avi.hide();

                        ListView lv_historyOrder = findViewById(R.id.lv_historyOrder);
                        lv_historyOrder.setAdapter(new HistoryOrderAdapter());
                    }
                });

            }
        }).start();

    }

    //完成订单列表适配器
    private class HistoryOrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            if (view == null){
                v = View.inflate(getApplicationContext(),R.layout.distribution_order_item,null);
            }else {
                v = view;
            }

            //将list以配送结束时间倒序排列
            Collections.sort(list, new Comparator<Order>() {
                @Override
                public int compare(Order order, Order order2) {

                    int i = order2.getDistributionEndTime().compareTo(order.getDistributionEndTime());//倒序
                    return i;
                }
            });

            final Order order = list.get(i);

            //显示送达时间和配送费
            RelativeLayout rl_distributionInfo = v.findViewById(R.id.rl_distributionInfo);
            rl_distributionInfo.setVisibility(View.VISIBLE);

            TextView tv_distributionTime = v.findViewById(R.id.tv_distributionTime);
            tv_distributionTime.setText(order.getDistributionEndTime() + "送达");

            String str="配送费 <font color='#d8822a'>" + "¥" + order.getDisMoney() + "</font> 元";
            TextView tv_distributionMoney = v.findViewById(R.id.tv_distributionMoney);
            tv_distributionMoney.setText(Html.fromHtml(str));

            //显示商家信息
            TextView tv_businessName = v.findViewById(R.id.tv_businessName);
            tv_businessName.setText(salePointName);

            TextView tv_businessAddress = v.findViewById(R.id.tv_businessAddress);
            tv_businessAddress.setText(detailedAddress);

            //显示用户地址
            TextView tv_userAddress = v.findViewById(R.id.tv_userAddress);
            tv_userAddress.setText(order.getUserSite() + order.getUserSiteDetails());

            //显示订单编号
            TextView tv_orderId = v.findViewById(R.id.tv_orderId);
            tv_orderId.setText("订单号：" + order.getId() + "-" + order.getOrderId());

            //跳转到详情页
            LinearLayout ll_distributionInfo = v.findViewById(R.id.ll_distributionInfo);
            ll_distributionInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //传递orderId
                    Bundle bundle = new Bundle();

                    bundle.putString("orderId",order.getOrderId());

                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(HistoryOrderActivity.this,OrderInfoActivity.class);
                    startActivity(intent);
                }
            });

            return v;
        }
    }

    //设置自定义ActionBar
    private void setCustomActionBar(){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        TextView tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        tv_actionBarTitle.setText("历史订单");

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
