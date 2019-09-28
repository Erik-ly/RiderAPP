package com.bophia.erik.ykxrider.UI.mine.myEvaluation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.UI.distribution.PickupFragment;
import com.bophia.erik.ykxrider.UI.neworder.OrderInfoActivity;
import com.bophia.erik.ykxrider.entity.Evaluation;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.Anticlockwise;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.MyWebView;
import com.bophia.erik.ykxrider.utils.StringUtils;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * “用户评价”Fragment
 */
public class UserEvaluationFragment extends Fragment{

    //配送员信息
    private SharedPreferences sp;
    private int distributorId;

    //顶部三个按钮
    private TextView tv_allEvaluation;
    private TextView tv_satisfactionEvaluation;
    private TextView tv_dissatisfiedEvaluation;

    //请求链接
    private String httpsUrl;

    private List<Evaluation> list;
    private EvaluationAdapter evaluationAdapter;

    //加载动画
    private AVLoadingIndicatorView avi;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.myevaluation_user_layout,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //获取配送员信息
        sp = getActivity().getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);

        //获取链接
        httpsUrl = getString(R.string.httpsUrl);

        //定义list存放listView要展示的数据
        list = new ArrayList<>();
        list.clear();

        //默认显示全部评价
        tv_allEvaluation = getActivity().findViewById(R.id.tv_allEvaluation);
        tv_allEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_leftround_click));
        tv_allEvaluation.setTextColor(getResources().getColor(R.color.colorWhite));

        //加载动画
        avi = getActivity().findViewById(R.id.avi_loading);

        //获取全部用户评价
        getUserEvaluationRider(null);

        //按钮监听
        //全部
        tv_allEvaluation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //设置点击效果
                //全部
                tv_allEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_leftround_click));
                tv_allEvaluation.setTextColor(getResources().getColor(R.color.colorWhite));

                //满意
                tv_satisfactionEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame));
                tv_satisfactionEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //不满意
                tv_dissatisfiedEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_rightround));
                tv_dissatisfiedEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //获取全部用户评价
                getUserEvaluationRider(null);

            }
        });

        //满意
        tv_satisfactionEvaluation = getActivity().findViewById(R.id.tv_satisfactionEvaluation);
        tv_satisfactionEvaluation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //设置点击效果
                //全部
                tv_allEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_leftround));
                tv_allEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //满意
                tv_satisfactionEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_click));
                tv_satisfactionEvaluation.setTextColor(getResources().getColor(R.color.colorWhite));

                //不满意
                tv_dissatisfiedEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_rightround));
                tv_dissatisfiedEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //获取用户满意评价
                getUserEvaluationRider("0");

            }
        });

        //不满意
        tv_dissatisfiedEvaluation = getActivity().findViewById(R.id.tv_dissatisfiedEvaluation);
        tv_dissatisfiedEvaluation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //设置点击效果
                //全部
                tv_allEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_leftround));
                tv_allEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //满意
                tv_satisfactionEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame));
                tv_satisfactionEvaluation.setTextColor(getResources().getColor(R.color.colorTitle));

                //不满意
                tv_dissatisfiedEvaluation.setBackground(getResources().getDrawable(R.drawable.bg_frame_rightround_click));
                tv_dissatisfiedEvaluation.setTextColor(getResources().getColor(R.color.colorWhite));

                //获取用户不满意评价
                getUserEvaluationRider("1");

            }
        });


        //找到控件ListView
        final ListView lv_userEvaluation = getActivity().findViewById(R.id.lv_userEvaluation);

        evaluationAdapter = new EvaluationAdapter();
        lv_userEvaluation.setAdapter(evaluationAdapter);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //获取用户评价骑手信息
    private void getUserEvaluationRider(final String level){

        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {

                //显示加载动画
                avi.show();

                //获取所有与该销售点相关的待取货订单
                String result = HttpRequest.sendGet(httpsUrl + "GetEvaluationRiderByStaffId","staffId=" + distributorId + "&level=" + level);

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
                    JSONObject userEvaluationRider = new JSONObject(result);

                    JSONArray content = userEvaluationRider.optJSONArray("content");


                    for (int i=0; i<content.length(); i++){
                        JSONObject UERObject = content.getJSONObject(i);

                        Evaluation evaluation = new Evaluation();

                        JSONObject userInfo = UERObject.getJSONObject("user");
                        evaluation.setUserIcon(userInfo.optString("user_avatarUrl"));

                        evaluation.setOrderId(UERObject.optString("orderId"));
                        evaluation.setUserName(UERObject.optString("userName"));
                        evaluation.setEvaluationTime(UERObject.optString("evaluationTime"));
                        evaluation.setEvaluationLabel(UERObject.optString("evaluationLabel"));
                        evaluation.setEvaluationText(UERObject.optString("evaluationText"));

                        evaluation.setLevel(UERObject.optInt("level"));
                        evaluation.setState(UERObject.optInt("state"));
                        evaluation.setAnonymous(UERObject.optInt("anonymous"));

                        list.add(evaluation);

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

                            //隐藏加载动画
                            avi.hide();

                            evaluationAdapter.notifyDataSetChanged();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }).start();

    }

    //定义listview数据适配器
    private class EvaluationAdapter extends BaseAdapter {

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
                //创建新的对象
                v = View.inflate(getActivity().getApplicationContext(),R.layout.myevaluation_item,null);

            }else {
                v = view;
            }

            if(i>=list.size()){
                return v;
            }

            //显示数据
            final Evaluation evaluation = list.get(i);

            //显示用户头像
            MyWebView wv_userAvatar = v.findViewById(R.id.wv_userAvatar);

            String userIconStr = evaluation.getUserIcon();

            //如果头像不为空并且是非匿名评价
            if (StringUtils.isNullOrEmpty(userIconStr) && evaluation.getAnonymous() == 0){

                wv_userAvatar.loadUrl(evaluation.getUserIcon());

            //如果头像为空或者匿名显示默认头像
            }else {
                wv_userAvatar.loadUrl(getString(R.string.userAvatar));
            }

            wv_userAvatar.getSettings().setUseWideViewPort(true);
            wv_userAvatar.getSettings().setLoadWithOverviewMode(true);//设置图片缩放

            //显示用户名
            TextView tv_userName = v.findViewById(R.id.tv_userName);

            String userName = evaluation.getUserName();

            //解码
            String userNameDe = "";
            try {
                userNameDe = URLDecoder.decode(userName,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //判断是否为匿名评价
            if (evaluation.getAnonymous() == 1){
                tv_userName.setText("匿名用户");
            }else {
                tv_userName.setText(userNameDe);
            }

            //显示时间
            TextView tv_evaluationTime = v.findViewById(R.id.tv_evaluationTime);
            tv_evaluationTime.setText(evaluation.getEvaluationTime());

            //显示标签
            TextView tv_evaluationLabel = v.findViewById(R.id.tv_evaluationLabel);

            String evaluationLabelStr = evaluation.getEvaluationLabel();

            //判断评价标签是否为空
            if (evaluationLabelStr.equals("#")){
                if (evaluation.getLevel() == 0){
                    tv_evaluationLabel.setText("好评");
                }else if (evaluation.getLevel() == 1){
                    tv_evaluationLabel.setText("差评");
                }else {
                    tv_evaluationLabel.setText("（无评价标签）");
                }
            }else {
                String evaluationLabel = evaluationLabelStr.replace("#","  ");
                tv_evaluationLabel.setText(evaluationLabel);
            }

            //显示内容
            TextView tv_evaluationText = v.findViewById(R.id.tv_evaluationText);

            String evaluationText = evaluation.getEvaluationText();

            //评价文字内容为空
            if (!StringUtils.isNullOrEmpty(evaluationText)){
                tv_evaluationText.setVisibility(View.GONE);
            }else {

                tv_evaluationText.setVisibility(View.VISIBLE);

                //解码
                String evaluationTextDe = "";
                try {
                    evaluationTextDe = URLDecoder.decode(evaluationText,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                tv_evaluationText.setText(evaluationTextDe);

            }

            //查看订单详情
            TextView tv_viewOrderInfo = v.findViewById(R.id.tv_viewOrderInfo);
            if (evaluation.getAnonymous() == 1){
                tv_viewOrderInfo.setVisibility(View.GONE);
            }else if (evaluation.getAnonymous() == 0){
                tv_viewOrderInfo.setVisibility(View.VISIBLE);
            }

            tv_viewOrderInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //传递orderId
                    Bundle bundle = new Bundle();
                    bundle.putString("orderId",evaluation.getOrderId());

                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(getActivity(),OrderInfoActivity.class);
                    startActivity(intent);

                }
            });

            return v;
        }

        //设置ListView不可点击
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

}
