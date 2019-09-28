package com.bophia.erik.ykxrider.UI.mine.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.UI.signIn.SignInActivity;
import com.bophia.erik.ykxrider.utils.YKXRiderActivity;

/**
 * “设置”界面Activity
 */
public class SettingActivity extends YKXRiderActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //加载布局
        setContentView(R.layout.setting_layout);

        //自定义ActionBar
        setCustomActionBar("设置",true,"我的");

        /**
         * 找到控件并设置监听
         */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //登录密码
                RelativeLayout rl_settingPassword = findViewById(R.id.rl_settingPassword);
                rl_settingPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        Toast.makeText(SettingActivity.this,"修改“登录密码”功能即将上线！",Toast.LENGTH_LONG).show();

                        Intent intent = new Intent();
                        intent.setClass(SettingActivity.this,ChangeSignInPWDActivity.class);
                        startActivity(intent);
                    }
                });

                //提现密码
                RelativeLayout rl_withdrawPassword = findViewById(R.id.rl_withdrawPassword);
                rl_withdrawPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent();
                        intent.setClass(SettingActivity.this,ChangeWithdrawActivity.class);
                        startActivity(intent);

                    }
                });

                //清理聊天缓存
                RelativeLayout rl_cleanChat = findViewById(R.id.rl_cleanChat);
                rl_cleanChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(SettingActivity.this,"“清理聊天缓存”即将上线！",Toast.LENGTH_LONG).show();

                    }
                });

                RelativeLayout rl_remindSetting = findViewById(R.id.rl_remindSetting);
                rl_remindSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(SettingActivity.this,"“提醒设置”即将上线！",Toast.LENGTH_LONG).show();

                    }
                });

                //关于我们
                RelativeLayout rl_aboutUs = findViewById(R.id.rl_aboutUs);
                rl_aboutUs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LayoutInflater li = LayoutInflater.from(SettingActivity.this);
                        View view_about = li.inflate(R.layout.about,null);
                        final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
                        dialog.setTitle("关于");
                        dialog.setView(view_about);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        dialog.show();

                        //客服电话
                        LinearLayout ll_disHelpPhone = view_about.findViewById(R.id.ll_disHelpPhone);
                        final TextView tv_disHelpPhoen = view_about.findViewById(R.id.tv_disHelpPhone);
                        ll_disHelpPhone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String disHelpPhone = tv_disHelpPhoen.getText().toString().trim();
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+disHelpPhone));//跳转到拨号界面，同时传递电话号码
                                startActivity(intent);
                            }
                        });

                    }
                });

                //用户反馈
                RelativeLayout rl_feedback = findViewById(R.id.rl_feedback);
                rl_feedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(SettingActivity.this,"“用户反馈”即将上线！",Toast.LENGTH_LONG).show();

                    }
                });

                //联系客服
                RelativeLayout rl_contactCustomerService = findViewById(R.id.rl_contactCustomerService);
                rl_contactCustomerService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+"16603858967"));//跳转到拨号界面，同时传递电话号码
                        startActivity(intent);

                    }
                });

                //退出登录
                TextView tt_signOut = findViewById(R.id.tt_signOut);
                tt_signOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
                        dialog.setTitle("退出登录");
                        dialog.setMessage("确定退出登录？");
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new SignOut().onClick(null);

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

            }
        });

    }

    //退出登录
    private class SignOut implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            //清空SharePreferences
            SharedPreferences sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();

            //跳转到登录界面
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(SettingActivity.this,SignInActivity.class);
            startActivity(intent);
        }
    }

}
