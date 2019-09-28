package com.bophia.erik.ykxrider.entity;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

/**
 * 订单Order的JavaBean
 */
public class Order{

    //Orders
    private int id;
    private String orderId;
    private int relationSalepoint;
    private int orderType;
    private int payMothed;
    private int whether;
    private String time;
    private String paymentTime;
    private String booking;
    private String userId;
    private int payMethod;
    private String remark;
    private Float total;
    private int distributionFee;
    private String distributionPhone;
    private String userName;
    private String userPhone;
    private int userSex;
    private String userSite;
    private String userSiteDetails;
    private String distributionStartTime;
    private String distributionEndTime;
    private String receiptTime;

    //salepoint
    private String salePointName;
    private String salePointAddress;
    private String salePointLongitude;
    private String salePointLatitude;
    private String salePointAddressLongitude;
    private String salePointAddressLatitude;

    //orderInfo
    private String goodsId;
    private String goodsName;
    private int goodsNum;
    private Float goodsPrice;
    private int goodsOperate;
    private Float goodsBuying;


    //goods
    private String icon;
    private int ifDispose;
    private Float operateFee;
    private String residueWeight;
    private String deviation;

    //printingOrderInfo
    private int printingStartPage;
    private int printingEndPage;
    private int printingShareNum;
    private String printingColor;
    private String printingPattern;
    private String printingDirection;
    private String printingSize;
    private String printingRemark;
    private String filename;

    //distributor
    private String distributorId;
    private String distributorName;
    private String distributorSex;
    private String distributorPhone;
    private String distributorPsd;

    public String getReceiptTime() {
        return receiptTime;
    }

    public void setReceiptTime(String receiptTime) {
        this.receiptTime = receiptTime;
    }

    public String getOutBoundTime() {
        return outBoundTime;
    }

    public void setOutBoundTime(String outBoundTime) {
        this.outBoundTime = outBoundTime;
    }

    private String outBoundTime;

    public int getDisWarn() {
        return disWarn;
    }

    public void setDisWarn(int disWarn) {
        this.disWarn = disWarn;
    }

    private int disWarn;

    public String getUserSiteDetails() {
        return userSiteDetails;
    }

    public void setUserSiteDetails(String userSiteDetails) {
        this.userSiteDetails = userSiteDetails;
    }

    public int getRelationSalepoint() {
        return relationSalepoint;
    }

    public void setRelationSalepoint(int relationSalepoint) {
        this.relationSalepoint = relationSalepoint;
    }

    public int getPayMothed() {
        return payMothed;
    }

    public void setPayMothed(int payMothed) {
        this.payMothed = payMothed;
    }

    public BigDecimal getDisMoney() {
        return disMoney;
    }

    public void setDisMoney(BigDecimal disMoney) {
        this.disMoney = disMoney;
    }

    private BigDecimal disMoney;

    public String getDisTime() {
        return disTime;
    }

    public void setDisTime(String disTime) {
        this.disTime = disTime;
    }

    private String disTime;

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public String getDistributionStartTime() {
        return distributionStartTime;
    }

    public void setDistributionStartTime(String distributionStartTime) {
        this.distributionStartTime = distributionStartTime;
    }

    public String getDistributionEndTime() {
        return distributionEndTime;
    }

    public void setDistributionEndTime(String distributionEndTime) {
        this.distributionEndTime = distributionEndTime;
    }

    public int getPrintingStartPage() {
        return printingStartPage;
    }

    public void setPrintingStartPage(int printingStartPage) {
        this.printingStartPage = printingStartPage;
    }

    public int getPrintingEndPage() {
        return printingEndPage;
    }

    public void setPrintingEndPage(int printingEndPage) {
        this.printingEndPage = printingEndPage;
    }

    public int getPrintingShareNum() {
        return printingShareNum;
    }

    public void setPrintingShareNum(int printingShareNum) {
        this.printingShareNum = printingShareNum;
    }

    public String getPrintingColor() {
        return printingColor;
    }

    public void setPrintingColor(String printingColor) {
        this.printingColor = printingColor;
    }

    public String getPrintingPattern() {
        return printingPattern;
    }

    public void setPrintingPattern(String printingPattern) {
        this.printingPattern = printingPattern;
    }

    public String getPrintingDirection() {
        return printingDirection;
    }

    public void setPrintingDirection(String printingDirection) {
        this.printingDirection = printingDirection;
    }

    public String getPrintingSize() {
        return printingSize;
    }

    public void setPrintingSize(String printingSize) {
        this.printingSize = printingSize;
    }

    public String getPrintingRemark() {
        return printingRemark;
    }

    public void setPrintingRemark(String printingRemark) {
        this.printingRemark = printingRemark;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getDistributionNum() {
        return distributionNum;
    }

    public void setDistributionNum(int distributionNum) {
        this.distributionNum = distributionNum;
    }

    public BigDecimal getDistributionMoney() {
        return distributionMoney;
    }

    public void setDistributionMoney(BigDecimal distributionMoney) {
        this.distributionMoney = distributionMoney;
    }

    private int distributionNum;
    private BigDecimal distributionMoney;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getWhether() {
        return whether;
    }

    public void setWhether(int whether) {
        this.whether = whether;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(int payMethod) {
        this.payMethod = payMethod;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public int getDistributionFee() {
        return distributionFee;
    }

    public void setDistributionFee(int distributionFee) {
        this.distributionFee = distributionFee;
    }

    public String getDistributionPhone() {
        return distributionPhone;
    }

    public void setDistributionPhone(String distributionPhone) {
        this.distributionPhone = distributionPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public int getUserSex() {
        return userSex;
    }

    public void setUserSex(int userSex) {
        this.userSex = userSex;
    }

    public String getUserSite() {
        return userSite;
    }

    public void setUserSite(String userSite) {
        this.userSite = userSite;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public int getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(int goodsNum) {
        this.goodsNum = goodsNum;
    }

    public Float getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(Float goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Float getGoodsBuying() {
        return goodsBuying;
    }

    public void setGoodsBuying(Float goodsBuying) {
        this.goodsBuying = goodsBuying;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public String getDistributorSex() {
        return distributorSex;
    }

    public void setDistributorSex(String distributorSex) {
        this.distributorSex = distributorSex;
    }

    public String getDistributorPhone() {
        return distributorPhone;
    }

    public void setDistributorPhone(String distributorPhone) {
        this.distributorPhone = distributorPhone;
    }

    public String getDistributorPsd() {
        return distributorPsd;
    }

    public void setDistributorPsd(String distributorPsd) {
        this.distributorPsd = distributorPsd;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getBooking() {
        return booking;
    }

    public void setBooking(String booking) {
        this.booking = booking;
    }

    public String getSalePointName() {
        return salePointName;
    }

    public void setSalePointName(String salePointName) {
        this.salePointName = salePointName;
    }

    public String getSalePointAddress() {
        return salePointAddress;
    }

    public void setSalePointAddress(String salePointAddress) {
        this.salePointAddress = salePointAddress;
    }

    public String getSalePointLongitude() {
        return salePointLongitude;
    }

    public void setSalePointLongitude(String salePointLongitude) {
        this.salePointLongitude = salePointLongitude;
    }

    public String getSalePointLatitude() {
        return salePointLatitude;
    }

    public void setSalePointLatitude(String salePointLatitude) {
        this.salePointLatitude = salePointLatitude;
    }

    public String getSalePointAddressLongitude() {
        return salePointAddressLongitude;
    }

    public void setSalePointAddressLongitude(String salePointAddressLongitude) {
        this.salePointAddressLongitude = salePointAddressLongitude;
    }

    public String getSalePointAddressLatitude() {
        return salePointAddressLatitude;
    }

    public void setSalePointAddressLatitude(String salePointAddressLatitude) {
        this.salePointAddressLatitude = salePointAddressLatitude;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getGoodsOperate() {
        return goodsOperate;
    }

    public void setGoodsOperate(int goodsOperate) {
        this.goodsOperate = goodsOperate;
    }


    public int getIfDispose() {
        return ifDispose;
    }

    public void setIfDispose(int ifDispose) {
        this.ifDispose = ifDispose;
    }

    public Float getOperateFee() {
        return operateFee;
    }

    public void setOperateFee(Float operateFee) {
        this.operateFee = operateFee;
    }

    public String getResidueWeight() {
        return residueWeight;
    }

    public void setResidueWeight(String residueWeight) {
        this.residueWeight = residueWeight;
    }

    public String getDeviation() {
        return deviation;
    }

    public void setDeviation(String deviation) {
        this.deviation = deviation;
    }

//    @Override
//    public int compareTo(@NonNull Order order) {
//
////        int i = this.distributionEndTime.compareTo(order.getDistributionEndTime());//正序
//        int i = order.getDistributionEndTime().compareTo(this.distributionEndTime);//倒序
//
//        return i;
//    }
}
