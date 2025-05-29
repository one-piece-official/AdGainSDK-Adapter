package com.adgain.demo;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.adgain.sdk.AdGainSdk;
import com.adgain.sdk.api.AdGainSdkConfig;
import com.adgain.sdk.api.CustomController;
import com.adgain.sdk.api.InitCallback;
import com.windmill.sdk.WMAdConfig;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WMCustomController;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.custom.WMCustomAdapterProxy;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdGainCustomerProxy extends WMCustomAdapterProxy {
    private static final String TAG = "AdGainCustomerProxy";
    private static final String SERVER_EXTRA_CUSTOM_APP_ID = "appId";
    private WMCustomController controller = null;

    @Override
    public void initializeADN(Context context, Map<String, Object> serverExtra) {
        Log.d(TAG, "initializeADN: s: " + serverExtra);
        try {
            String customInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);
            JSONObject joCustom = new JSONObject(customInfo);
            String gtAdAppId = joCustom.getString(SERVER_EXTRA_CUSTOM_APP_ID);
            HashMap<String, Object> customData = new HashMap<>(serverExtra);
            WMAdConfig adConfig = WindMillAd.sharedAds().getAdConfig();
            if (adConfig != null && adConfig.getCustomController() != null) {
                controller = adConfig.getCustomController();
            }
            AdGainSdkConfig config = new AdGainSdkConfig.Builder()
                    .appId(gtAdAppId)
                    .showLog(true)
                    .addCustomData(customData)
                    .setInitCallback(new InitCallback() {
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
                    .customController(new CustomController() {
                        @Override
                        public boolean canReadLocation() {
                            if (controller != null) {
                                return controller.isCanUseLocation();
                            }
                            return super.canReadLocation();
                        }

                        @Override
                        public boolean canUsePhoneState() {
                            if (controller != null) {
                                return controller.isCanUsePhoneState();
                            }
                            return super.canUsePhoneState();
                        }

                        @Override
                        public boolean canUseAndroidId() {
                            if (controller != null) {
                                return controller.isCanUseAndroidId();
                            }
                            return super.canUseAndroidId();
                        }

                        @Override
                        public boolean canUseWifiState() {
                            if (controller != null) {
                                return controller.isCanUseWifiState();
                            }
                            return super.canUsePhoneState();
                        }

                        @Override
                        public String getOaid() {
                            if (controller != null) {
                                return controller.getDevOaid();
                            }
                            return super.getOaid();
                        }

                        @Override
                        public Location getLocation() {
                            if (controller != null) {
                                return controller.getLocation();
                            }
                            return super.getLocation();
                        }

                        @Override
                        public String getMacAddress() {
                            if (controller != null) {
                                return controller.getMacAddress();
                            }
                            return super.getMacAddress();
                        }

                        @Override
                        public String getImei() {
                            if (controller != null) {
                                return controller.getDevImei();
                            }
                            return super.getImei();
                        }

                        @Override
                        public String getAndroidId() {
                            if (controller != null) {
                                return controller.getAndroidId();
                            }
                            return super.getAndroidId();
                        }
                    })
                    .build();
            AdGainSdk.getInstance().init(context, config);
            updatePrivacySetting();
        } catch (Throwable tr) {
            Log.e(TAG, "initializeADN exception: ", tr);
            callInitFail(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "initializeADN exception: " + Log.getStackTraceString(tr));
        }
    }

    @Override
    public String getNetworkSdkVersion() {
        return AdGainSdk.getVersionName();
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
        Log.d(TAG, "updatePrivacySetting");
        AdGainSdk.getInstance().setPersonalizedAdvertisingOn(
                WindMillAd.sharedAds().isPersonalizedAdvertisingOn());
    }
}
