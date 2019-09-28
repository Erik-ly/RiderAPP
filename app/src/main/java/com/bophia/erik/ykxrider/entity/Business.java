package com.bophia.erik.ykxrider.entity;

/**
 *  商户信息Business相关的JavaBean
 */
public class Business {

    //商户基本信息
    private String businessId;
    private String businessName;
    private String businessAddress;
    private String businessPhone;

    //商户商品信息
    private int businessGoodsId;
    private String goodsId;
    private String price;
    private String actualPayment;

    //商品订单详情相关
    private String goodsName;
    private int goodsNum;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public int getBusinessGoodsId() {
        return businessGoodsId;
    }

    public void setBusinessGoodsId(int businessGoodsId) {
        this.businessGoodsId = businessGoodsId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getActualPayment() {
        return actualPayment;
    }

    public void setActualPayment(String actualPayment) {
        this.actualPayment = actualPayment;
    }

    public int getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(int goodsNum) {
        this.goodsNum = goodsNum;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }
}
