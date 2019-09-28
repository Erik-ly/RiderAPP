package com.bophia.erik.ykxrider.UI.mine.statistics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.MyXFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * “统计图表”activity
 */
public class StatisticChartsActivity extends Activity {

    private BarChart BarChart_useTime;
    private BarChart bc_orderNum;
    private BarChart bc_disMoney;

    private SharedPreferences sp;
    private int distributorId;
    private String distributorName;
    private String distributorPhone;

    private double receiptUseTimeAve;
    private double outBoundUseTimeAve;
    private double distributionUseTimeAve;
    private double disTimeAve;

    //每月接单量
    private int JanOrderNum = 0;
    private int FebOrderNum = 0;
    private int MarOrderNum = 0;
    private int AprOrderNum = 0;
    private int MayOrderNum = 0;
    private int JunOrderNum = 0;
    private int JulOrderNum = 0;
    private int AugOrderNum = 0;
    private int SepOrderNum = 0;
    private int OctOrderNum = 0;
    private int NovOrderNum = 0;
    private int DecOrderNum = 0;

    //每月警告次数
    private int JanWarnNum = 0;
    private int FebWarnNum = 0;
    private int MarWarnNum = 0;
    private int AprWarnNum = 0;
    private int MayWarnNum = 0;
    private int JunWarnNum = 0;
    private int JulWarnNum = 0;
    private int AugWarnNum = 0;
    private int SepWarnNum = 0;
    private int OctWarnNum = 0;
    private int NovWarnNum = 0;
    private int DecWarnNum = 0;

    //每月配送费
    private float JanDisMoney;
    private float FebDisMoney;
    private float MarDisMoney;
    private float AprDisMoney;
    private float MayDisMoney;
    private float JunDisMoney;
    private float JulDisMoney;
    private float AugDisMoney;
    private float SepDisMoney;
    private float OctDisMoney;
    private float NovDisMoney;
    private float DecDisMoney;

    //横轴坐标
    protected  String[] xValues1 = new String[]{"总共用时","接单时间","出库时间","配送时间"};
    protected  String[] xValues2 = new String[]{"12月","1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statisticcharts);

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        distributorId = sp.getInt("id",-1);
        distributorName = sp.getString("name","NULL");
        distributorPhone = sp.getString("phone","NULL");

        new Thread(new Runnable() {
            @Override
            public void run() {

                //配送统计
                String httpsUrl = getString(R.string.httpsUrl);
                String url = httpsUrl + "DistributionStatistics";
                String result = HttpRequest.sendGet(url,"distributorPhone=" + distributorPhone);

                //判断网络是否正常
                if (result.equals("404")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StatisticChartsActivity.this,"网络连接异常，请检查手机网络",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                try {
                    JSONObject statisticsInfo = new JSONObject(result);

                    int state = statisticsInfo.optInt("state");

                    if (state == 0){

                        //配送各阶段平均用时
                        receiptUseTimeAve = Double.parseDouble(statisticsInfo.optString("receiptUseTimeAve"));
                        outBoundUseTimeAve = Double.parseDouble(statisticsInfo.optString("outBoundUseTimeAve"));
                        distributionUseTimeAve = Double.parseDouble(statisticsInfo.optString("distributionUseTimeAve"));
                        disTimeAve = Double.parseDouble(statisticsInfo.optString("disTimeAve"));

                        //每月接单量
                        JanOrderNum = Integer.parseInt(statisticsInfo.optString("JanOrderNum"));
                        FebOrderNum = Integer.parseInt(statisticsInfo.optString("FebOrderNum"));
                        MarOrderNum = Integer.parseInt(statisticsInfo.optString("MarOrderNum"));
                        AprOrderNum = Integer.parseInt(statisticsInfo.optString("AprOrderNum"));
                        MayOrderNum = Integer.parseInt(statisticsInfo.optString("MayOrderNum"));
                        JunOrderNum = Integer.parseInt(statisticsInfo.optString("JunOrderNum"));
                        JulOrderNum = Integer.parseInt(statisticsInfo.optString("JulOrderNum"));
                        AugOrderNum = Integer.parseInt(statisticsInfo.optString("AugOrderNum"));
                        SepOrderNum = Integer.parseInt(statisticsInfo.optString("SepOrderNum"));
                        OctOrderNum = Integer.parseInt(statisticsInfo.optString("OctOrderNum"));
                        NovOrderNum = Integer.parseInt(statisticsInfo.optString("NovOrderNum"));
                        DecOrderNum = Integer.parseInt(statisticsInfo.optString("DecOrderNum"));

                        //每月警告次数
                        JanWarnNum = Integer.parseInt(statisticsInfo.optString("JanWarnNum"));
                        FebWarnNum = Integer.parseInt(statisticsInfo.optString("FebWarnNum"));
                        MarWarnNum = Integer.parseInt(statisticsInfo.optString("MarWarnNum"));
                        AprWarnNum = Integer.parseInt(statisticsInfo.optString("AprWarnNum"));
                        MayWarnNum = Integer.parseInt(statisticsInfo.optString("MayWarnNum"));
                        JunWarnNum = Integer.parseInt(statisticsInfo.optString("JunWarnNum"));
                        JulWarnNum = Integer.parseInt(statisticsInfo.optString("JulWarnNum"));
                        AugWarnNum = Integer.parseInt(statisticsInfo.optString("AugWarnNum"));
                        SepWarnNum = Integer.parseInt(statisticsInfo.optString("SepWarnNum"));
                        OctWarnNum = Integer.parseInt(statisticsInfo.optString("OctWarnNum"));
                        NovWarnNum = Integer.parseInt(statisticsInfo.optString("NovWarnNum"));
                        DecWarnNum = Integer.parseInt(statisticsInfo.optString("DecWarnNum"));

                        //每月配送费
                        JanDisMoney = Float.parseFloat(statisticsInfo.optString("JanDisMoney"));
                        FebDisMoney = Float.parseFloat(statisticsInfo.optString("FebDisMoney"));
                        MarDisMoney = Float.parseFloat(statisticsInfo.optString("MarDisMoney"));
                        AprDisMoney = Float.parseFloat(statisticsInfo.optString("AprDisMoney"));
                        MayDisMoney = Float.parseFloat(statisticsInfo.optString("MayDisMoney"));
                        JunDisMoney = Float.parseFloat(statisticsInfo.optString("JunDisMoney"));
                        JulDisMoney = Float.parseFloat(statisticsInfo.optString("JulDisMoney"));
                        AugDisMoney = Float.parseFloat(statisticsInfo.optString("AugDisMoney"));
                        SepDisMoney = Float.parseFloat(statisticsInfo.optString("SepDisMoney"));
                        OctDisMoney = Float.parseFloat(statisticsInfo.optString("OctDisMoney"));
                        NovDisMoney = Float.parseFloat(statisticsInfo.optString("NovDisMoney"));
                        DecDisMoney = Float.parseFloat(statisticsInfo.optString("DecDisMoney"));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置图表数据
                        initViewUseTime();
                        initViewOrderNum();
                        initViewDisMoney();
                    }
                });

            }
        }).start();

        //选择时间按钮
        Button bt_selectTime = findViewById(R.id.bt_selectTime);
        bt_selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DatePickerFragment dataPicker = new DatePickerFragment();
//                dataPicker.getShowsDialog();

//                //显示日历表
//                new DatePickerDialog(StatisticChartsActivity.this, 0, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
//
//                    }
//                }
//                ,2018
//                ,7
//                ,8).show();
            }
        });

    }

    //初始化配送时间图表
    private void initViewUseTime(){

        BarChart_useTime = findViewById(R.id.BarChart_useTime);
        BarChart_useTime.getDescription().setEnabled(false);
        BarChart_useTime.setMaxVisibleValueCount(20);//设置图表上最大可见绘制值标签的数量
        BarChart_useTime.setPinchZoom(false);//true缩放，false单独缩放x轴和y轴
        BarChart_useTime.setDrawBarShadow(false);//如果设置为true，则在每个条形后面绘制一个灰色区域，指示最大值。启用他的功能会使性能降低约40％。
        BarChart_useTime.setDrawGridBackground(false);//如果启用，将绘制图表绘图区域后面的背景矩形。
        BarChart_useTime.setDrawValueAboveBar(true);//如果设置为true，则所有值都会在其条形图上方绘制，而不是在其顶部之下。

        XAxis xAxis = BarChart_useTime.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x标签的位置
        xAxis.setDrawGridLines(false);//网格线

        //设置x轴文字
        MyXFormatter formatter = new MyXFormatter(xValues1);
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(formatter);

        //设置y轴
//        YAxis leftAxis = BarChart_useTime.getAxisLeft();
//        leftAxis.setStartAtZero(true);//y轴从0开始
        BarChart_useTime.getAxisLeft().setDrawGridLines(false);
        BarChart_useTime.animateY(1500);//y轴动画时长
        BarChart_useTime.getLegend().setEnabled(false);

        setDataUseTime();

    }

    //设置配送时间图数据
    private void setDataUseTime() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(1,(float)receiptUseTimeAve));
        yVals1.add(new BarEntry(2,(float)outBoundUseTimeAve));
        yVals1.add(new BarEntry(3,(float)distributionUseTimeAve));
        yVals1.add(new BarEntry(4,(float)disTimeAve));

        BarDataSet set1;
        if (BarChart_useTime.getData() != null &&
                BarChart_useTime.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) BarChart_useTime.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            BarChart_useTime.getData().notifyDataChanged();
            BarChart_useTime.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "日期设置");
            //设置多彩 也可以单一颜色
            set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
//            set1.setColor(Color.parseColor("#87CEEB"));
            set1.setDrawValues(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            BarChart_useTime.setData(data);
            BarChart_useTime.setFitBars(true);
        }

        //显示数值
        for (IDataSet set : BarChart_useTime.getData().getDataSets()){
            set.setDrawValues(!set.isDrawValuesEnabled());
        }
        BarChart_useTime.invalidate();
    }

    //初始化每月接单量图表
    private void initViewOrderNum(){

        bc_orderNum = findViewById(R.id.bc_orderNum);
        bc_orderNum.getDescription().setEnabled(false);
        bc_orderNum.setMaxVisibleValueCount(20);
        bc_orderNum.setPinchZoom(false);
        bc_orderNum.setDrawBarShadow(false);
        bc_orderNum.setDrawGridBackground(false);
        bc_orderNum.setDrawValueAboveBar(true);

        XAxis xAxis = bc_orderNum.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        //设置x轴文字
        MyXFormatter formatter2 = new MyXFormatter(xValues2);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(formatter2);

        //设置y轴
        bc_orderNum.getAxisLeft().setDrawGridLines(false);
//        bc_orderNum.getAxisLeft().setStartAtZero(true);//y轴从0开始
        bc_orderNum.animateY(1500);
        bc_orderNum.getLegend().setEnabled(false);

        setDataOrderNum();

    }

    //设置每月接单量图数据
    private void setDataOrderNum() {
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

        yVals2.add(new BarEntry(1,JanOrderNum));
        yVals2.add(new BarEntry(2,FebOrderNum));
        yVals2.add(new BarEntry(3,MarOrderNum));
        yVals2.add(new BarEntry(4,AprOrderNum));
        yVals2.add(new BarEntry(5,MayOrderNum));
        yVals2.add(new BarEntry(6,JunOrderNum));
        yVals2.add(new BarEntry(7,JulOrderNum));
        yVals2.add(new BarEntry(8,AugOrderNum));
        yVals2.add(new BarEntry(9,SepOrderNum));
        yVals2.add(new BarEntry(10,OctOrderNum));
        yVals2.add(new BarEntry(11,NovOrderNum));
        yVals2.add(new BarEntry(12,DecOrderNum));

        BarDataSet set2;
        if (bc_orderNum.getData() != null &&
                bc_orderNum.getData().getDataSetCount() > 0) {
            set2 = (BarDataSet) bc_orderNum.getData().getDataSetByIndex(0);
            set2.setValues(yVals2);
            bc_orderNum.getData().notifyDataChanged();
            bc_orderNum.notifyDataSetChanged();
        } else {
            set2 = new BarDataSet(yVals2, "日期设置");
            //设置多彩 也可以单一颜色
            set2.setColor(Color.parseColor("#87CEEB"));
            set2.setDrawValues(false);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set2);

            BarData data = new BarData(dataSets);
            bc_orderNum.setData(data);
            bc_orderNum.setFitBars(true);
        }

        //显示数值
        for (IDataSet set : bc_orderNum.getData().getDataSets()){
            set.setDrawValues(!set.isDrawValuesEnabled());
        }
        bc_orderNum.invalidate();
    }

    //初始化每月配送费图表
    private void initViewDisMoney(){

        bc_disMoney = findViewById(R.id.bc_disMoney);
        bc_disMoney.getDescription().setEnabled(false);
        bc_disMoney.setMaxVisibleValueCount(20);
        bc_disMoney.setPinchZoom(false);
        bc_disMoney.setDrawBarShadow(false);
        bc_disMoney.setDrawGridBackground(false);
        bc_disMoney.setDrawValueAboveBar(true);


        XAxis xAxis = bc_disMoney.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        //设置x轴文字
        MyXFormatter formatter2 = new MyXFormatter(xValues2);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(formatter2);

        //设置y轴
        bc_disMoney.getAxisLeft().setDrawGridLines(false);
        bc_disMoney.animateY(1500);
        bc_disMoney.getLegend().setEnabled(false);

        setDataDisMoney();

    }

    //设置每月接单量图数据
    private void setDataDisMoney() {
        ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();

        yVals3.add(new BarEntry(1,JanDisMoney));
        yVals3.add(new BarEntry(2,FebDisMoney));
        yVals3.add(new BarEntry(3,MarDisMoney));
        yVals3.add(new BarEntry(4,AprDisMoney));
        yVals3.add(new BarEntry(5,MayDisMoney));
        yVals3.add(new BarEntry(6,JunDisMoney));
        yVals3.add(new BarEntry(7,JulDisMoney));
        yVals3.add(new BarEntry(8,AugDisMoney));
        yVals3.add(new BarEntry(9,SepDisMoney));
        yVals3.add(new BarEntry(10,OctDisMoney));
        yVals3.add(new BarEntry(11,NovDisMoney));
        yVals3.add(new BarEntry(12,DecDisMoney));

        BarDataSet set3;
        if (bc_disMoney.getData() != null &&
                bc_disMoney.getData().getDataSetCount() > 0) {
            set3 = (BarDataSet) bc_disMoney.getData().getDataSetByIndex(0);
            set3.setValues(yVals3);
            bc_disMoney.getData().notifyDataChanged();
            bc_disMoney.notifyDataSetChanged();
        } else {
            set3 = new BarDataSet(yVals3, "日期设置");
            //设置多彩 也可以单一颜色
            set3.setColor(Color.parseColor("#FFA500"));
            set3.setDrawValues(false);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set3);

            BarData data = new BarData(dataSets);
            bc_disMoney.setData(data);
            bc_disMoney.setFitBars(true);
        }

        //显示数值
        for (IDataSet set : bc_disMoney.getData().getDataSets()){
            set.setDrawValues(!set.isDrawValuesEnabled());
        }
        bc_disMoney.invalidate();
    }

}
