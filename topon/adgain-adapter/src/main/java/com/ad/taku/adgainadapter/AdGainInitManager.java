package com.ad.taku.adgainadapter;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adgain.sdk.AdGainSdk;
import com.adgain.sdk.api.AdGainSdkConfig;
import com.adgain.sdk.api.CustomController;
import com.adgain.sdk.api.IBidding;
import com.adgain.sdk.api.InitCallback;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.MediationInitCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdGainInitManager extends ATInitMediation {

    public static final String TAG = "AdGainAdapter";

    private volatile static AdGainInitManager sInstance;

    int personAdStatus = 0;

    private boolean mHasInit;
    private String mLocalInitAppId;
    private final AtomicBoolean mIsIniting;

    private final Object mLock = new Object();

    private List<MediationInitCallback> mListeners;

    private AdGainInitManager() {
        mIsIniting = new AtomicBoolean(false);
    }

    public static AdGainInitManager getInstance() {
        if (sInstance == null) {
            synchronized (AdGainInitManager.class) {
                if (sInstance == null) sInstance = new AdGainInitManager();
            }
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, MediationInitCallback onInitCallback) {
        try {
            personAdStatus = ATSDK.getPersionalizedAdStatus();
            Log.d(TAG, "initSDK: personalAd = " + personAdStatus);
        } catch (Throwable ignored) {
        }

        if (mHasInit) {
            if (onInitCallback != null) {
                onInitCallback.onSuccess();
            }
            return;
        }

        synchronized (mLock) {

            if (mIsIniting.get()) {
                if (onInitCallback != null) {
                    mListeners.add(onInitCallback);
                }
                return;
            }

            if (mListeners == null) {
                mListeners = new ArrayList<>();
            }

            mIsIniting.set(true);
        }

        String appId = getStringFromMap(serviceExtras, "app_id");
        if (TextUtils.isEmpty(appId)) {
            appId = getStringFromMap(serviceExtras, "appId");
        }
        if (onInitCallback != null) {
            mListeners.add(onInitCallback);
        }

        if (serviceExtras.containsKey(ATInitMediation.KEY_LOCAL)) {
            mLocalInitAppId = appId;
        } else if (mLocalInitAppId != null && !TextUtils.equals(mLocalInitAppId, appId)) {
            checkToSaveInitData(getNetworkName(), serviceExtras, mLocalInitAppId);
            mLocalInitAppId = null;
        }

        Map<String, Object> customData = new HashMap<>();
        customData.put(IBidding.THIRD_MEDIATION, "taku");

        Log.d(TAG, "initSDK: real start  appId = " + appId);
        AdGainSdk.getInstance().init(context, new AdGainSdkConfig.Builder()
                .appId(appId)         //必填
                .showLog(false)    // 是否展示 adsdk 日志
                .addCustomData(customData) //自定义数据

                .customController(new CustomController() {
                    @Override
                    public String getOaid() {
                        return "";
                    }
                })
                .setInitCallback(new InitCallback() {
                    @Override
                    public void onSuccess() {
                        // 初始化成功 后 再加载广告
                        Log.d(TAG, "initSDK ------------onSuccess----------- ");

                        mHasInit = true;
                        callbackResult(true, null, null);
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        Log.d(TAG, "initSDK ------------onFail----------- msg = " + msg);

                        callbackResult(false, code + "", "GDT initSDK failed." + msg);
                    }
                }).build());

        if (personAdStatus == ATAdConst.PRIVACY.PERSIONALIZED_LIMIT_STATUS) {
            AdGainSdk.getInstance().setPersonalizedAdvertisingOn(false);

        } else {
            AdGainSdk.getInstance().setPersonalizedAdvertisingOn(true);  // 开启 个性化广告
        }
    }

    private void callbackResult(boolean success, String errorCode, String errorMsg) {
        synchronized (mLock) {
            int size = mListeners.size();
            MediationInitCallback initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    if (success) {
                        initListener.onSuccess();
                    } else {
                        initListener.onFail(errorCode + " | " + errorMsg);
                    }
                }
            }
            mListeners.clear();

            mIsIniting.set(false);
        }
    }

    @Override
    public String getNetworkName() {
        return "AdGain";
    }

    @Override
    public String getNetworkVersion() {
        return AdGainSdk.getVersionName();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.adgain.sdk";
    }

    public static int getBidFloor(Map<String, Object> serverExtra) {
        return ATInitMediation.getIntFromMap(serverExtra, "bid_floor", 0);
    }


}
