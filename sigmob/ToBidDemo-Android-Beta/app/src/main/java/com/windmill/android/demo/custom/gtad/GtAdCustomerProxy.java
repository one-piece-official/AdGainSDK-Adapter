package com.windmill.android.demo.custom.gtad;

import android.content.Context;
import android.util.Log;

import com.gt.sdk.GTAdSdk;
import com.gt.sdk.api.GtInitCallback;
import com.gt.sdk.api.GtSdkConfig;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.custom.WMCustomAdapterProxy;

import org.json.JSONObject;

import java.util.Map;

public class GtAdCustomerProxy extends WMCustomAdapterProxy {
    private static final String TAG = "GtAdCustomerProxy";
    private static final String SERVER_EXTRA_CUSTOM_APP_ID = "appId";

    @Override
    public void initializeADN(Context context, Map<String, Object> serverExtra) {
        Log.d(TAG, "initializeADN: s: " + serverExtra);
        try {
            String customInfo = (String)serverExtra.get(WMConstants.CUSTOM_INFO);
            JSONObject joCustom = new JSONObject(customInfo);
            String gtAdAppId = joCustom.getString(SERVER_EXTRA_CUSTOM_APP_ID);
            GtSdkConfig config = new GtSdkConfig.Builder()
                    .appId(gtAdAppId)
                    .debugEnv(true)
                    .showLog(true)
                    .setInitCallback(new GtInitCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Gt init onSuccess");
                            callInitSuccess();
                        }

                        @Override
                        public void onFail(int code, String message) {
                            Log.d(TAG, "Gt init onFail " + code + " msg: " + message);
                            callInitFail(code, message);
                        }
                    })
                    .build();
            GTAdSdk.getInstance().init(context, config);
            updatePrivacySetting();
        } catch (Throwable tr) {
            Log.e(TAG, "initializeADN exception: ", tr);
            callInitFail(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "initializeADN exception: " + Log.getStackTraceString(tr));
        }
    }

    @Override
    public String getNetworkSdkVersion() {
        return GTAdSdk.getVersionName();
    }

    @Override
    public int baseOnToBidCustomAdapterVersion() {
        return WMConstants.TO_BID_CUSTOM_ADAPTER_VERSION_2;
    }

    @Override
    public void notifyPrivacyStatusChange() {
        Log.d(TAG, "notifyPrivacyStatusChange");
        updatePrivacySetting();
    }

    private void updatePrivacySetting() {
        GTAdSdk.getInstance().setPersonalizedAdvertisingOn(
                WindMillAd.sharedAds().isPersonalizedAdvertisingOn());
    }
}
