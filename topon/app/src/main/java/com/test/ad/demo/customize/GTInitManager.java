

package com.test.ad.demo.customize;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.MediationInitCallback;
import com.gt.sdk.GTAdSdk;
import com.gt.sdk.api.GtCustomController;
import com.gt.sdk.api.GtInitCallback;
import com.gt.sdk.api.GtSdkConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GTInitManager extends ATInitMediation {

    public static final String TAG = "GTAdapter";

    private volatile static GTInitManager sInstance;

    int personAdStatus = 0;

    private boolean mHasInit;
    private String mLocalInitAppId;
    private final AtomicBoolean mIsIniting;

    private final Object mLock = new Object();

    private List<MediationInitCallback> mListeners;

    private GTInitManager() {
        mIsIniting = new AtomicBoolean(false);
    }

    public static GTInitManager getInstance() {
        if (sInstance == null) {
            synchronized (GTInitManager.class) {
                if (sInstance == null) sInstance = new GTInitManager();
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

        String app_id = getStringFromMap(serviceExtras, "app_id");

        if (onInitCallback != null) {
            mListeners.add(onInitCallback);
        }

        if (serviceExtras.containsKey(ATInitMediation.KEY_LOCAL)) {
            mLocalInitAppId = app_id;

        } else if (mLocalInitAppId != null && !TextUtils.equals(mLocalInitAppId, app_id)) {
            checkToSaveInitData(getNetworkName(), serviceExtras, mLocalInitAppId);
            mLocalInitAppId = null;
        }

        Map<String, Object> customData = new HashMap<>();
        customData.put("custom_key", "custom_value");

        Log.d(TAG, "initSDK: real start  app_id = " + app_id);

        GTAdSdk.getInstance().init(context, new GtSdkConfig.Builder()
                .appId(app_id)         //必填
                .userId("")            // 非必须，有就填
                .debugEnv(true)   // 线上环境务必传 false
                .showLog(true)    // 是否展示 广推adsdk 日志
                .addCustomData(customData) //自定义数据

                .customController(new GtCustomController() {

                    @Override
                    public boolean canReadLocation() {
                        return true;
                    }

                    // imei deviceid
                    @Override
                    public boolean canUsePhoneState() {
                        return true;
                    }

                    @Override
                    public boolean canUseAndroidId() {
                        return true;
                    }

                    @Override
                    public boolean canUseWriteExternal() {
                        return true;
                    }

                    @Override
                    public boolean canReadInstalledPackages() {

                        return true;
                    }

                    @Override
                    public boolean canUseWifiState() {

                        return true;
                    }

                    @Override
                    public String getOaid() {

                        return "";
                    }
                })
                .setInitCallback(new GtInitCallback() {
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
            GTAdSdk.getInstance().setPersonalizedAdvertisingOn(false);

        } else {
            GTAdSdk.getInstance().setPersonalizedAdvertisingOn(true);  // 开启 个性化广告
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
        return GTAdSdk.getNetworkName();
    }

    @Override
    public String getNetworkVersion() {
        return GTAdSdk.getVersionName();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.gt.sdk.base.activity.AdActivity";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.gt.sdk.base.activity.LandAdActivity");
        list.add("com.gt.sdk.base.activity.LandTransparentAdActivity");
        list.add("com.gt.sdk.base.activity.PortraitAdActivity");
        list.add("com.gt.sdk.base.activity.PortraitTransparentAdActivity");
        list.add("com.gt.sdk.base.activity.TransparentAdActivity");

        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
//        list.add("com.qq.e.comm.DownloadService");
        return list;
    }
}
