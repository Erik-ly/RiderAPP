package com.bophia.erik.ykxrider.UI.mine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.MainActivity;
import com.bophia.erik.ykxrider.UI.distribution.PickupFragment;
import com.bophia.erik.ykxrider.UI.mine.historyOrder.HistoryOrderActivity;
import com.bophia.erik.ykxrider.UI.mine.statistics.MonthStatisticsFragment;
import com.bophia.erik.ykxrider.UI.mine.myEvaluation.MyEvaluationActivity;
import com.bophia.erik.ykxrider.utils.MyViewPagerAdapter;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.UI.mine.setting.SettingActivity;
import com.bophia.erik.ykxrider.UI.mine.statistics.StatisticChartsActivity;
import com.bophia.erik.ykxrider.UI.mine.statistics.TodayStatisticsFragment;
import com.bophia.erik.ykxrider.UI.mine.wallet.WalletActivity;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.SetView;
import com.bophia.erik.ykxrider.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * “我的”界面Fragment
 */
public class MineFragment extends Fragment implements TabLayout.OnTabSelectedListener{

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;
    private View view;
    private TextView tv_disName;
    private TextView tv_disPhone;

    //上下线
    private ImageView iv_online;
    private TextView tv_online;
    private ImageView iv_offline;
    private TextView tv_offline;

    private int onlineState;
    private int distributionNum;
    private BigDecimal distributionMoney;

    private int warnNum;
    private BigDecimal forfeit;

    private String aliPayId;
    private String aliPayAmount;
    private String aliPayName;
    private String aliPayRemark;

    private List<Order> list = new ArrayList<>();
    private DoneDistributionOrderAdapter doneDistributionOrderAdapter;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyViewPagerAdapter viewPagerAdapter;
    //TabLayout标签
    private String[] titles=new String[]{"今日战绩","本月战绩"};
    private List<Fragment> fragments=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.mine_scrollview,container,false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //查询接单总量，余额和本月警告次数
        new Thread(new Runnable() {
            @Override
            public void run() {

                String httpsUrl = getString(R.string.httpsUrl);

                //已完成订单
//                doneDistributionOrderAdapter = new DoneDistributionOrderAdapter();

                String doneOrderInfo = HttpRequest.sendGet(httpsUrl + "GetDoneDisOrderByDisPhone","distributorPhone=" + distributorPhone);

                //判断网络是否正常
                if (doneOrderInfo.equals("404")){

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
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

                //获取配送员已完成订单数和余额
                String url = httpsUrl + "GetDistributorById";

                String result = HttpRequest.sendGet(url,"staffId=" + distributorId);

                //判断网络是否正常
                if (result.equals("404")){

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject distributorInfo = new JSONObject(result);

                    int state = distributorInfo.optInt("state");

                    if (state == 0){

                        onlineState = distributorInfo.optInt("onlineState");

                        distributionNum = distributorInfo.optInt("distributionNum");

//                        String distributionMoneyString = distributorInfo.optString("distributionMoney");
//                        System.out.println("distributionMoneyString:=============" + distributionMoneyString);

                        distributionMoney = new BigDecimal(distributorInfo.optString("distributionMoney"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //获取当前年月
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date date = new Date(System.currentTimeMillis());
                String nowDate = sdf.format(date);

                String year1 = nowDate.substring(0,4);//获取年
                String month1 = nowDate.substring(5,7);//获取月

                int year = Integer.valueOf(year1).intValue();
                int month = Integer.valueOf(month1).intValue();

                String url_2 = httpsUrl + "GetDistributorWarnByStaffId";
                String result_2 = HttpRequest.sendGet(url_2,"staffId=" + distributorId + "&year=" + year + "&month=" + month);

                //判断网络是否正常
                if (result_2.equals("404")){

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject distributorWarnInfo = new JSONObject(result_2);

                    int state = distributorWarnInfo.optInt("state");

                    if (state == 0){
                        warnNum = distributorWarnInfo.optInt("warnNum");
                    }else {
                        warnNum = 0;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(String.valueOf(distributionNum)) || TextUtils.isEmpty(String.valueOf(warnNum)) || TextUtils.isEmpty(String.valueOf(distributionMoney))){
                    return;

                }else {

                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //上下线
                            //上线
                            if (onlineState == 1){

                                iv_online.setImageDrawable(getResources().getDrawable(R.drawable.icon_online_click));
                                tv_online.setTextColor(getResources().getColor(R.color.colorGreen));

                                iv_offline.setImageDrawable(getResources().getDrawable(R.drawable.icon_offline_unclicked));
                                tv_offline.setTextColor(getResources().getColor(R.color.colorGray));

                            //下线
                            }else if (onlineState == 0){
                                iv_online.setImageDrawable(getResources().getDrawable(R.drawable.icon_online_unclicked));
                                tv_online.setTextColor(getResources().getColor(R.color.colorGray));

                                iv_offline.setImageDrawable(getResources().getDrawable(R.drawable.icon_offline_click));
                                tv_offline.setTextColor(getResources().getColor(R.color.colorRed));
                            }

                            //“钱包”
                            RelativeLayout rl_wallet = getActivity().findViewById(R.id.rl_wallet);
                            rl_wallet.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //跳转到“钱包”页面
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(),WalletActivity.class);
                                    startActivity(intent);
                                }

                            });

                            //“历史订单”
                            RelativeLayout rl_doneOrder = getActivity().findViewById(R.id.rl_doneOrder);
                            rl_doneOrder.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //跳转到“历史订单”页面
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(),HistoryOrderActivity.class);
                                    startActivity(intent);

                                }
                            });

                            //“我的评价”
                            RelativeLayout rl_myEvaluation = getActivity().findViewById(R.id.rl_myEvaluation);
                            rl_myEvaluation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //跳转到“我的评价”页面
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(),MyEvaluationActivity.class);
                                    startActivity(intent);

                                }
                            });

                            //我的余额
                            TextView tt_myBalance = getActivity().findViewById(R.id.tt_myBalance);
                            tt_myBalance.setText(distributionMoney + "元");

                            //统计图表
                            RelativeLayout rl_statisticCharts = getActivity().findViewById(R.id.rl_statisticCharts);

                            rl_statisticCharts.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(),StatisticChartsActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                }
            }
        }).start();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //头部
        //显示配送员姓名、电话
        tv_disName = getActivity().findViewById(R.id.tv_disName);
        tv_disPhone = getActivity().findViewById(R.id.tv_disPhone);

        //获取配送员信息
        sp = getActivity().getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");

        //设置头部信息
        final TextView tv_disName = view.findViewById(R.id.tv_disName);
        TextView tv_disPhone = view.findViewById(R.id.tv_disPhone);

        tv_disName.setText(distributorName);
        tv_disPhone.setText(distributorPhone);

        //上下线
        //上线
        LinearLayout ll_online = view.findViewById(R.id.ll_online);
        iv_online = view.findViewById(R.id.iv_online);
        tv_online = view.findViewById(R.id.tv_online);

        ll_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //判断当前状态
                if (onlineState == 1){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(),"当前已是“上线”状态！",Toast.LENGTH_LONG).show();
                        }
                    });
                }else if (onlineState == 0){

                    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setTitle("上线");
                    dialog.setMessage("确定上线？");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            changeOnlineState(1);

                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                }
            }
        });

        //下线
        LinearLayout ll_offline = view.findViewById(R.id.ll_offline);
        iv_offline = view.findViewById(R.id.iv_offline);
        tv_offline = view.findViewById(R.id.tv_offline);

        ll_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //判断当前状态
                if (onlineState == 0){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(),"当前已是“下线”状态！",Toast.LENGTH_LONG).show();
                        }
                    });
                }else if (onlineState == 1){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(),"现阶段不允许配送员“下线”！",Toast.LENGTH_LONG).show();
                        }
                    });

//                    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
//                    dialog.setTitle("下线");
//                    dialog.setMessage("确定下线？");
//                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                            changeOnlineState(0);
//
//                        }
//                    });
//                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialog.dismiss();
//                        }
//                    });
//                    dialog.show();

                }
            }
        });

        //统计战绩
        initStatistics();

        //设置
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //活动中心
                RelativeLayout rl_activityCenter = getActivity().findViewById(R.id.rl_activityCenter);
                rl_activityCenter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(getActivity(),"“活动中心”即将上线！",Toast.LENGTH_LONG).show();

                    }
                });

                //使用帮助
                RelativeLayout rl_helpCenter = getActivity().findViewById(R.id.rl_helpCenter);

                rl_helpCenter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LayoutInflater li = LayoutInflater.from(getActivity());
                        View view_disUseHelp = li.inflate(R.layout.disusehelp,null);

                        WebView wv_disUseHelp = view_disUseHelp.findViewById(R.id.wv_disUseHelp);
                        wv_disUseHelp.loadUrl("file:///android_asset/usehelp.html");

                        //应用内打开
                        wv_disUseHelp.setWebViewClient(new WebViewClient(){
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url){
                                view.loadUrl(url);
                                return true;
                            }
                        });

                        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                        dialog.setTitle("使用帮助");
                        dialog.setView(view_disUseHelp);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        dialog.show();
                    }
                });

                //费用说明
                RelativeLayout rl_moneyInfo = getActivity().findViewById(R.id.rl_moneyInfo);

                rl_moneyInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LayoutInflater li = LayoutInflater.from(getActivity());
                        View view_disMoneyInfo = li.inflate(R.layout.dismoneyinfo,null);
                        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                        dialog.setTitle("费用说明");
                        dialog.setView(view_disMoneyInfo);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        dialog.show();

                    }
                });

                //设置
                RelativeLayout rl_setting = getActivity().findViewById(R.id.rl_setting);

                rl_setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //跳转到“设置”页面
                        Intent intent = new Intent();
                        intent.setClass(getActivity(),SettingActivity.class);
                        startActivity(intent);

                    }
                });
            }
        });
    }

    //战绩部分TabLayout设置
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

    //完成订单列表适配器
    private class DoneDistributionOrderAdapter extends BaseAdapter {

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
                v = View.inflate(getActivity().getApplicationContext(),R.layout.item,null);
            }else {
                v = view;
            }

            Order doneOrder = list.get(i);

            //找到控件，显示数据
            TextView tv_orderId = v.findViewById(R.id.tv_orderId);
            tv_orderId.setText(doneOrder.getId() + "-" + doneOrder.getOrderId());

            TextView tv_orderTime = v.findViewById(R.id.tv_orderTime);
            tv_orderTime.setText(doneOrder.getTime());

            TextView tv_userName = v.findViewById(R.id.tv_userName);
            tv_userName.setText(doneOrder.getUserName());

            TextView tv_userSex = v.findViewById(R.id.tv_userSex);
            int userSex = doneOrder.getUserSex();
            if (userSex == 1){
                tv_userSex.setText("(先生)");
            }else if (userSex == 2){
                tv_userSex.setText("(女士)");
            }else if (userSex == 0){
                tv_userSex.setText("(未知)");
            }

            TextView tv_userPhone = v.findViewById(R.id.tv_userPhone);
            tv_userPhone.setText(doneOrder.getUserPhone());

            TextView tv_userSite = v.findViewById(R.id.tv_userSite);
            tv_userSite.setText(doneOrder.getUserSite() + doneOrder.getUserSiteDetails());

            LinearLayout ll_remark = v.findViewById(R.id.ll_remark);
            ll_remark.setVisibility(View.GONE);

            //接单时间
            LinearLayout ll_receiptTime = v.findViewById(R.id.ll_receiptTime);
            ll_receiptTime.setVisibility(View.VISIBLE);
            TextView tv_receiptTime = v.findViewById(R.id.tv_receiptTime);
            tv_receiptTime.setText(doneOrder.getReceiptTime());

            //出库时间
            LinearLayout ll_outBoundTime = v.findViewById(R.id.ll_outBoundTime);
            ll_outBoundTime.setVisibility(View.VISIBLE);
            TextView tv_outBoundTime = v.findViewById(R.id.tv_outBoundTime);
            tv_outBoundTime.setText(doneOrder.getOutBoundTime());

            //开始配送时间
            LinearLayout ll_distributionStartTime = v.findViewById(R.id.ll_distributionStartTime);
            ll_distributionStartTime.setVisibility(View.VISIBLE);
            TextView tv_distributionStartTime = v.findViewById(R.id.tv_distributionStartTime);
            tv_distributionStartTime.setText(doneOrder.getDistributionStartTime());

            //完成时间
            LinearLayout ll_distributionEndTime = v.findViewById(R.id.ll_distributionEndTime);
            ll_distributionEndTime.setVisibility(View.VISIBLE);
            TextView tv_distributionEndTime = v.findViewById(R.id.tv_distributionEndTime);
            tv_distributionEndTime.setText(doneOrder.getDistributionEndTime());

            //总共用时
            LinearLayout ll_allUseTime = v.findViewById(R.id.ll_allUseTime);
            ll_allUseTime.setVisibility(View.VISIBLE);
            TextView tv_allUseTime = v.findViewById(R.id.tv_allUseTime);
            tv_allUseTime.setText(doneOrder.getDisTime());

            //配送费
            LinearLayout ll_disMoney = v.findViewById(R.id.ll_disMoney);
            ll_disMoney.setVisibility(View.VISIBLE);
            TextView tv_disMoney = v.findViewById(R.id.tv_disMoney);
            tv_disMoney.setText(doneOrder.getDisMoney().toString() + "元");

            //警告
            if (doneOrder.getDisWarn() == 1){
                LinearLayout ll_warn = v.findViewById(R.id.ll_warn);
                ll_warn.setVisibility(View.VISIBLE);
                TextView tv_warn = v.findViewById(R.id.tv_warn);
                tv_warn.setText("1 次");
            }

            return v;
        }
    }

    //“今日战绩”和“本月战绩”初始化
    private void initStatistics(){
        tabLayout = getActivity().findViewById(R.id.tl_statistics);
        viewPager = getActivity().findViewById(R.id.vp_achievement);

        //设置TabLayout标签的显示方式
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        //循环注入标签
        for (String tab:titles){
            tabLayout.addTab(tabLayout.newTab().setText(tab));
        }

        //设置TabLayout点击事件
        tabLayout.setOnTabSelectedListener(this);

        fragments.clear();

        fragments.add(new TodayStatisticsFragment());
        fragments.add(new MonthStatisticsFragment());

        viewPagerAdapter = new MyViewPagerAdapter(getActivity().getSupportFragmentManager(),titles,fragments);
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


    //“上下线”按钮
    public void changeOnlineState(final int changeState){

        new Thread(new Runnable() {
            @Override
            public void run() {

                String httpsUrl = getString(R.string.httpsUrl);
                String url = httpsUrl + "ChangeOnlineState";

                String result = HttpRequest.sendGet(url,"staffId=" + distributorId + "&onlineState=" + changeState);

                //判断网络是否正常
                if (result.equals("404")){

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject updateDisOrderReceipt = new JSONObject(result);

                    final int state = updateDisOrderReceipt.optInt("state");
                    final String message = updateDisOrderReceipt.optString("message");

                    if (state == 0){

                        if (getActivity() == null){
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //从“上线”改为“下线”状态
                                if (changeState == 0){

                                    //修改在线状态
                                    onlineState = 0;

                                    iv_online.setImageDrawable(getResources().getDrawable(R.drawable.icon_online_unclicked));
                                    tv_online.setTextColor(getResources().getColor(R.color.colorGray));

                                    iv_offline.setImageDrawable(getResources().getDrawable(R.drawable.icon_offline_click));
                                    tv_offline.setTextColor(getResources().getColor(R.color.colorRed));

                                    Toast.makeText(getActivity(),"您已下线！",Toast.LENGTH_LONG).show();

                                    //从“下线”改为“上线”状态
                                }else if (changeState == 1){

                                    //修改在线状态
                                    onlineState = 1;

                                    iv_online.setImageDrawable(getResources().getDrawable(R.drawable.icon_online_click));
                                    tv_online.setTextColor(getResources().getColor(R.color.colorGreen));

                                    iv_offline.setImageDrawable(getResources().getDrawable(R.drawable.icon_offline_unclicked));
                                    tv_offline.setTextColor(getResources().getColor(R.color.colorGray));

                                    Toast.makeText(getActivity(),"您已上线！",Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }else {

                        if (getActivity() == null){
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




}
