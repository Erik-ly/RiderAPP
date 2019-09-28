package com.bophia.erik.ykxrider.UI.mine.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.EncryptionUtil;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.YKXRiderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 修改登录密码
 */
public class ChangeSignInPWDActivity extends YKXRiderActivity {

    private SharedPreferences sp;
    private int staffId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_signin_password_layout);

        //自定义ActionBar
        setCustomActionBar("修改登录密码",true,"设置");

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        staffId = sp.getInt("id",-1);

        //“确定修改”按钮
        Button bt_changePWD = findViewById(R.id.bt_changePWD);
        bt_changePWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取数据
                //获取原来的密码
                EditText et_oldPWD = findViewById(R.id.et_oldPWD);
                String oldPWD = et_oldPWD.getText().toString().trim();

                //获取新的密码
                EditText et_newPWD = findViewById(R.id.et_newPWD);
                String newPWD = et_newPWD.getText().toString().trim();

                //获取确认新的密码
                EditText et_reviewPWD = findViewById(R.id.et_reviewPWD);
                String reviewPWD = et_reviewPWD.getText().toString().trim();

                //判断两次输入的新密码是否相同
                if (!newPWD.equals(reviewPWD)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChangeSignInPWDActivity.this,"两次输入的新密码不一致！",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                System.out.println("ChangeSignInPWDActivity:===============oldPWD:" + oldPWD + " newPWD:" + newPWD + " reviewPWD:" + reviewPWD);

                //加密传输字段
                String staffIdEn = null;
                String oldPWDEn = null;
                String newPWDEn = null;

                try {
                    staffIdEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(String.valueOf(staffId).getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    oldPWDEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(oldPWD.getBytes(), EncryptionUtil.APPKEY.getBytes()));
                    newPWDEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(newPWD.getBytes(), EncryptionUtil.APPKEY.getBytes()));

                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                        | BadPaddingException | IllegalArgumentException e1) {
                    e1.printStackTrace();
                }

                final String finalStaffIdEn = staffIdEn;
                final String finalOldPWDEn = oldPWDEn;
                final String finalNewPWDEn = newPWDEn;

                //发送请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String httpsUrl = getString(R.string.httpsUrl);
                        String url = httpsUrl + "ChangeSignInPWD";

                        String result = HttpRequest.sendGet(url,"staffId=" + finalStaffIdEn + "&oldPWD=" + finalOldPWDEn + "&newPWD=" + finalNewPWDEn );

                        //判断网络是否正常
                        if (result.equals("404")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ChangeSignInPWDActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                                }
                            });

                            return;
                        }

                        try {
                            JSONObject changePWD = new JSONObject(result);

                            int state = changePWD.optInt("state");
                            final String message = changePWD.optString("message");

                            if (state == 0){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChangeSignInPWDActivity.this,message,Toast.LENGTH_LONG).show();

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
                                        Toast.makeText(ChangeSignInPWDActivity.this,message,Toast.LENGTH_LONG).show();
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
