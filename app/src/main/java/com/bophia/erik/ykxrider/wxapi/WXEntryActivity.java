package com.bophia.erik.ykxrider.wxapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.MyApplication;
import com.bophia.erik.ykxrider.R;
//import com.bophia.erik.ykxrider.pay.WXPayWithdrawActivity;
//import com.bophia.erik.ykxrider.pay.WXPayWithdrawActivity;
import com.bophia.erik.ykxrider.UI.mine.wallet.WalletActivity;
import com.bophia.erik.ykxrider.utils.EncryptionUtil;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * “提现到微信”activity
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler{

    //配速员信息
    private SharedPreferences sp;
    private int staffId;

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    private String openid;
    private String code;

    private int count = 0;

    //微信开放平台申请的APPID
    private static final String APP_ID = "wx817cbea5c383cf64";
    private static final String APP_Secret = "31bf6f084c86aadb0e3cd75c62721518";

    String nameEncoded= null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //加载布局
        setContentView(R.layout.withdrawtowx);

        //自定义ActionBar
        setCustomActionBar();

        //获取配送员信息
        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        staffId = sp.getInt("id",-1);

//        TextView tv_withdrawBalance = findViewById(R.id.tv_withdrawBalance);
//        tv_withdrawBalance.setText("¥" + balance + "元");

        api = WXAPIFactory.createWXAPI(this,APP_ID,true);

//        将应用的appid注册到微信
        api.registerApp(APP_ID);


        //微信登录按钮
        Button bt_wxLogin = findViewById(R.id.bt_wxLogin);
        bt_wxLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //微信登录
                wxLogin();

            }
        });

        try {
            api.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //"提现"按钮
        Button bt_wxPayWithdraw = findViewById(R.id.bt_wxPayWithdraw);
        bt_wxPayWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取数据
                //获取姓名
                EditText et_wxPayName = findViewById(R.id.et_wxPayName);
                String name = et_wxPayName.getText().toString().trim();

                //获取微信号
                EditText et_weChatId = findViewById(R.id.et_weChatId);
                String weChatId = et_weChatId.getText().toString().trim();

                //获取金额
                EditText et_wxPayAmount = findViewById(R.id.et_wxPayAmount);
                final String amount = et_wxPayAmount.getText().toString().trim();

                //获取提现密码
                EditText et_withdrawPassword = findViewById(R.id.et_withdrawPassword);
                String withdrawPassword = et_withdrawPassword.getText().toString().trim();

                //判断信息是否为空
                //判断姓名是否为空
                if (TextUtils.isEmpty(name)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WXEntryActivity.this,"请输入您的微信真实姓名！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //判断微信号是否为空
                if (TextUtils.isEmpty(weChatId)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WXEntryActivity.this,"请输入您的微信号！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //判断提现金额是否为空
                if (TextUtils.isEmpty(amount)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WXEntryActivity.this,"请输入提现金额！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //判断提现密码是否为空
                if (TextUtils.isEmpty(withdrawPassword)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WXEntryActivity.this,"请输入提现密码！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //判断金额是否超出余额
                MyApplication myApplication = (MyApplication) getApplicationContext();
                String balanceStr = myApplication.myBalance;

//                System.out.println("WXPayWithdrawActivity:==============balanceStr:" + balanceStr);

                if (Float.parseFloat(amount) > Float.parseFloat(balanceStr)){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WXEntryActivity.this,"您的提现金额超出了余额！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //加密传输参数
                String staffIdEn = null;
                String nameEn = null;
                String weChatIdEn = null;
                String amountEn = null;
                String withdrawPasswordEn = null;
                String openidEn = null;

                try {
                    staffIdEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(String.valueOf(staffId).getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    nameEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(name.getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    weChatIdEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(weChatId.getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    amountEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(String.valueOf((int) Float.parseFloat(amount) * 100).getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    withdrawPasswordEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(withdrawPassword.getBytes(), EncryptionUtil.APPKEY.getBytes()));

                    if (!TextUtils.isEmpty(openid)){
                        openidEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(openid.getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    }

                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                        | BadPaddingException | IllegalArgumentException e1) {
                    e1.printStackTrace();
                }

//                System.out.println("WXPayWithdrawActivity:==============name:" + name + " weChatId:" + weChatId + " amount:" + amount + " withdrawPassword:" + withdrawPassword);
//                System.out.println("WXPayWithdrawActivity:==============nameEn:" + nameEn + " weChatIdEn:" + weChatIdEn + " amountEn:" + amountEn + " withdrawPasswordEn:" + withdrawPasswordEn);

                //发送提现请求
                final String finalNameEn = nameEn;
                final String finalAmountEn = amountEn;
                final String finalWeChatIdEn = weChatIdEn;
                final String finalWithdrawPasswordEn = withdrawPasswordEn;
                final String finalStaffIdEn = staffIdEn;
                final String finalOpenidEn = openidEn;

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //提现到微信请求
                        String httpsUrl = getString(R.string.httpsUrl);
                        String url = httpsUrl + "WXWithdrawService";

                        String result = HttpRequest.sendGet(url,"staffId=" + finalStaffIdEn + "&name=" + finalNameEn + "&weChatId=" + finalWeChatIdEn + "&money=" + finalAmountEn + "&withdrawPassword=" + finalWithdrawPasswordEn + "&openId=" + finalOpenidEn);

                        //判断网络是否正常
                        if (result.equals("404")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(WXEntryActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                                }
                            });

                            return;
                        }

                        try {
                            JSONObject wXWithdrawService = new JSONObject(result);

                            int state = wXWithdrawService.optInt("state");
                            final String message = wXWithdrawService.optString("message");

                            if (state == 0){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(WXEntryActivity.this,message,Toast.LENGTH_LONG).show();

//                                        Intent intent = new Intent();
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        intent.setClass(WXEntryActivity.this,WalletActivity.class);
//                                        startActivity(intent);

                                    }
                                });

                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(WXEntryActivity.this,message,Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    //设置自定义ActionBar
    private void setCustomActionBar(){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        TextView tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        tv_actionBarTitle.setText("提现到微信");

        LinearLayout ll_back = mActionBarView.findViewById(R.id.ll_back);
        ll_back.setVisibility(View.VISIBLE);

        TextView tv_backName = mActionBarView.findViewById(R.id.tv_backName);
        tv_backName.setText("钱包");

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

    //微信登录
    private void wxLogin(){
//
//        api = WXAPIFactory.createWXAPI(this,APP_ID,true);
//        api.registerApp(APP_ID);

        //检查是否安装微信客户端
        if (!api.isWXAppInstalled()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WXEntryActivity.this,"您还未安装微信客户端！",Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        SendAuth.Req req = new SendAuth.Req();
//        req.scope = "snsapi_base";
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        Boolean sendReqBoolean = api.sendReq(req);
    }

    @Override
    public void onReq(BaseReq baseReq) {

//        System.out.println("WXEntryActivity:回调测试========================onReq");

    }

    @Override
    public void onResp(BaseResp resp) {
//        System.out.println("WXEntryActivity:回调测试========================onResp");

        int result = -1;
        SendAuth.Resp re = ((SendAuth.Resp) resp);
        code = re.code;

//        System.out.println("WXEntryActivity:onResp_code:======================" + code);

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = 0;

                getOpenID();

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = 1;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = 2;
                break;
            default:
                result = 3;
                break;
        }

//        System.out.println("result_WXEntryActivity:============" + result + " code:" + code);
    }

    //获取openid
    private void getOpenID() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // APP_ID和APP_Secret在微信开发平台添加应用的时候会生成，grant_type 用默认的"authorization_code"即可.

                String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
                String url2 = "https://api.weixin.qq.com/sns/oauth2/access_token?" + "appid=" + APP_ID + "&secret=" + APP_Secret + "&code=" + code + "&grant_type=authorization_code";

//                System.out.println("WXEntryActivity:==============url:"+url2 + "         APP_ID:" + APP_ID + " APP_Secret:" + APP_Secret + " code:" + code);

                String result = HttpRequest.sendPost(url,"appid=" + APP_ID + "&secret=" + APP_Secret + "&code=" + code + "&grant_type=authorization_code");

//                System.out.println("WXEntryActivity:result_http:=========" + result);

                try {
                    JSONObject object = new JSONObject(result);

                    openid = object.getString("openid");

//                    System.out.println("WXEntryActivity:result_http:=========openid:" + openid);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

}
