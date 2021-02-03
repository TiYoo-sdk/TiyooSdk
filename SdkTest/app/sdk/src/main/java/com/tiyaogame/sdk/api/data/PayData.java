package com.tiyaogame.sdk.api.data;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

public class PayData {
    public String   purchaseOrderId;    //支付订单id
    public String   gameOrderId;    //游戏订单id
    public String   productName;    //商品名称
    public String   productID;      //商品ID
    public String   productAttr;    //商品介绍
    public double   productPrice;   //商品价格
    public String   currencyCode;   //货币代码
    public Integer  productNumber;  //购买商品的数量 默认为 1
    public int      payType;        //支付方式:1.Google Play; 2.支付宝; 3.网银; 4.财付通; 5.移动通信; 6.联通通信; 7.电信通信; 8.PayPal.
    public String   stay_field1 = "";
    public String   stay_field2 = "";

    //新增的字段
    public String   userName    = "";
    public String   userArea    = "";
    public String   userId      = "";
    public int      userLevel   = -1;

    public String   skuType =  BillingClient.SkuType.INAPP;
    public SkuDetails skuDetails;
    public Purchase purchase;
}
