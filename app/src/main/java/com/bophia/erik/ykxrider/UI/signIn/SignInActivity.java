package com.bophia.erik.ykxrider.UI.signIn;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.MainActivity;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.CodeUtils;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.YKXRiderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * “登录”界面Activity
 */
public class SignInActivity extends YKXRiderActivity {

    private EditText et_signInPhone;
    private EditText et_signInPsd;
    private String distributorPhone;
    private String distributorPsd;
    private String sivcode;
    private List<Order> list = new ArrayList<>();
    private String distributorId;
    private String distributorName;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private EditText et_sivCode;
    private ImageView im_sivCode;
    private String code;

    private JSONObject distributorInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //自定义ActionBar
        setCustomActionBar("骑手登录",false,null);

        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        editor = sp.edit();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sp.getBoolean("main",false)){

                    Intent intent = new Intent();
                    intent.setClass(SignInActivity.this,MainActivity.class);
                    startActivity(intent);
                    SignInActivity.this.finish();
                }
            }
        });

        setContentView(R.layout.signin_main);
        et_signInPhone = findViewById(R.id.et_signInPhone);
        et_signInPsd = findViewById(R.id.et_singInPsd);
        et_sivCode = findViewById(R.id.et_sivCode);

        im_sivCode = findViewById(R.id.im_sivCode);
        im_sivCode.setImageBitmap(CodeUtils.getInstance().createBitmap());
        code = CodeUtils.getCode();
        im_sivCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                im_sivCode.setImageBitmap(CodeUtils.getInstance().createBitmap());
                code = CodeUtils.getCode();
            }
        });

        //监听“登陆”按钮
        Button bt_signIn = findViewById(R.id.bt_signIn);
        bt_signIn.setOnClickListener(new SignIn());

        //监听“忘记密码？”
        TextView tv_forgetPsd = findViewById(R.id.tv_forgetPsd);
        tv_forgetPsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String customerServiceNumber = "16603858967";
                final AlertDialog dialog = new AlertDialog.Builder(SignInActivity.this).create();
                dialog.setTitle("忘记密码？");
                dialog.setMessage("请拨打客服电话：" + customerServiceNumber);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "拨打", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+customerServiceNumber));//跳转到拨号界面，同时传递电话号码
                        startActivity(intent);

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


    //点击“登陆”按钮事件
    private class SignIn implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            //获取输入信息
            distributorPhone = et_signInPhone.getText().toString().trim();
            distributorPsd = et_signInPsd.getText().toString().trim();
            sivcode = et_sivCode.getText().toString().trim();

            //判断输入信息是否为空
            if (TextUtils.isEmpty(distributorPhone) || TextUtils.isEmpty(distributorPsd)) {
                Toast.makeText(SignInActivity.this, "请输入手机号和密码", Toast.LENGTH_LONG).show();
            } else if (!code.equalsIgnoreCase(sivcode)) {
                Toast.makeText(SignInActivity.this, "请输入正确的验证码", Toast.LENGTH_LONG).show();
            } else if (code.equalsIgnoreCase(sivcode)) {

                //将输入手机号和密码与数据库中数据对比
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //请求登录
                        String httpsUrl = getString(R.string.httpsUrl);
                        String result = HttpRequest.sendGet(httpsUrl + "DistributorSignIn","staffPhone=" + distributorPhone + "&staffPassword=" + distributorPsd);

                        //判断网络是否正常
                        if (result.equals("404")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignInActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                                }
                            });

                            return;
                        }

                        try {
                            distributorInfo = new JSONObject(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //获取返回状态
                        int state = distributorInfo.optInt("state");

                        //账号密码正确
                        if (state == 0){

                            //获取配送员相关信息
                            int staffId = distributorInfo.optInt("staffId");
                            String staffName = distributorInfo.optString("staffName");
                            int staffSex = distributorInfo.optInt("staffSex");
                            String staffPhone = distributorInfo.optString("staffPhone");
                            int distributorType = distributorInfo.optInt("distributorType");
                            int relationSalepoint = distributorInfo.optInt("relationSalepoint");

                            String salePointName = distributorInfo.optString("salePointName");
                            String detailedAddress = distributorInfo.optString("detailedAddress");
                            String salePointLongitude = distributorInfo.optString("salePointLongitude");
                            String salePointLatitude = distributorInfo.optString("salePointLatitude");

                            //将配送员数据存放在SharedPreferences中
                            editor.putBoolean("main", true);
                            editor.putInt("id",staffId);
                            editor.putString("name", staffName);
                            editor.putString("phone", staffPhone);
                            editor.putInt("staffSex",staffSex);
                            editor.putInt("distributorType",distributorType);
                            editor.putInt("relationSalepoint",relationSalepoint);
                            editor.putString("salePointName",salePointName);
                            editor.putString("detailedAddress",detailedAddress);
                            editor.putString("salePointLongitude",salePointLongitude);
                            editor.putString("salePointLatitude",salePointLatitude);
                            editor.commit();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //显示“登录成功”
                                    String message = distributorInfo.optString("message");
                                    Toast.makeText(SignInActivity.this,message,Toast.LENGTH_LONG).show();

                                    //跳转到“未配送”页面
                                    Intent intent = new Intent();
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setClass(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);

                                }
                            });

                        //其他情况
                        }else if (state == -4){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //显示返回信息
                                    String message = distributorInfo.optString("message");
                                    Toast.makeText(SignInActivity.this,message,Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

}
