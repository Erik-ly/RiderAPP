package com.bophia.erik.ykxrider.UI.distribution;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.UI.neworder.OrderInfoActivity;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.Anticlockwise;
import com.bophia.erik.ykxrider.utils.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * “配送中”的Fragment
 */
public class DistributingFragment extends Fragment {

    //配送员信息
    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;

    private String salePointName;
    private String detailedAddress;
    private String salePointLongitude;
    private String salePointLatitude;

    private List<Order> lists;
    private DistributingAdapter distributingAdapter;

    //请求链接
    private String httpsUrl;

    //实例
    private static DistributingFragment distributingFragmentInstance;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.distribution_distributing_fragment,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //获取配送员信息
        sp = getActivity().getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");

        salePointName = sp.getString("salePointName","NULL");
        detailedAddress = sp.getString("detailedAddress","NULL");
        salePointLongitude = sp.getString("salePointLongitude","NULL");
        salePointLatitude = sp.getString("salePointLatitude","NULL");

        //获取链接
        httpsUrl = getString(R.string.httpsUrl);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        distributingFragmentInstance = this;

    }

    @Override
    public void onResume() {
        super.onResume();

        //定义list存放listView要展示的数据
        lists = new ArrayList<Order>();
        lists.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {

                //获取所有与该销售点相关的正在配送的订单
                String httpsUrl = getString(R.string.httpsUrl);
                String url = httpsUrl + "GetDistributionOrderByDisPhone";

                String result = HttpRequest.sendGet(url,"distributorPhone=" + distributorPhone + "&orderWhether=" + 2);

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
                    JSONObject getDistributionOrderByDisPhone = new JSONObject(result);

                    JSONArray content = getDistributionOrderByDisPhone.optJSONArray("content");


                    for (int i=0; i<content.length(); i++){
                        JSONObject distributionOrder = content.getJSONObject(i);

                        Order historyorder = new Order();
                        historyorder.setId(distributionOrder.optInt("id"));
                        historyorder.setOrderId(distributionOrder.getString("orderId"));
                        historyorder.setRelationSalepoint(distributionOrder.optInt("relationSalepoint"));
                        historyorder.setOrderType(distributionOrder.optInt("orderType"));
                        historyorder.setWhether(distributionOrder.optInt("whether"));
                        historyorder.setTime(distributionOrder.getString("time"));
                        historyorder.setPaymentTime(distributionOrder.getString("paymentTime"));
                        historyorder.setUserName(distributionOrder.getString("userName"));
                        historyorder.setUserSex(distributionOrder.optInt("userSex"));
                        historyorder.setUserPhone(distributionOrder.getString("userPhone"));
                        historyorder.setUserSite(distributionOrder.getString("userSite"));
                        historyorder.setUserSiteDetails(distributionOrder.getString("userSiteDetails"));
                        historyorder.setDistributionStartTime(distributionOrder.getString("distributionStartTime"));
                        historyorder.setRemark(distributionOrder.getString("remark"));
                        historyorder.setBooking(distributionOrder.optString("booking"));

                        //把javabeen对象加入到集合中
                        lists.add(historyorder);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (getActivity() == null){
                    return;

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            distributingAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });


            }
        }).start();

        //找到控件ListView
        final ListView lv_historyOrder = getActivity().findViewById(R.id.lv_distribution);

        distributingAdapter = new DistributingAdapter();
        lv_historyOrder.setAdapter(distributingAdapter);
    }

    //定义listview数据适配器
    private class DistributingAdapter extends BaseAdapter {
        private Long disTime;
        private String nowTime;

        @Override
        public int getCount() {
            return lists.size();
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
                //创建新的对象
                v = View.inflate(getActivity().getApplicationContext(),R.layout.distribution_order_item,null);

            }else {
                v = view;
            }

            if(i>=lists.size()){
                return v;
            }

            //显示数据
            final Order order = lists.get(i);

            String orderTime = order.getPaymentTime();

            //获取当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            nowTime = sdf.format(date);

            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                disTime = sdf2.parse(nowTime).getTime() - sdf2.parse(orderTime).getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //计算倒计时初始化时间
            int initTime = 900 - disTime.intValue()/1000 ;

            if (initTime > 0){

                //倒计时
                LinearLayout ll_countdownTimer = v.findViewById(R.id.ll_countdownTimer);
                ll_countdownTimer.setVisibility(View.VISIBLE);

                Anticlockwise mTimer = v.findViewById(R.id.ac_timer);
                mTimer.initTime(initTime);
                mTimer.reStart();

            }

            //超时计时
            if (initTime < 0){

                //隐藏倒计时
                LinearLayout ll_countdownTimer = v.findViewById(R.id.ll_countdownTimer);
                ll_countdownTimer.setVisibility(View.GONE);

                //显示计时
                LinearLayout ll_timer = v.findViewById(R.id.ll_timer);
                ll_timer.setVisibility(View.VISIBLE);

                //显示计时
                Chronometer ch_timer = v.findViewById(R.id.ch_timer);
                ch_timer.setBase(SystemClock.elapsedRealtime() - disTime + 900000);//设置开始时间
                ch_timer.setFormat("%s");
                ch_timer.start();
            }

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
                    intent.setClass(getActivity(),OrderInfoActivity.class);
                    startActivity(intent);
                }
            });

            //按钮
            LinearLayout ll_button = v.findViewById(R.id.ll_button);
            ll_button.setVisibility(View.VISIBLE);

            //显示“联系买家”按钮
            Button bt_callUser = v.findViewById(R.id.bt_callUser);
            bt_callUser.setVisibility(View.VISIBLE);
            bt_callUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+order.getUserPhone()));//跳转到拨号界面，同时传递电话号码
                    startActivity(intent);
                }
            });

            //显示“我已送达”按钮
            Button bt_doneDistribution = v.findViewById(R.id.bt_doneDistribution);
            bt_doneDistribution.setVisibility(View.VISIBLE);
            bt_doneDistribution.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (getActivity() == null){
                        return;
                    }

                    //验证码布局
                    LayoutInflater li = LayoutInflater.from(getActivity());
                    final View view_verificationcode = li.inflate(R.layout.verificationcode,null);

                    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setTitle("完成配送");
                    dialog.setView(view_verificationcode);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //获取验证码
                            EditText editText = view_verificationcode.findViewById(R.id.et_verificationCode);
                            String verificationcode = editText.getText().toString().trim();

                            //事件响应
                            new ClickListener().doneDistribution(order,verificationcode);

                            //刷新界面
                            getInstance().onResume();

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
            });
            return v;
        }
    }

    //按钮监听
    private class ClickListener{

        public void doneDistribution(final Order itemOrder, final String verificationcode){

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String result = HttpRequest.sendGet(httpsUrl + "UpdateDisOrderDisEnd","id=" + itemOrder.getId() + "&staffId=" + distributorId + "&verificationCode=" + verificationcode);

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
                        JSONObject updateDisOrderOutbound = new JSONObject(result);

                        int state = updateDisOrderOutbound.optInt("state");
                        final String message = updateDisOrderOutbound.optString("message");

                        if (state == 0){

                            if (getActivity() == null){
                                return;
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();

                                    //刷新界面
                                    getInstance().onResume();

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

    //获取实例
    public static DistributingFragment getInstance(){
        return distributingFragmentInstance;
    }

}
