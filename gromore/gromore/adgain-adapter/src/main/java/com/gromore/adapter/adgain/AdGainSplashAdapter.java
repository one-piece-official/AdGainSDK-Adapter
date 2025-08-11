package com.gromore.adapter.adgain;

import static com.gromore.adapter.adgain.AdGainCustomerInit.TAG;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.SplashAd;
import com.adgain.sdk.api.SplashAdListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.splash.MediationCustomSplashLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomServiceConfig;

public class AdGainSplashAdapter extends MediationCustomSplashLoader implements GMBiddingUtil.NotifyBiddingListener {
    private SplashAd splashAd;

    public AdGainSplashAdapter() {
    }

    @Override
    public void load(Context context, AdSlot adSlot, MediationCustomServiceConfig serviceConfig) {

        try {
            if (serviceConfig == null) {
                Log.d(TAG, "splash load: serviceConfig is null");
                callLoadFail(40000, "serviceConfig 为 null");
                return;
            }

            SplashAdListener mSplashAdListener = new SplashAdListener() {

                @Override
                public void onAdLoadSuccess() {
                    Log.d(TAG, "splash ----------onAdLoadSuccess---------- " + splashAd.getBidPrice() + " " + isClientBidding());
                    if (isClientBidding()) {
                        callLoadSuccess(splashAd.getBidPrice());  // 单位分
                    }
                }

                @Override
                public void onAdCacheSuccess() {
                    Log.d(TAG, "splash ----------onAdCacheSuccess----------");
                    if (!isClientBidding()) {
                        callLoadSuccess();
                    }
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
                    if (isSkip) {
                        callSplashAdSkip();
                    } else {
                        callSplashAdDismiss();
                    }
                }

            };
            GMBiddingUtil.addNotifyBiddingListener(this);
            AdRequest adRequest = new AdRequest.Builder()
                    .setCodeId(serviceConfig.getADNNetworkSlotId())
                    .setBidFloor(AdGainCustomerInit.getBidFloor(serviceConfig.getCustomAdapterJson()))
                    .build();
            splashAd = new SplashAd(adRequest, mSplashAdListener);
            splashAd.loadAd();
        } catch (Exception e) {
            callLoadFail(40000, "Exception " + e.getMessage());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        GMBiddingUtil.removeNotifyBiddingListener(this);
        Log.i(TAG, "splash onDestroy");
        if (splashAd != null) {
            splashAd.destroyAd();
            splashAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(Object object) { // 如果是自己竞胜，曝光之后 isReady对应的status 就变了
        if (object instanceof CSJSplashAd && splashAd != null && splashAd.isReady()) {// 有填充才进行竞败回传
            String ecpm = ((CSJSplashAd) object).getMediationManager().getShowEcpm().getEcpm();
            GMBiddingUtil.adgainNotifyLoss(splashAd, ecpm, this);
        }
    }


}
