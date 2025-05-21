package com.union_test.toutiao.adgain;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.adgain.sdk.AdGainSdk;
import com.adgain.sdk.api.AdGainSdkConfig;
import com.adgain.sdk.api.CustomController;
import com.adgain.sdk.api.InitCallback;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.MediationCustomInitLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomInitConfig;

import java.util.HashMap;
import java.util.Map;

public class AdGainCustomerInit extends MediationCustomInitLoader {

    public static final String TAG = "AdGainCustomer";

    @Override
    public String getNetworkSdkVersion() {
        return AdGainSdk.getVersionName();
    }


    @Override
    public void initializeADN(Context context, MediationCustomInitConfig mediationCustomInitConfig, Map<String, Object> map) {
        Map<String, Object> customData = new HashMap<>();
        customData.put("custom_key", "custom_value");

        AdGainSdk.getInstance().init(context, new AdGainSdkConfig.Builder()
                .appId("1470_1264")       //必填，向广推商务获取
                .userId("")  // 用户ID，有就填
                .showLog(true)
                .addCustomData(customData)  //自定义数据
                .customController(new CustomController() {
                    // 是否允许SDK获取位置信息
                    @Override
                    public boolean canReadLocation() {
                        return true;
                    }

                    // 是否允许SDK获取手机状态地信息，如：imei deviceid
                    @Override
                    public boolean canUsePhoneState() {
                        return true;
                    }

                    // 是否允许SDK使用AndoridId
                    @Override
                    public boolean canUseAndroidId() {
                        return true;

                    }

                    // 是否允许SDK获取Wifi状态
                    @Override
                    public boolean canUseWifiState() {
                        return true;
                    }

                    @Override
                    public Location getLocation() {
                        return super.getLocation();
                    }

                    @Override
                    public String getMacAddress() {
                        return super.getMacAddress();
                    }

                    @Override
                    public String getImei() {
                        return super.getImei();
                    }

                    @Override
                    public String getAndroidId() {
                        return super.getAndroidId();
                    }

                    // 为SDK提供oaid
                    @Override
                    public String getOaid() {
                        return "";

                    }
                })
                .setInitCallback(new InitCallback() {
                    // 初始化成功回调，初始化成功后才可以加载广告
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "init--------------onSuccess-----------");
                        callInitSuccess();
                    }

                    // 初始化失败回调
                    @Override
                    public void onFail(int code, String msg) {
                        Log.d(TAG, "init--------------onFail-----------" + code + ":" + msg);
                    }
                }).build());

        // 个性化广告开关设置
        AdGainSdk.getInstance().setPersonalizedAdvertisingOn(true);
    }

    @Override
    public String getBiddingToken(Context context, Map<String, Object> extra) {
        return "";
    }

    @Override
    public String getSdkInfo(Context context, Map<String, Object> extra) {
        return "";
    }

}
