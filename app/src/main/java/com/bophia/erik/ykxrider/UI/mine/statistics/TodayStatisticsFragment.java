package com.bophia.erik.ykxrider.UI.mine.statistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * “今日战绩”Fragment
 */
public class TodayStatisticsFragment extends Fragment {

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;

    private int orderNum;
    private int disWarnNum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.achievement_today,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //获取配送员信息
        sp = getActivity().getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");
    }

    @Override
    public void onResume() {
        super.onResume();

        //今日战绩
        new Thread(new Runnable() {
            @Override
            public void run() {

                String todayOrderInfo = HttpRequest.sendGet(getString(R.string.httpsUrl) + "GetDateStatistics","distributorPhone=" + distributorPhone + "&dateType=" + "today");

                //判断网络是否正常
                if (todayOrderInfo.equals("404")){

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
                    JSONObject getDoneDisOrderByDisPhone = new JSONObject(todayOrderInfo);

                    JSONArray content = getDoneDisOrderByDisPhone.optJSONArray("content");

                    //获取订单数量
                    orderNum = content.length();

                    //获取警告次数
                    disWarnNum = 0;
                    for (int i=0; i<content.length(); i++){
                        JSONObject doneDisOrder = content.getJSONObject(i);

                        int disWarn = doneDisOrder.optInt("disWarn");

                        if (disWarn == 1){
                            disWarnNum ++;
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //显示订单数
                        TextView tv_todayOrderNum = getActivity().findViewById(R.id.tv_todayOrderNum);
                        tv_todayOrderNum.setText(orderNum + "");

                        //显示警告次数
                        TextView tv_todayWarnNum = getActivity().findViewById(R.id.tv_todayWarnNum);
                        tv_todayWarnNum.setText(disWarnNum + "");

                        TextView tv_todayRanking = getActivity().findViewById(R.id.tv_todayRanking);
                        tv_todayRanking.setText("今日排名：第 1 名");

                    }
                });

            }
        }).start();

    }
}
