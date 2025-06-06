package com.gromore.adapter.adgain;

import android.content.Context;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdLoadListener;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.native_ad.MediationCustomNativeLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomServiceConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdGainNativeAdapter extends MediationCustomNativeLoader {

    private static final String TAG = AdGainCustomerInit.TAG;
    private NativeUnifiedAd nativeUnifiedAd;

    @Override
    public void load(Context context, AdSlot adSlot, MediationCustomServiceConfig serviceConfig) {

        try {
            Log.e(TAG, "load custom native ad----- " + serviceConfig.getADNNetworkSlotId() + "  " + getBiddingType());

            Log.i(TAG, "自渲染");

            Map<String, Object> options = new HashMap<>();

            AdRequest adRequest = new AdRequest
                    .Builder()
                    .setCodeId(serviceConfig.getADNNetworkSlotId())
                    .setExtOption(options)
                    .build();

            nativeUnifiedAd = new NativeUnifiedAd(adRequest, new NativeAdLoadListener() {

                @Override
                public void onAdLoad(List<NativeAdData> list) {
                    if (list != null && !list.isEmpty()) {

                        List<AdGainNativeAdRender> tempList = new ArrayList<>();

                        for (NativeAdData feedAd : list) {

                            AdGainNativeAdRender gdtNativeAd = new AdGainNativeAdRender(context, feedAd, nativeUnifiedAd);

                            Map<String, Object> extraMsg = new HashMap<>();
                            extraMsg.put("key1_自渲染", "value1_自渲染");
                            gdtNativeAd.setMediaExtraInfo(extraMsg);

                            double ecpm = feedAd.getPrice();
                            Log.e(TAG, "ecpm:" + ecpm);
                            gdtNativeAd.setBiddingPrice(ecpm); //回传竞价广告价格

                            tempList.add(gdtNativeAd);
                        }

                        callLoadSuccess(tempList);

                    }
                }

                @Override
                public void onAdError(AdError adError) {

                    if (adError != null) {
                        Log.i(TAG, "onNoAD errorCode = " + adError.getErrorCode() + " errorMessage = " + adError.getMessage());
                        callLoadFail(adError.getErrorCode(), adError.getMessage());
                    } else {
                        callLoadFail(40000, "no ad");
                    }
                }
            });

            nativeUnifiedAd.loadAd();
        } catch (Exception e) {

        }
    }

    public boolean isClientBidding() {
        return getBiddingType() == MediationConstant.AD_TYPE_CLIENT_BIDING;
    }


    public boolean isServerBidding() {
        return getBiddingType() == MediationConstant.AD_TYPE_SERVER_BIDING;
    }

    @Override
    public void receiveBidResult(boolean win, double winnerPrice, int loseReason, Map<String, Object> extra) {
        super.receiveBidResult(win, winnerPrice, loseReason, extra);

        Log.d(TAG, "receiveBidResult: win = " + win + " winnerPrice = " + winnerPrice + " loseReason = " + loseReason + " extra = " + extra);

        AdGainBiddingNotice.notifyADN(nativeUnifiedAd, win, winnerPrice, loseReason, extra);
    }
}
