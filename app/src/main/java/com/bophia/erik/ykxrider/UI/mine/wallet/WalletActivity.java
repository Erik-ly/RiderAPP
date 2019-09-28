package com.bophia.erik.ykxrider.UI.mine.wallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.MyApplication;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.UI.neworder.OrderInfoActivity;
import com.bophia.erik.ykxrider.entity.Bill;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.YKXRiderActivity;
import com.bophia.erik.ykxrider.wxapi.WXEntryActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * “钱包”界面Activity
 */
public class WalletActivity extends YKXRiderActivity{

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;

    private int distributionNum;
    private BigDecimal distributionMoney;

    private String httpsUrl;

    private List<Bill> billList = new ArrayList<>();
    private float todayIncome = 0.00f;//今日收入
    private float todayExpenditure = 0.00f;//今日支出

    //加载动画
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //加载布局
        setContentView(R.layout.wallet_layout);

        //加载动画
        avi = findViewById(R.id.avi_loading);

        //自定义ActionBar
        setCustomActionBar("钱包",true,"我的");

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");

        //请求链接路径
        httpsUrl = getString(R.string.httpsUrl);


        //获取配送员余额
        new Thread(new Runnable() {
            @Override
            public void run() {

                //获取配送员已完成订单数和余额
                String result = HttpRequest.sendGet(httpsUrl + "GetDistributorById","staffId=" + distributorId);

                //判断网络是否正常
                if (result.equals("404")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WalletActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject distributorInfo = new JSONObject(result);

                    int state = distributorInfo.optInt("state");

                    if (state == 0){
                        distributionNum = distributorInfo.optInt("distributionNum");

//                        String distributionMoneyString = distributorInfo.optString("distributionMoney");
//                        System.out.println("distributionMoneyString:=============" + distributionMoneyString);

                        distributionMoney = new BigDecimal(distributorInfo.optString("distributionMoney"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //显示余额
                        TextView tv_myBalance = findViewById(R.id.tv_myBalance);
                        tv_myBalance.setText(distributionMoney + "");

                        //“提现”监听
                        ImageView iv_withdraw = findViewById(R.id.iv_withdraw);
                        iv_withdraw.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //跳转到提现到微信界面
                                //将余额放入全局变量中
                                MyApplication myApplication = (MyApplication) getApplicationContext();
                                myApplication.myBalance = distributionMoney.toString();

                                Intent intent = new Intent();
                                intent.setClass(WalletActivity.this,WXEntryActivity.class);
                                startActivity(intent);
                            }
                        });

                    }
                });

            }
        }).start();



        //获取配送员账单详情
        new Thread(new Runnable() {
            @Override
            public void run() {

                //显示加载动画
                avi.show();

                String result = HttpRequest.sendGet(httpsUrl + "GetBillDetails","staffId=" + distributorId);

                //判断网络是否正常
                if (result.equals("404")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WalletActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject billInfo = new JSONObject(result);

                    int state = billInfo.optInt("state");

                    if (state == 0){

                        JSONArray orderArray = billInfo.optJSONArray("orderList");
                        JSONArray withdrawArray = billInfo.optJSONArray("disWithdrawList");

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(System.currentTimeMillis());
                        String todayDate = simpleDateFormat.format(date);
                        String today = todayDate.substring(0,10);

                        for (int i=0; i<orderArray.length(); i++){
                            JSONObject orderObject = orderArray.getJSONObject(i);

                            String distributionEndTime = orderObject.optString("distributionEndTime");
                            String disMoney = orderObject.optString("disMoney");

                            Bill bill = new Bill();

                            bill.setType("配送费");
                            bill.setOrderId(orderObject.optString("orderId"));
                            bill.setTime(distributionEndTime);
                            bill.setMoney(disMoney);

                            billList.add(bill);

                            if (distributionEndTime.contains(today)){

                                float disMoneyFloat = Float.parseFloat(disMoney);
                                todayIncome += disMoneyFloat;

//                                System.out.println("WalletActivity==================todayIncome:" + todayIncome);
                            }

                        }

                        for (int i=0; i<withdrawArray.length(); i++){
                            JSONObject withdrawObject = withdrawArray.getJSONObject(i);

                            String time = withdrawObject.optString("time");
                            String money = withdrawObject.optString("money");

                            Bill bill = new Bill();

                            bill.setType("提现");
                            bill.setTime(time);
                            bill.setMoney(money);

                            billList.add(bill);

                            if (time.contains(today)){

                                float moneyFloat = Float.parseFloat(money);
                                todayExpenditure += moneyFloat;

//                                System.out.println("WalletActivity==================todayExpenditure:" + todayExpenditure);

                            }
                        }

//                        System.out.println("WalletActivity==================billList.size():" + billList.size());

//                        for (Bill bill : billList){
//
//                            String type = bill.getType();
//                            String orderId = bill.getOrderId();
//                            String money = bill.getMoney();
//                            String time = bill.getTime();
//                            System.out.println("WalletActivity==================billList:type:" + type + " orderId:" + orderId
//                                    + " money:" + money + " time:" + time);
//
//
//                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //显示数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //显示“今日概览”
                        TextView tv_income = findViewById(R.id.tv_income);
                        tv_income.setText(String.valueOf(todayIncome));

                        TextView tv_expenditure = findViewById(R.id.tv_expenditure);
                        tv_expenditure.setText(String.valueOf(todayExpenditure));

                        //隐藏加载动画
                        avi.hide();

                        ListView lv_bill = findViewById(R.id.lv_bill);
                        lv_bill.setVisibility(View.VISIBLE);
                        lv_bill.setAdapter(new BillAdapter());

                        //设置监听
                        lv_bill.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                Bill bill = billList.get(i);

                                if (bill.getType().equals("配送费")){

                                    String orderId = bill.getOrderId();

                                    //传递orderId
                                    Bundle bundle = new Bundle();

                                    bundle.putString("orderId",orderId);

                                    Intent intent = new Intent();
                                    intent.putExtras(bundle);
                                    intent.setClass(WalletActivity.this,OrderInfoActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                });
            }
        }).start();

    }

    //账单列表Adapter
    private class BillAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return billList.size();
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
                v = View.inflate(getApplicationContext(),R.layout.bill_item_layout,null);
            }else {
                v = view;
            }

            //将billList以时间倒序排列
            Collections.sort(billList, new Comparator<Bill>() {
                @Override
                public int compare(Bill bill, Bill bill2) {

                    int i = bill2.getTime().compareTo(bill.getTime());//倒序
                    return i;
                }

            });

            Bill bill = billList.get(i);

            //时间
            TextView tv_time = v.findViewById(R.id.tv_time);
            tv_time.setText(bill.getTime());

            //类型和金额
            TextView tv_type = v.findViewById(R.id.tv_type);
            TextView tv_money = v.findViewById(R.id.tv_money);

            String type = bill.getType();
            String money = bill.getMoney();
            float moneyFloat = Float.parseFloat(money);

            if (moneyFloat >= 0){

                if (type.equals("配送费")){

                    tv_type.setText(type);
                    tv_money.setTextColor(getResources().getColor(R.color.colorGreen));
                    tv_money.setText("+ ¥" + money);
                }else if (type.equals("提现")){

                    tv_type.setText(type);
                    tv_money.setTextColor(getResources().getColor(R.color.colorRed));
                    tv_money.setText("- ¥" + money);
                }

            }else if (moneyFloat < 0 && type.equals("配送费")){

                tv_type.setText("配送违规处罚");
                float moneyAbs = Math.abs(moneyFloat);
                tv_money.setText("- ¥" + moneyAbs);
            }

            //订单号
            TextView tv_orderId = v.findViewById(R.id.tv_orderId);
            String orderId = bill.getOrderId();
            if (orderId != null){

                tv_orderId.setVisibility(View.VISIBLE);
                tv_orderId.setText("订单号：" + orderId);
            }else {
                tv_orderId.setVisibility(View.GONE);
            }

            return v;
        }
    }

}
