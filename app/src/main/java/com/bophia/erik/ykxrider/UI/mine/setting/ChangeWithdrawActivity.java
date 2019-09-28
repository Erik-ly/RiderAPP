package com.bophia.erik.ykxrider.UI.mine.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.EncryptionUtil;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.YKXRiderActivity;
import com.bophia.erik.ykxrider.wxapi.WXEntryActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 修改提现密码
 */
public class ChangeWithdrawActivity extends YKXRiderActivity {

    private SharedPreferences sp;
    private int staffId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_withdraw_password_layout);

        //自定义ActionBar
        setCustomActionBar("修改提现密码",true,"设置");

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        staffId = sp.getInt("id",-1);

        //“确定修改”按钮
        Button bt_changeWithdrawPWD = findViewById(R.id.bt_changeWithdrawPWD);
        bt_changeWithdrawPWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取数据
                //获取原来的密码
                EditText et_oldWithdrawPWD = findViewById(R.id.et_oldWithdrawPWD);
                String oldWithdrawPWD = et_oldWithdrawPWD.getText().toString().trim();

                //获取新的密码
                EditText et_newWithdrawPWD = findViewById(R.id.et_newWithdrawPWD);
                String newWithdrawPWD = et_newWithdrawPWD.getText().toString().trim();

                //获取确认新的密码
                EditText et_reviewWithdrawPWD = findViewById(R.id.et_reviewWithdrawPWD);
                String reviewWithdrawPWD = et_reviewWithdrawPWD.getText().toString().trim();

                //判断两次输入的新密码是否相同
                if (!newWithdrawPWD.equals(reviewWithdrawPWD)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChangeWithdrawActivity.this,"两次输入的新密码不一致！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //加密传输字段
                String staffIdEn = null;
                String oldWithdrawPWDEn = null;
                String newWithdrawPWDEn = null;

                try {
                    staffIdEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(String.valueOf(staffId).getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    oldWithdrawPWDEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(oldWithdrawPWD.getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    newWithdrawPWDEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(newWithdrawPWD.getBytes(), EncryptionUtil.APPKEY.getBytes()));

                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                        | BadPaddingException | IllegalArgumentException e1) {
                    e1.printStackTrace();
                }

                final String finalStaffIdEn = staffIdEn;
                final String finalOldWithdrawPWDEn = oldWithdrawPWDEn;
                final String finalNewWithdrawPWDEn = newWithdrawPWDEn;

                //发送请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String httpsUrl = getString(R.string.httpsUrl);
                        String url = httpsUrl + "ChangeWithdrawPWD";

                        String result = HttpRequest.sendGet(url,"staffId=" + finalStaffIdEn + "&oldWithdrawPWD=" + finalOldWithdrawPWDEn + "&newWithdrawPWD=" + finalNewWithdrawPWDEn );

                        //判断网络是否正常
                        if (result.equals("404")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ChangeWithdrawActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                                }
                            });

                            return;
                        }

                        try {
                            JSONObject changeWithdrawPWD = new JSONObject(result);

                            int state = changeWithdrawPWD.optInt("state");
                            final String message = changeWithdrawPWD.optString("message");

                            if (state == 0){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChangeWithdrawActivity.this,message,Toast.LENGTH_LONG).show();

//                                        Intent intent = new Intent();
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        intent.setClass(ChangeWithdrawActivity.this,SettingActivity.class);
//                                        startActivity(intent);

                                    }
                                });

                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChangeWithdrawActivity.this,message,Toast.LENGTH_LONG).show();
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
}
