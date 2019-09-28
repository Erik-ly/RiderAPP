package com.bophia.erik.ykxrider.UI.neworder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.entity.Business;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.DateUtils;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.StringUtils;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * “订单详情”Activity
 */
public class OrderInfoActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;
    private int relationSalepoint;

    private int id;
    private String orderId;
    private String time;
    private String paymentTime;
    private String userName;
    private String userPhone;
    private int userSex;
    private String userSite;
    private String userSiteDetails;
    private int orderType;
    private int orderWhether;
    private String distributionStartTime;
    private String remark;
    private String booking;
    private int throwCount;
    private String disMoney;

    private List<Order> list = new ArrayList<>();
    private JSONArray goodsOrderInfoList;
    private JSONObject businessMap;
    private JSONObject businessGoodsMap;


    private TextView tv_userPhone;

    private Button bt_throwOut;
    private LinearLayout ll_doubleButton;
    private Button bt_receipt;
    private Button bt_grab;
    private Button bt_navigation;
    private Button bt_callBusiness;
    private Button bt_callUser;
    private Button bt_pickup;
    private Button bt_doneDistribution;
    private Button bt_outBound;
    private Button bt_distribution;
    private Button bt_completeDistribution;
    private String verificationcode;

//    private int  checkedId;
//    private EditText et_paymentAmount;
//    private String paymentAmount;

    private Map<String,Business> choiceBusinessMap = new HashMap<>();//存放选择的商户相关信息
    private Map<String,Business> existBusinessMap = new HashMap<>();//存放已有的查询到的已有的商户相关信息

    private Map<String,List<Business>> tempBusinessMap = new HashMap<>();//存放查询到的原始数据

    //商家信息列表
    List<Business> businessList = new ArrayList<>();

    private AVLoadingIndicatorView avi;

    @Override
    protected void onResume() {
        super.onResume();

        list.clear();

        //获取订单及订单详情信息
        new Thread(new Runnable() {
            @Override
            public void run() {

                String httpsUrl = getString(R.string.httpsUrl);
                String url = httpsUrl + "GetDisOrderByOrderId";

                //加载动画
                avi.show();

                String result = HttpRequest.sendGet(url,"orderId=" + orderId);

                //判断网络是否正常
                if (result.equals("404")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderInfoActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //解析数据
                try {
                    JSONObject NoDisOrder = new JSONObject(result);

//                    //商户相关信息
//                    businessMap = NoDisOrder.optJSONObject("businessMap");
//
//                    //商户商品相关信息
//                    businessGoodsMap = NoDisOrder.optJSONObject("businessGoodsMap");

                    //商户相关信息
                    businessMap = NoDisOrder.optJSONObject("businessMap");

                    //商户商品相关信息
                    businessGoodsMap = NoDisOrder.optJSONObject("businessGoodsMap");

                    //当状态为已取货和已送达时，获取businessGoodsId存放在existBusinessMap中
                    if ((orderWhether ==2 || orderWhether == 4) && businessGoodsMap != null){

                        System.out.println("OrderInfoActivity===========我执行到这里了！" );

                        for (Map.Entry<String, Business> entry : existBusinessMap.entrySet()){

                            String goodsId = entry.getKey();
                            Business business = entry.getValue();
                            String businessId = business.getBusinessId();

                            JSONArray businessGoodsMapArray = businessGoodsMap.optJSONArray(goodsId);

                            for (int i=0; i<businessGoodsMapArray.length(); i++){
                                JSONObject bgObject = businessGoodsMapArray.getJSONObject(i);

                                String bgId = bgObject.optString("businessGoodsId");

                                JSONObject businessInfoObject = bgObject.optJSONObject("businessInfo");

                                String bId = businessInfoObject.optString("businessId");

                                if (businessId.equals(bId)){
                                    business.setBusinessGoodsId(Integer.parseInt(bgId));
                                }

                            }
                        }
                    }

                    //存储tempBusinessMap信息
                    //遍历businessGoodsMap
                    Iterator<String> sIterator = businessGoodsMap.keys();
                    while (sIterator.hasNext()){

                        String goodsId = sIterator.next();
                        JSONArray businessGoodsMapArray = businessGoodsMap.optJSONArray(goodsId);

                        List<Business> businessList = new ArrayList<>();
                        if (businessGoodsMapArray != null && businessGoodsMapArray.length()>0){

                            for (int a=0; a<businessGoodsMapArray.length(); a++){
                                JSONObject businessGoodsObject = businessGoodsMapArray.getJSONObject(a);

                                int businessGoodsId = businessGoodsObject.optInt("businessGoodsId");
                                String price = businessGoodsObject.optString("price");

                                JSONObject businessInfoObject = businessGoodsObject.optJSONObject("businessInfo");
                                String businessId = businessInfoObject.optString("businessId");
                                String businessName = businessInfoObject.optString("businessName");
                                String businessAddress = businessInfoObject.optString("businessAddress");
                                String businessPhone = businessInfoObject.optString("businessPhone");

                                Business business = new Business();
                                business.setBusinessGoodsId(businessGoodsId);
                                business.setPrice(price);
                                business.setBusinessId(businessId);
                                business.setBusinessName(businessName);
                                business.setBusinessPhone(businessPhone);
                                business.setBusinessAddress(businessAddress);

                                businessList.add(business);
                                tempBusinessMap.put(goodsId,businessList);

                            }
                        }
                    }

                    //订单相关信息
                    JSONObject content = NoDisOrder.optJSONObject("content");

                    if (content != null){
                        id = content.optInt("id");
                        orderType = content.optInt("orderType");
                        orderWhether = content.optInt("whether");
                        time = content.optString("time");
                        paymentTime = content.optString("paymentTime");
                        userName = content.optString("userName");
                        userSex = content.optInt("userSex");
                        userPhone = content.optString("userPhone");
                        userSite = content.optString("userSite");
                        userSiteDetails = content.optString("userSiteDetails");
                        remark = content.optString("remark");
                        booking = content.optString("booking");
                        throwCount = content.optInt("throwCount");
                        disMoney = content.getString("disMoney");

                        goodsOrderInfoList = content.optJSONArray("goodsOrderInfoList");

                        List<Business> tempBusinessList = new ArrayList<>();//存放商户相关信息列表
                        for (int i=0; i<goodsOrderInfoList.length(); i++){
                            JSONObject orderObject = goodsOrderInfoList.getJSONObject(i);

                            //获取订单详情基本信息
                            Order htOrderInfo = new Order();
                            String goodsId = orderObject.optJSONObject("goods").optString("goodsId");
                            int goodsNum = orderObject.optInt("goodsNum");
                            htOrderInfo.setGoodsId(goodsId);
                            String goodsName = orderObject.optString("goodsName");
                            htOrderInfo.setGoodsName(goodsName);
                            htOrderInfo.setGoodsNum(goodsNum);
                            htOrderInfo.setGoodsPrice(Float.parseFloat(orderObject.optString("goodsPrice")));
                            htOrderInfo.setGoodsOperate(orderObject.optInt("goodsOperate"));

                            //获取商户相关信息
                            Business business = new Business();
                            business.setBusinessId(orderObject.getString("businessId"));
                            business.setActualPayment(orderObject.getString("actualPayment"));
                            existBusinessMap.put(goodsId,business);

                            //存放tempBusinessMap
                            List<Business> businessList = tempBusinessMap.get(goodsId);

                            if (businessList != null && businessList.size()>0){
                                for (Business b: businessList){
                                    b.setGoodsName(goodsName);
                                    b.setGoodsNum(goodsNum);
                                }
                            }

                            //获取商品基本信息
                            htOrderInfo.setIcon(orderObject.optJSONObject("goods").optString("icon"));
                            htOrderInfo.setIfDispose(orderObject.optJSONObject("goods").optInt("ifDispose"));
                            htOrderInfo.setOperateFee(Float.parseFloat(orderObject.optJSONObject("goods").optString("operateFee")));
                            htOrderInfo.setResidueWeight(orderObject.optJSONObject("goods").optString("residueWeight"));
                            htOrderInfo.setDeviation(orderObject.optJSONObject("goods").optString("deviation"));

                            //数据放入列表中
                            list.add(htOrderInfo);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //显示数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //隐藏动画
                        avi.hide();

                        /**
                         * 显示订单基本信息
                         */
                        //找到控件
                        TextView tv_orderId = findViewById(R.id.tv_oiOrderId);
                        TextView tv_orderTime = findViewById(R.id.tv_oiOrderTime);
                        TextView tv_userName = findViewById(R.id.tv_oiUserName);
                        TextView tv_userSex = findViewById(R.id.tv_oiUserSex);
                        tv_userPhone = findViewById(R.id.tv_oiUserPhone);
                        TextView tv_userSite = findViewById(R.id.tv_oiUserSite);

                        //设置数据
                        tv_orderId.setText(id + "-" + orderId);
                        tv_orderTime.setText(time);
                        tv_userName.setText(userName);

                        //性别
                        if (userSex == 1){
                            tv_userSex.setText("(先生)");
                        }else if (userSex == 2){
                            tv_userSex.setText("(女士)");
                        }else if (userSex == 0){
                            tv_userSex.setText("(未知)");
                        }

                        tv_userPhone.setText(userPhone);
                        tv_userPhone.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
                        tv_userPhone.getPaint().setAntiAlias(true);//亢锯齿

                        tv_userSite.setText(userSite + userSiteDetails);

                        //备注
                        LinearLayout ll_remark = findViewById(R.id.ll_oiRemark);
                        TextView tv_remark = findViewById(R.id.tv_oiRemark);
                        if (!StringUtils.isNullOrEmpty(remark) || remark.equals("备注")){
                            ll_remark.setVisibility(View.GONE);
                        }else {

                            String remarkDe = "";
                            try {
                                remarkDe = URLDecoder.decode(remark,"UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            ll_remark.setVisibility(View.VISIBLE);
                            tv_remark.setText(remarkDe);

                        }

                        //预约订单
                        LinearLayout ll_booking = findViewById(R.id.ll_booking);
                        if (booking.equals("尽快送达")){
                            ll_booking.setVisibility(View.GONE);
                        }else {
                            ll_booking.setVisibility(View.VISIBLE);
                            TextView tv_booking = findViewById(R.id.tv_booking);
                            tv_booking.setText(booking);
                        }

                        //本单收入
                        TextView tv_disMoney = findViewById(R.id.tv_disMoney);
                        if (orderWhether == 4){

                            String str="本单收入 <font color='#EC823C'>" + "¥" + disMoney + "</font> 元";
                            tv_disMoney.setText(Html.fromHtml(str));

                        }else {

                            String estimateDisMoney = DateUtils.estimateDisMoney(paymentTime);
                            String str="预计本单收入 <font color='#d8822a'>" + "¥" + estimateDisMoney + "</font> 元";
                            tv_disMoney.setText(Html.fromHtml(str));
                        }

                        /**
                         * 显示订单详情信息
                         */
                        //显示物品列表
                        LinearLayout ll_goodsList = findViewById(R.id.ll_goodsList);
                        ll_goodsList.setVisibility(View.VISIBLE);

                        //设置ListView
                        ListView lv_orderInfo = findViewById(R.id.lv_orderInfo);
                        lv_orderInfo.setAdapter(new OrderInfoAdapter());

                        //点击商品名称显示图片
                        lv_orderInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                //找到对应的item
                                Order goodsInfo = list.get(i);
                                String goodsName = goodsInfo.getGoodsName();
                                String goodsIcon = goodsInfo.getIcon();
                                final String goodsId = goodsInfo.getGoodsId();

                                LayoutInflater li = LayoutInflater.from(OrderInfoActivity.this);

                                View view_goodsinfo = li.inflate(R.layout.goodsinfo_scrollview,null);

                                WebView wv_disUseHelp = view_goodsinfo.findViewById(R.id.wv_goodsIcon);

                                //设置商品名
                                TextView goodsname = view_goodsinfo.findViewById(R.id.tv_goodsName);
                                goodsname.setText(goodsName);

                                //设置商品图片
                                wv_disUseHelp.loadUrl(goodsIcon);

                                //应用内打开
                                wv_disUseHelp.setWebViewClient(new WebViewClient(){
                                    @Override
                                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                                        view.loadUrl(url);
                                        return true;
                                    }
                                });

                                //输入价格
                                LinearLayout ll_paymentAmount = view_goodsinfo.findViewById(R.id.ll_paymentAmount);
                                final EditText et_paymentAmount = view_goodsinfo.findViewById(R.id.et_paymentAmount);

                                //商家信息
                                final RadioGroup rg_businessInfo = view_goodsinfo.findViewById(R.id.rg_businessInfo);

                                //设置布局
                                RadioGroup.LayoutParams params_rb = new RadioGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                                params_rb.setMargins(0, 20, 0, 0);//设置间距

                                //设置监听，同时改变et_paymentAmount的值
                                rg_businessInfo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup radioGroup, int i) {

                                        //获取选中的id
                                        int checkedRadioButtonId = rg_businessInfo.getCheckedRadioButtonId();

                                        List<Business> businessList = tempBusinessMap.get(goodsId);

                                        if (businessList != null && businessList.size()>0){
                                            for (Business business : businessList){
                                                int businessGoodsId = business.getBusinessGoodsId();
//                                                String businessId = business.getBusinessId();

                                                if (checkedRadioButtonId == businessGoodsId){

                                                    int goodsNum = business.getGoodsNum();
                                                    String price = business.getPrice();
                                                    float priceFloat = Float.parseFloat(price);
                                                    float tempPaymentAmount = priceFloat * goodsNum;

                                                    et_paymentAmount.setText(tempPaymentAmount + "");

                                                }
                                            }
                                        }
                                    }
                                });

                                /**
                                 *  当不同状态显示不同的内容：
                                 *  未接单：RadioGroup不能点选；ll_paymentAmount不显示
                                 *  已接单未出货：RadioGroup能点选；ll_paymentAmount显示
                                 *  已出货未送达和已送达：RadioGroup不能点选，有默认已选项；ll_paymentAmount显示，显示已有价格
                                 */

                                try {
                                    //解析商户商品信息
                                    JSONArray businessGoodsMapArray = businessGoodsMap.optJSONArray(goodsId);

                                    //判断是否有数据，如果有说明是第三方商家商品，解析显示
                                    if (businessGoodsMapArray != null && businessGoodsMapArray.length()>0){

                                        for (int a=0; a<businessGoodsMapArray.length(); a++){
                                            JSONObject businessGoodsObject = businessGoodsMapArray.getJSONObject(a);

                                            //获取商户商品信息
                                            int businessGoodsId = businessGoodsObject.optInt("businessGoodsId");
                                            String price = businessGoodsObject.optString("price");

                                            //解析商户信息
                                            JSONObject businessInfo = businessGoodsObject.getJSONObject("businessInfo");

                                            //获取商户基本信息
                                            String businessId = businessInfo.optString("businessId");
                                            String businessName = businessInfo.optString("businessName");
                                            String businessAddress = businessInfo.optString("businessAddress");
                                            String businessPhone = businessInfo.optString("businessPhone");

                                            /**
                                             * 当未接单时，不让选择，当接单后才能选择
                                             */

                                            //添加RadioButton
                                            RadioButton radioButton = new RadioButton(OrderInfoActivity.this);

                                            System.out.println("OrderInfoActivity:==========businessId：" + businessId);

                                            radioButton.setId(businessGoodsId);//设置RadioButton的id

                                            //设置默认选中项
                                            if (orderWhether ==1){

                                                Business choiceBusiness = choiceBusinessMap.get(goodsId);
                                                if (choiceBusiness != null){
                                                    int checkedId = choiceBusiness.getBusinessGoodsId();
                                                    radioButton.setChecked(businessGoodsId == checkedId);//默认选中项
                                                }
                                            }else if (orderWhether == 2 || orderWhether == 4){

                                                Business existBusiness = existBusinessMap.get(goodsId);
                                                if (existBusiness != null){

                                                    int checkedId = existBusiness.getBusinessGoodsId();
                                                    radioButton.setChecked(businessGoodsId == checkedId);//默认选中项
                                                }

                                            }

                                            radioButton.setSingleLine(false);//多行显示
                                            radioButton.setText("商  户  名：" + businessName +"\n" + " 商户地址："+ businessAddress +"\n"+ " 商户电话："+ businessPhone +"\n"+ " 价        格：¥"+ price + "元");
                                            radioButton.setTextColor(getResources().getColor(R.color.colorTitle));
                                            radioButton.setTextSize(getResources().getDimensionPixelSize(R.dimen.radioButton_text));
                                            rg_businessInfo.addView(radioButton, params_rb);

                                        }

                                        //未接单时：RadioGroup不能点选；ll_paymentAmount默认不显示
                                        if (orderWhether == 0){

                                            //设置rg_businessInfo都不能点击
                                            for (int m = 0; m < rg_businessInfo.getChildCount(); m++){
                                                rg_businessInfo.getChildAt(m).setClickable(false);
                                            }

                                        //已接单未出货：RadioGroup能点选；ll_paymentAmount显示
                                        }else if (orderWhether == 1){

                                            //显示价格输入框
                                            ll_paymentAmount.setVisibility(View.VISIBLE);

                                            //如果choiceBusinessMap有数据，显示已有数据
                                            Business choiceBusiness = choiceBusinessMap.get(goodsId);
                                            if (choiceBusiness != null){
                                                String actualPayment = choiceBusiness.getActualPayment();
                                                et_paymentAmount.setText(actualPayment);
                                            }

                                        //已取货或已送达：RadioGroup不能能点选；显示已有金额，并且输入框不能编辑
                                        }else if (orderWhether == 2 || orderWhether == 4){

                                            //设置rg_businessInfo都不能点击
                                            for (int m = 0; m < rg_businessInfo.getChildCount(); m++){
                                                rg_businessInfo.getChildAt(m).setClickable(false);
                                            }

                                            //显示价格输入框
                                            ll_paymentAmount.setVisibility(View.VISIBLE);

                                            //显示已有金额，并且不能编辑
                                            Business existBusiness = existBusinessMap.get(goodsId);
                                            if (existBusiness != null){
                                                String actualPayment = existBusiness.getActualPayment();
                                                et_paymentAmount.setText(actualPayment);
//                                                et_paymentAmount.setFocusable(false);
                                                et_paymentAmount.setFocusableInTouchMode(false);

                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //弹窗
                                final AlertDialog dialog = new AlertDialog.Builder(OrderInfoActivity.this).create();
                                dialog.setTitle("商品信息");
                                dialog.setView(view_goodsinfo);
                                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //当未待取货状态并且如果该商品在tempBusinessMap中有信息，说明是第三方商户商品，判断是否获取商品
                                        if (orderWhether == 1 && tempBusinessMap.get(goodsId) != null){

                                            //获取选中的商户
                                            int checkedRadioButtonId = rg_businessInfo.getCheckedRadioButtonId();
                                            System.out.println("OrderInfoActivity:==========dialog:checkedRadioButtonId:" + checkedRadioButtonId);

                                            //获取输入的金额
                                            String paymentAmount = et_paymentAmount.getText().toString().trim();
                                            System.out.println("OrderInfoActivity:==========dialog:paymentAmount:" + paymentAmount);

                                            //判断是否选择了商家
                                            if (checkedRadioButtonId == -1){

                                                Toast.makeText(OrderInfoActivity.this,"请选择商家！",Toast.LENGTH_LONG).show();

                                                return;

                                            }


                                            String bId = null;//商户id
                                            String price = null;//商户商品价格
                                            int goodsNum = 0;//商品数量
                                            List<Business> tempBusinessList = tempBusinessMap.get(goodsId);
                                            for (Business tempBusiness : tempBusinessList){

                                                if (tempBusiness.getBusinessGoodsId() == checkedRadioButtonId){
                                                    bId = tempBusiness.getBusinessId();
                                                    price = tempBusiness.getPrice();
                                                    goodsNum = tempBusiness.getGoodsNum();

                                                    Business choiceBusiness = new Business();
                                                    choiceBusiness.setGoodsId(goodsId);
                                                    choiceBusiness.setBusinessId(bId);
                                                    choiceBusiness.setBusinessGoodsId(checkedRadioButtonId);
                                                    choiceBusinessMap.put(goodsId,choiceBusiness);

                                                    break;

                                                }
                                            }

                                            //判断输入金额
                                            //判断是否为空
                                            if (!StringUtils.isNullOrEmpty(paymentAmount)){

                                                Toast.makeText(OrderInfoActivity.this,"请输入付款金额",Toast.LENGTH_LONG).show();

                                                //判断输入的金额是否满足条件
                                            }else {

                                                float paymentAmountFloat = Float.parseFloat(paymentAmount);

                                                //判断输入金额是否满足条件：商户商品单价*购买数量 + 商户商品单价*0.2 = 商户商品单价 * (购买数量 + 0.2）
                                                Float maxAmount = Float.parseFloat(price) * (goodsNum + 0.2f);
                                                Float minAmount = Float.parseFloat(price) * (goodsNum - 0.2f);

                                                if (paymentAmountFloat < minAmount || paymentAmountFloat > maxAmount){

                                                    Toast.makeText(OrderInfoActivity.this,"支付金额不能超过商品单价×(±100g)！",Toast.LENGTH_LONG).show();

                                                }

                                                //设置支付金额
                                                Business choiceBusiness = choiceBusinessMap.get(goodsId);
                                                choiceBusiness.setActualPayment(String.valueOf(paymentAmountFloat));

                                            }
                                        }
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

                        /**
                         * 根据不同状态显示不同按钮
                         */

                        //新订单或大厅订单状态
                        if (orderWhether == 0 ){

                            //显示底部按钮
                            LinearLayout ll_button = findViewById(R.id.ll_button);
                            ll_button.setVisibility(View.VISIBLE);

                            //新订单
                            if (throwCount == 0){

                                //显示“接单”按钮
                                bt_receipt = findViewById(R.id.bt_receipt);
                                bt_receipt.setVisibility(View.VISIBLE);

                                //大厅订单
                            }else if (throwCount == 1){

                                //显示“抢单”按钮
                                bt_grab = findViewById(R.id.bt_grab);
                                bt_grab.setVisibility(View.VISIBLE);

                            }

                            //接单后“待取货”状态
                        }else if (orderWhether == 1){

                            //显示底部按钮
                            LinearLayout ll_button = findViewById(R.id.ll_button);
                            ll_button.setVisibility(View.VISIBLE);

                            //显示“查看路线”按钮
                            bt_navigation = findViewById(R.id.bt_navigation);
                            bt_navigation.setVisibility(View.VISIBLE);

                            //显示“联系商家”按钮
                            bt_callBusiness =findViewById(R.id.bt_callBusiness);
                            bt_callBusiness.setVisibility(View.VISIBLE);

                            //显示“我已取货”按钮
                            bt_pickup = findViewById(R.id.bt_pickup);
                            bt_pickup.setVisibility(View.VISIBLE);

                            //“已取货”状态（原“已出库状态”）
                        }else if (orderWhether == 2){

                            //显示底部按钮
                            LinearLayout ll_button = findViewById(R.id.ll_button);
                            ll_button.setVisibility(View.VISIBLE);

                            //显示“查看路线”按钮
                            bt_navigation = findViewById(R.id.bt_navigation);
                            bt_navigation.setVisibility(View.VISIBLE);

                            //显示“联系买家”按钮
                            bt_callUser =findViewById(R.id.bt_callUser);
                            bt_callUser.setVisibility(View.VISIBLE);

                            //显示“我已送达”按钮
                            bt_doneDistribution = findViewById(R.id.bt_doneDistribution);
                            bt_doneDistribution.setVisibility(View.VISIBLE);

                            //其余情况
                        }else {

                            //隐藏底部按钮
                            LinearLayout ll_button = findViewById(R.id.ll_button);
                            ll_button.setVisibility(View.GONE);

                        }

                        /**
                         * 按钮监听事件
                         */
                        //“接单”按钮监听
                        bt_receipt = findViewById(R.id.bt_receipt);
                        bt_receipt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final AlertDialog dialog2 = new AlertDialog.Builder(OrderInfoActivity.this).create();
                                dialog2.setTitle("确认接单");
                                dialog2.setMessage("是否确认接单？");
                                dialog2.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new ConfirmationReceipt().onClick(null);
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


                        //“抢单”按钮监听
                        bt_grab = findViewById(R.id.bt_grab);
                        bt_grab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final AlertDialog dialog2 = new AlertDialog.Builder(OrderInfoActivity.this).create();
                                dialog2.setTitle("确认抢单");
                                dialog2.setMessage("是否确认抢单？");
                                dialog2.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new ConfirmationGrab().onClick(null);
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


                        //“我已取货”按钮监听
                        bt_pickup = findViewById(R.id.bt_pickup);
                        bt_pickup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                /**
                                 * 判断第三方商户的商品是否都已选好商家和填好价格
                                 *
                                 * 判断有没有第三方商户商品:如果有第三方商家商品，判断是否有未选择商家或价格，如果有弹出提示
                                 * 如果没有第三方商户商品，或已经选择了第三方商户商品，弹出是否出库框
                                 */

                                //判断有没有第三方商户商品
                                //如果有第三方商家商品
                                if (tempBusinessMap != null && tempBusinessMap.size() >0){

                                    Boolean choiceBusinessState = true;
                                    String tempGoodsName = "";//存放未选择商户或价格的商品
                                    for (Map.Entry<String, List<Business>> tempEntry : tempBusinessMap.entrySet()){

                                        String goodsId = tempEntry.getKey();

                                        //获取已选择商户信息
                                        Business choiceBusiness = choiceBusinessMap.get(goodsId);

                                        //判断是否有未选择商家或价格
                                        if (choiceBusiness == null || choiceBusiness.getBusinessId() == null || choiceBusiness.getActualPayment() == null){

                                            choiceBusinessState = false;
                                            tempGoodsName += tempBusinessMap.get(goodsId).get(0).getGoodsName() + ";";

                                        }
                                    }

                                    //假如有未选择商家或价格，显示商品未选择商家提示
                                    if (choiceBusinessState == false){
                                        String noChoiceBusinessGoods = tempGoodsName.substring(0,tempGoodsName.length()-1);
                                        Toast.makeText(OrderInfoActivity.this,"商品：" + noChoiceBusinessGoods +" 未选择商家或价格",Toast.LENGTH_LONG).show();

                                        return;
                                    }

                                }

                                //当没有第三方商家商品或已经选择完后，弹出是否出库对话框
                                final AlertDialog dialog2 = new AlertDialog.Builder(OrderInfoActivity.this).create();
                                dialog2.setTitle("确认取货");
                                dialog2.setMessage("是否确认已取到商品？");
                                dialog2.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        new ConfirmationOutBound().onClick(null);

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

                        //“我已送达”按钮监听
                        bt_doneDistribution = findViewById(R.id.bt_doneDistribution);
                        bt_doneDistribution.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //验证码布局
                                LayoutInflater li = LayoutInflater.from(OrderInfoActivity.this);
                                final View view_verificationcode = li.inflate(R.layout.verificationcode,null);

                                final AlertDialog dialog = new AlertDialog.Builder(OrderInfoActivity.this).create();
                                dialog.setTitle("完成配送");
                                dialog.setView(view_verificationcode);
                                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //获取验证码
                                        EditText editText = view_verificationcode.findViewById(R.id.et_verificationCode);
                                        verificationcode = editText.getText().toString().trim();

                                        new CompleteDistribution().onClick(null);

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

                        //拨打电话
                        tv_userPhone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String disHelpPhone = tv_userPhone.getText().toString().trim();
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+disHelpPhone));//跳转到拨号界面，同时传递电话号码
                                startActivity(intent);

                            }
                        });
                    }
                });
            }
        }).start();



    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderinfo_layout);

        //自定义ActionBar
        setCustomActionBar();

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");
        relationSalepoint = sp.getInt("relationSalepoint",-1);

        //获取orderid
        final Bundle bundle = getIntent().getExtras();
        orderId = bundle.getString("orderId");

        //加载动画
        avi = findViewById(R.id.avi_loading);

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                avi.show();
//            }
//        });

    }

    //设置自定义ActionBar
    private void setCustomActionBar(){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        TextView tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        tv_actionBarTitle.setText("订单详情");

        LinearLayout ll_back = mActionBarView.findViewById(R.id.ll_back);
        ll_back.setVisibility(View.VISIBLE);

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

    //商品详情列表Adapter
    private class OrderInfoAdapter extends BaseAdapter{

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
                v = View.inflate(getApplicationContext(),R.layout.goodslist_item,null);
            }else {
                v = view;
            }

//            System.out.println("HistoryOrderActivity=============orderInfoActivity" + list.size());

            Order goodsList = list.get(i);

            //商品图片
//            WebView wv_goodsImg = v.findViewById(R.id.wv_goodsImg);
//            MyWebView wv_goodsImg = v.findViewById(R.id.mwv_goodsImg);
//            wv_goodsImg.loadUrl(goodsList.getIcon());
//            wv_goodsImg.getSettings().setUseWideViewPort(true);
//            wv_goodsImg.getSettings().setLoadWithOverviewMode(true);//设置图片缩放

            //商品名称
            TextView tv_goodsName = v.findViewById(R.id.tv_goodsName);
            tv_goodsName.setText(goodsList.getGoodsName());

            //自营状态
            TextView tv_selfSellState = v.findViewById(R.id.tv_selfSellState);

            //获取商户信息
//            System.out.println("OrderInfoActivity=====================goodsList.getGoodsId():" + goodsList.getGoodsId());

            String goodsId = goodsList.getGoodsId();

            JSONArray businessInfoArray = businessMap.optJSONArray(goodsId);
            if (businessInfoArray != null && businessInfoArray.length()>0){
                tv_selfSellState.setText("否");
            }else {
                tv_selfSellState.setText("是");
            }

            //数量
            TextView tv_goodsNum = v.findViewById(R.id.tv_goodsNum);
            tv_goodsNum.setText("×" + goodsList.getGoodsNum());

            //价格
            TextView tv_goodsPrice = v.findViewById(R.id.tv_goodsPrice);
            tv_goodsPrice.setText("¥" + goodsList.getGoodsPrice());

            //处理
            if (goodsList.getGoodsOperate() == 1){
                TextView tv_disposeInfo = v.findViewById(R.id.tv_disposeInfo);
                tv_disposeInfo.setVisibility(View.VISIBLE);

                tv_disposeInfo.setText("处理详情：" + "操作费：" + goodsList.getOperateFee() + " 处理之后重量：" + goodsList.getResidueWeight() + " 商品误差：" + goodsList.getDeviation());

            }
            return v;
        }
    }

    //“接单”按钮事件
    private class ConfirmationReceipt implements View.OnClickListener{

        private int state;
        private String message;

        @Override
        public void onClick(View view) {

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

                    String result = HttpRequest.sendGet(url,"id=" + id + "&staffId=" + distributorId + "&distributorName=" + distributorNameEncoded + "&distributorPhone=" + distributorPhone);

                    //判断网络是否正常
                    if (result.equals("404")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }

                    try {
                        JSONObject updateDisOrderReceipt = new JSONObject(result);

                        state = updateDisOrderReceipt.optInt("state");
                        message = updateDisOrderReceipt.optString("message");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (state == 0){

                        //修改orderWhether
//                        orderWhether = 1;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(OrderInfoActivity.this,"接单成功！",Toast.LENGTH_LONG).show();

                                //刷新数据
                                onResume();

                                //隐藏“接单”按钮
                                bt_receipt.setVisibility(View.GONE);

                                //显示“查看路线”、“联系商家”和“我已取货”按钮
                                //显示底部按钮
                                LinearLayout ll_button = findViewById(R.id.ll_button);
                                ll_button.setVisibility(View.VISIBLE);

                                //显示“查看路线”按钮
                                bt_navigation = findViewById(R.id.bt_navigation);
                                bt_navigation.setVisibility(View.VISIBLE);

                                //显示“联系商家”按钮
                                bt_callBusiness =findViewById(R.id.bt_callBusiness);
                                bt_callBusiness.setVisibility(View.VISIBLE);

                                //显示“我已取货”按钮
                                bt_pickup = findViewById(R.id.bt_pickup);
                                bt_pickup.setVisibility(View.VISIBLE);

                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,message,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        }
    }

    //“抢单”按钮事件
    private class ConfirmationGrab implements View.OnClickListener{

        private int state;
        private String message;

        @Override
        public void onClick(View view) {

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

//                    String result = HttpRequest.sendGet(url,"id=" + id + "&distributorName=" + distributorNameEncoded + "&distributorPhone=" + distributorPhone);

                    String result = HttpRequest.sendGet(url,"id=" + id + "&staffId=" + distributorId + "&distributorName=" + distributorNameEncoded + "&distributorPhone=" + distributorPhone);


                    //判断网络是否正常
                    if (result.equals("404")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }

                    try {
                        JSONObject updateDisOrderReceipt = new JSONObject(result);

                        state = updateDisOrderReceipt.optInt("state");
                        message = updateDisOrderReceipt.optString("message");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (state == 0){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(OrderInfoActivity.this,"接单成功！",Toast.LENGTH_LONG).show();

                                //刷新数据
                                onResume();

                                //隐藏“抢单”按钮
                                bt_receipt.setVisibility(View.GONE);

                                //显示“查看路线”、“联系商家”和“我已取货”按钮
                                //显示底部按钮
                                LinearLayout ll_button = findViewById(R.id.ll_button);
                                ll_button.setVisibility(View.VISIBLE);

                                //显示“查看路线”按钮
                                bt_navigation = findViewById(R.id.bt_navigation);
                                bt_navigation.setVisibility(View.VISIBLE);

                                //显示“联系商家”按钮
                                bt_callBusiness =findViewById(R.id.bt_callBusiness);
                                bt_callBusiness.setVisibility(View.VISIBLE);

                                //显示“我已取货”按钮
                                bt_pickup = findViewById(R.id.bt_pickup);
                                bt_pickup.setVisibility(View.VISIBLE);

                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,message,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        }
    }

    //“我已取货”按钮事件
    private class ConfirmationOutBound implements View.OnClickListener{
        private int state;
        private String message;

        @Override
        public void onClick(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String httpsUrl = getString(R.string.httpsUrl);
                    String url = httpsUrl + "UpdateDisOrderOutbound";

//                    String result = HttpRequest.sendGet(url,"id=" + id);

                    JSONObject businessObject = new JSONObject();

                    for (Map.Entry<String, Business> entry : choiceBusinessMap.entrySet()){

                        String goodsId = entry.getKey();
                        Business business = entry.getValue();
                        String businessId = business.getBusinessId();
                        String actualPayment = business.getActualPayment();

                        try {
                            businessObject.put(goodsId,businessId + "#" + actualPayment);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    String result = HttpRequest.sendPost(url,"id=" + id + "&selectedBusiness=" + businessObject.toString());

                    //判断网络是否正常
                    if (result.equals("404")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }

                    try {
                        JSONObject updateDisOrderOutbound = new JSONObject(result);

                        state = updateDisOrderOutbound.optInt("state");
                        message = updateDisOrderOutbound.optString("message");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (state == 0){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //刷新数据
                                onResume();

                                //隐藏“联系商家”和“我已取货”按钮
                                bt_callBusiness.setVisibility(View.GONE);
                                bt_pickup.setVisibility(View.GONE);

                                //显示“联系买家”和“我已送达”按钮
                                //显示“联系买家”按钮
                                bt_callUser =findViewById(R.id.bt_callUser);
                                bt_callUser.setVisibility(View.VISIBLE);

                                //显示“我已送达”按钮
                                bt_doneDistribution = findViewById(R.id.bt_doneDistribution);
                                bt_doneDistribution.setVisibility(View.VISIBLE);
                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,message,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    //“我已送达”按钮监听事件
    private class CompleteDistribution implements View.OnClickListener{

        private int state;
        private String message;

        @Override
        public void onClick(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String url = getString(R.string.httpsUrl) + "UpdateDisOrderDisEnd";

                    String result = HttpRequest.sendGet(url,"id=" + id + "&staffId=" + distributorId + "&verificationCode=" + verificationcode);

                    //判断网络是否正常
                    if (result.equals("404")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }

                    try {
                        JSONObject updateDisOrderDisEnd = new JSONObject(result);

                        state = updateDisOrderDisEnd.optInt("state");
                        message = updateDisOrderDisEnd.optString("message");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (state == 0){

                        //微信发送系统通知
                        HttpRequest.sendGet(getString(R.string.messageUrl),"orderId="+orderId);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,message,Toast.LENGTH_LONG).show();

//                                //刷新数据
//                                onResume();

                                //隐藏底部按钮
                                LinearLayout ll_button = findViewById(R.id.ll_button);
                                ll_button.setVisibility(View.GONE);

                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OrderInfoActivity.this,message,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();
        }
    }


}
