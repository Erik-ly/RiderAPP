package com.bophia.erik.ykxrider.UI.neworder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.bophia.erik.ykxrider.MyApplication;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.UI.distribution.PickupFragment;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.DateUtils;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.MapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * “新订单”界面Fragment
 */
public class NewOrderFragment extends Fragment {

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private int relationSalepoint;
    private String distributorPhone;

    private List<Order> lists = new ArrayList<Order>();
    private ListView lv_noDistribution;
    private View view;
    private MyAdapter myAdapter;
    private SwipeRefreshLayout sr_order;
    private static Ringtone mRingtone;//铃声

    //实例
    private static NewOrderFragment newOrderFragmentInstance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.nodistribution_listview,container,false);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newOrderFragmentInstance = this;

    }

    @Override
    public void onResume() {
        super.onResume();

//        lv_noDistribution = getActivity().findViewById(R.id.lv_noDistribution);
        new MyClickListener().onClick(null);
//        myAdapter = new MyAdapter();
//        myAdapter.notifyDataSetChanged();
//        lv_noDistribution.setAdapter(myAdapter);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //获取配送员信息
        sp = getActivity().getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");
        relationSalepoint = sp.getInt("relationSalepoint",-1);

        //设置ListView适配器
        lv_noDistribution = getActivity().findViewById(R.id.lv_noDistribution);
        myAdapter = new MyAdapter();
        lv_noDistribution.setAdapter(myAdapter);

        //设置下拉刷新
        sr_order = getActivity().findViewById(R.id.sr_order);
        sr_order.setColorSchemeResources(android.R.color.holo_blue_dark);
        sr_order.setProgressBackgroundColorSchemeResource(android.R.color.white);
        sr_order.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(sr_order.isRefreshing()){
                    lists.clear();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new MyClickListener().onClick(null);
                                    myAdapter.notifyDataSetChanged();
//                                    lv_noDistribution.setAdapter(myAdapter);
                                }
                            });

                            sr_order.setRefreshing(false);//取消刷新

                        }
                    },1500);
                }
            }
        });

    }

    //查询数据库
    public class MyClickListener implements View.OnClickListener {

        public void onClick(View v){

            lists.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {

                    synchronized (NewOrderFragment.this ){
                        Looper.prepare();

                            //获取所有与该销售点相关的未配送订单
                            String httpsUrl = getResources().getString(R.string.httpsUrl);

                            //获取新订单
                            String result = HttpRequest.sendGet(httpsUrl + "GetAllNoDisOrderByRelationSP","relationSalepoint=" + relationSalepoint + "&distributorPhone=" +distributorPhone);

                            //获取该销售点的信息
                            String salePointResult = HttpRequest.sendGet(httpsUrl + "GetSalePoint","salePointId=" + relationSalepoint);

                        //判断网络是否正常
                        if (result.equals("404") || salePointResult.equals("404")){

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

                            //新订单信息转换为JSON对象
                            JSONObject noDisOrderObject = new JSONObject(result);
                            JSONArray noDisOrderContent = noDisOrderObject.optJSONArray("content");

                            //销售点信息转换为JSON对象
                            JSONObject salePointObject = new JSONObject(salePointResult);
                            JSONObject salePointContent = salePointObject.getJSONObject("content");

                            //获取销售点基本信息
                            String salePointName = salePointContent.optString("salePointName");
                            String salePointAddress = salePointContent.optString("detailedAddress");
                            String salePointLongitude = salePointContent.optString("longitude");
                            String salePointLatitude = salePointContent.optString("latitude");

                            //获取配送点地址列表
                            JSONArray salePointAddressList = salePointContent.optJSONArray("addressList");

                            //遍历新订单
                            for (int i=0; i<noDisOrderContent.length(); i++){
                                JSONObject orderObject = noDisOrderContent.getJSONObject(i);

                                //遍历销售点地址
                                for (int j=0; j<salePointAddressList.length(); j++){
                                    JSONObject salePointAddressObject = salePointAddressList.getJSONObject(j);

                                    //寻找订单地址与配送点地址一样的
                                    if (orderObject.optString("userSite").equals(salePointAddressObject.optString("salePointAddressName"))){

                                        //把数据封装到Order中
                                        Order order = new Order();
                                        order.setId(orderObject.optInt("id"));
                                        order.setOrderId(orderObject.optString("orderId"));
                                        order.setOrderType(orderObject.optInt("orderType"));
                                        order.setTime(orderObject.optString("time"));
                                        order.setPaymentTime(orderObject.optString("paymentTime"));
                                        order.setUserName(orderObject.optString("userName"));
                                        order.setUserPhone(orderObject.optString("userPhone"));
                                        order.setUserSex(orderObject.optInt("userSex"));
                                        order.setUserSite(orderObject.optString("userSite"));
                                        order.setUserSiteDetails(orderObject.optString("userSiteDetails"));
                                        order.setWhether(orderObject.optInt("whether"));
                                        order.setRemark(orderObject.optString("remark"));
                                        order.setBooking(orderObject.optString("booking"));

                                        order.setSalePointName(salePointName);
                                        order.setSalePointAddress(salePointAddress);
                                        order.setSalePointLongitude(salePointLongitude);
                                        order.setSalePointLatitude(salePointLatitude);

                                        order.setSalePointAddressLongitude(salePointAddressObject.optString("longitude"));
                                        order.setSalePointAddressLatitude(salePointAddressObject.optString("latitude"));

                                        lists.add(order);

                                        break;
                                    }
                                }

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

                                myAdapter.notifyDataSetChanged();
//                                        lv_noDistribution.setAdapter(myAdapter);

                            }
                        });

                    }
                }
            }).start();
        }
    }

    //定义ListView数据适配器
    private class MyAdapter extends BaseAdapter{

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
                v = View.inflate(getActivity().getApplicationContext(),R.layout.new_order_item,null);

            }else {
                v = view;
            }

            if(i>=lists.size()){
                return v;
            }

            //找到控件，用来显示数据
            final Order order = lists.get(i);

            //抛出
            ImageView iv_throwOut = v.findViewById(R.id.iv_throwOut);
            iv_throwOut.setVisibility(View.VISIBLE);
            iv_throwOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                                dialog.setTitle("抛出订单");
                                dialog.setMessage("是否确认抛出订单？");
                                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        new ClickListener().throwOut(order);

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

            //预计配送费
            String orderTime = order.getPaymentTime();
            String disMoney = DateUtils.estimateDisMoney(orderTime);

            String str = "预计配送费 <font color='#d8822a'>" + "¥" + disMoney + "</font> 元";
            TextView tv_distributionFee = v.findViewById(R.id.tv_distributionFee);
            tv_distributionFee.setText(Html.fromHtml(str));

            //取货距离
            //获取配送员位置
            MyApplication myApplication = (MyApplication)getActivity().getApplicationContext();
            AMapLocation aMapLocation = myApplication.appMapLocation;

            if (aMapLocation != null && aMapLocation.getErrorCode() == 0){

                double disLongitude = aMapLocation.getLongitude();
                double disLatitude = aMapLocation.getLatitude();

                //计算取货距离
                Double pickupDistance = MapUtils.getDistance(disLongitude,disLatitude,Double.parseDouble(order.getSalePointLongitude()),Double.parseDouble(order.getSalePointLatitude()));

                //显示取货距离
                TextView tv_pickupDistance = v.findViewById(R.id.tv_pickupDistance);
                tv_pickupDistance.setText(pickupDistance + "m");
            }

            //送货距离
            Double deliveryDistance = MapUtils.getDistance(Double.parseDouble(order.getSalePointLongitude()),Double.parseDouble(order.getSalePointLatitude()),Double.parseDouble(order.getSalePointAddressLongitude()),Double.parseDouble(order.getSalePointAddressLatitude()));
            TextView tv_deliveryDistance = v.findViewById(R.id.tv_deliveryDistance);
            tv_deliveryDistance.setText(deliveryDistance + "m");

            //商家信息
            TextView tv_businessName = v.findViewById(R.id.tv_businessName);
            tv_businessName.setText(order.getSalePointName());

            TextView tv_businessAddress = v.findViewById(R.id.tv_businessAddress);
            tv_businessAddress.setText(order.getSalePointAddress());

            //用户地址
            TextView tv_userAddress = v.findViewById(R.id.tv_userAddress);
            String userAddress = order.getUserSite() + order.getUserSiteDetails();
            tv_userAddress.setText(userAddress);

            //跳转页面详情
            LinearLayout ll_orderInfo = v.findViewById(R.id.ll_orderInfo);
            ll_orderInfo.setOnClickListener(new View.OnClickListener() {
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

            //“接单”按钮
            Button bt_receipt = v.findViewById(R.id.bt_receipt);
            bt_receipt.setVisibility(View.VISIBLE);
            bt_receipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog dialog2 = new AlertDialog.Builder(getActivity()).create();
                    dialog2.setTitle("确认接单");
                    dialog2.setMessage("是否确认接单？");
                    dialog2.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            new ClickListener().receipt(order);

                        }
                    });

                    dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog2.dismiss();
                        }
                    });
                    dialog2.show();

                }
            });

            return v;
        }
    }

    //按钮监听
    private class ClickListener {

        //“抛出”按钮
        public void throwOut(final Order itemOrder){

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String httpsUrl = getString(R.string.httpsUrl);
                    String url = httpsUrl + "MiPushService";

                    String result = HttpRequest.sendGet(url,"staffId=" + distributorId + "&relationSalepoint=" + relationSalepoint + "&orderId=" + itemOrder.getOrderId());

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

                        int state = updateDisOrderReceipt.optInt("state");
                        final String message = updateDisOrderReceipt.optString("message");

                        if (state == 0){

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();

                                    //刷新页面
                                    getInstance().onResume();

                                }
                            });

                        }else {
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

        //“接单”按钮
        public void receipt(final Order itemOrder){

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String httpsUrl = getString(R.string.httpsUrl);
                    String url = httpsUrl + "UpdateDisOrderReceipt";

                    //加密
                    String distributorNameEncoded= null;
                    try {
                        distributorNameEncoded = android.util.Base64.encodeToString(distributorName.getBytes("utf-8"), android.util.Base64.NO_WRAP);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String result = HttpRequest.sendGet(url,"id=" + itemOrder.getId() + "&staffId=" + distributorId + "&distributorName=" + distributorNameEncoded + "&distributorPhone=" + distributorPhone);

//                    String result = HttpRequest.sendGet(url,"id=" + itemOrder.getId() + "&distributorName=" + distributorNameEncoded + "&distributorPhone=" + distributorPhone);

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

                        int state = updateDisOrderReceipt.optInt("state");
                        final String message = updateDisOrderReceipt.optString("message");

                        if (state == 0){

                            if (getActivity() == null){
                                return;
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();

                                    //刷新该页面
                                    getInstance().onResume();

                                    //刷新“待取货”界面
                                    if (PickupFragment.getInstance() == null){
                                        return;
                                    }
//                                    if (PickupFragment.getInstance().getActivity() == null){
//                                        return;
//                                    }

                                    PickupFragment.getInstance().onResume();

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
    public static NewOrderFragment getInstance(){
        return newOrderFragmentInstance;
    }

}
