package com.union_test.toutiao.adgain;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.SplashAd;
import com.adgain.sdk.api.SplashAdListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.splash.MediationCustomSplashLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomServiceConfig;

import java.util.HashMap;
import java.util.Map;

public class AdGainSplashAdapter extends MediationCustomSplashLoader {

    private static final String TAG = AdGainCustomerInit.TAG;

    private SplashAd splashAd;

    public AdGainSplashAdapter() {
    }

    @Override
    public void load(Context context, AdSlot adSlot, MediationCustomServiceConfig serviceConfig) {

        try {
            if (serviceConfig == null) {
                Log.d(TAG, "splash load: serviceConfig is null");
                return;
            }

            SplashAdListener mSplashAdListener = new SplashAdListener() {

                @Override
                public void onAdLoadSuccess() {
                    Log.d(TAG, "splash ----------onAdLoadSuccess---------- " + splashAd.getBidPrice());
                    callLoadSuccess(splashAd.getBidPrice());  // 单位分
                }

                @Override
                public void onAdCacheSuccess() {
                    Log.d(TAG, "splash ----------onAdCacheSuccess----------");

                }

                @Override
                public void onSplashAdLoadFail(AdError error) {
                    Log.d(TAG, "----------onSplashAdLoadFail----------" + error.toString());
                    if (error != null) {
                        Log.i(TAG, "onSplashAdLoadFail errorCode = " + error.getErrorCode() + " errorMessage = " + error.getMessage());
                        callLoadFail(error.getErrorCode(), error.getMessage());

                    } else {
                        callLoadFail(40000, "no ad");
                    }
                }

                @Override
                public void onSplashAdShow() {
                    Log.d(TAG, "----------onSplashAdShow----------");
                    callSplashAdShow();
                }

                @Override
                public void onSplashAdShowError(AdError error) {

                }

                @Override
                public void onSplashAdClick() {
                    Log.d(TAG, "----------onSplashAdClick----------");
                    callSplashAdClicked();
                }

                @Override
                public void onSplashAdClose(boolean isSkip) {
                    Log.d(TAG, "----------onSplashAdClose----------");
                    callSplashAdDismiss();
                }

            };

            Map<String, Object> options = new HashMap<>();
            options.put("splash_self_key", "splash_self_value");

            AdRequest adRequest = new AdRequest.Builder()
                    .setCodeId(serviceConfig.getADNNetworkSlotId())
                    .setExtOption(options)
                    .build();

            splashAd = new SplashAd(adRequest, mSplashAdListener);

            splashAd.loadAd();

            Log.i(TAG, "splash load");
        } catch (Exception e) {
            Log.d(TAG, "splash load: error = " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void showAd(ViewGroup container) {
        try {
            if (splashAd != null && splashAd.isReady()) {
                splashAd.showAd(container);
            }
        } catch (Exception e) {
            Log.d(TAG, "splash showAd: error = " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "splash onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "splash onResume");
    }

    @Override
    public MediationConstant.AdIsReadyStatus isReadyCondition() {
        return splashAd != null && splashAd.isReady() ?
                MediationConstant.AdIsReadyStatus.AD_IS_READY
                : MediationConstant.AdIsReadyStatus.AD_IS_NOT_READY;
    }

    /**
     * 是否clientBidding广告
     *
     * @return
     */
    public boolean isClientBidding() {
        return getBiddingType() == MediationConstant.AD_TYPE_CLIENT_BIDING;
    }

    /**
     * 是否serverBidding广告
     *
     * @return
     */
    public boolean isServerBidding() {
        return getBiddingType() == MediationConstant.AD_TYPE_SERVER_BIDING;
    }

    @Override
    public void receiveBidResult(boolean b, double v, int i, Map<String, Object> map) {
        super.receiveBidResult(b, v, i, map);
        Log.d(TAG, "receiveBidResult: win = " + b + " winnerPrice = " + v + " loseReason = " + i + " extra = " + map);

        AdGainBiddingNotice.notifyADN(splashAd, b, v, i, map);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "splash onDestroy");
        if (splashAd != null) {
            splashAd.destroyAd();
            splashAd = null;
        }
    }
}
