package com.union_test.toutiao.adgain;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.InterstitialAd;
import com.adgain.sdk.api.InterstitialAdListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.interstitial.MediationCustomInterstitialLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomServiceConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Date   :   2025/5/15
 * Time   :   14:24
 */
public class AdGainInterAdapter extends MediationCustomInterstitialLoader {

    private static final String TAG = AdGainCustomerInit.TAG;

    InterstitialAd mInterstitialAd;

    public AdGainInterAdapter() {
    }

    @Override
    public void load(Context context, AdSlot adSlot, MediationCustomServiceConfig serviceConfig) {

        try {
            if (serviceConfig == null) {
                Log.d(TAG, "inter load: serviceConfig is null");
                return;
            }

            InterstitialAdListener listener = new InterstitialAdListener() {

                @Override
                public void onInterstitialAdLoadSuccess() {
                    Log.d(TAG, "onInterstitialAdLoadSuccess: ");
                    callLoadSuccess(mInterstitialAd.getBidPrice()*1000);  // 单位 分
                }

                @Override
                public void onInterstitialAdLoadCached() {
                    Log.d(TAG, "onInterstitialAdLoadCached: ");
                }

                @Override
                public void onInterstitialAdLoadError(AdError error) {
                    if (error != null) {
                        Log.i(TAG, "onInterstitialAdLoadError errorCode = " + error.getErrorCode() + " errorMessage = " + error.getMessage());
                        callLoadFail(error.getErrorCode(), error.getMessage());

                    } else {
                        callLoadFail(40000, "no ad");
                    }
                }

                @Override
                public void onInterstitialAdShow() {
                    Log.d(TAG, "onInterstitialAdShow: ");
                    callInterstitialShow();
                }

                @Override
                public void onInterstitialAdPlayEnd() {

                }

                @Override
                public void onInterstitialAdClick() {
                    Log.d(TAG, "onInterstitialAdClick: ");
                    callInterstitialAdClick();
                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    callInterstitialClosed();
                }

                @Override
                public void onInterstitialAdShowError(AdError error) {

                }
            };

            Map<String, Object> options = new HashMap<>();
            options.put("inter_extra_test_key", "inter_extra_test_value");

            AdRequest adRequest = new AdRequest.Builder()
                    .setCodeId(serviceConfig.getADNNetworkSlotId())  // 广推广告位 从商务获取
                    .setExtOption(options)
                    .setOrientation(0)  //0 竖屏  1、横屏  2 随屏幕方向改变
                    .build();

            mInterstitialAd = new InterstitialAd(adRequest, listener);

            mInterstitialAd.loadAd();

            Log.i(TAG, "inter load");
        } catch (Exception e) {
            Log.d(TAG, "inter load: error = " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void showAd(Activity activity) {
        Log.i(TAG, "inter 自定义的showAd");

        try {
            if (mInterstitialAd != null && mInterstitialAd.isReady()) {
                mInterstitialAd.showAd(activity);
            }
        } catch (Exception e) {
            Log.d(TAG, "inter showAd: error = " + Log.getStackTraceString(e));
        }
    }

    @Override
    public MediationConstant.AdIsReadyStatus isReadyCondition() {
        return mInterstitialAd != null && mInterstitialAd.isReady() ?
                MediationConstant.AdIsReadyStatus.AD_IS_READY
                : MediationConstant.AdIsReadyStatus.AD_IS_NOT_READY;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "inter onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "inter onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "inter onDestroy");
        if (mInterstitialAd != null) {
            mInterstitialAd.destroyAd();
            mInterstitialAd = null;
        }
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

        AdGainBiddingNotice.notifyADN(mInterstitialAd, b, v, i, map);
    }
}
