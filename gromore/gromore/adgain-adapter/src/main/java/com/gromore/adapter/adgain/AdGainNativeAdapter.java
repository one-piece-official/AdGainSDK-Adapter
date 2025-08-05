package com.gromore.adapter.adgain;

import android.content.Context;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdLoadListener;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.native_ad.MediationCustomNativeLoader;
import com.bytedance.sdk.openadsdk.mediation.custom.MediationCustomServiceConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdGainNativeAdapter extends MediationCustomNativeLoader implements GMBiddingUtil.NotifyBiddingListener {

    private static final String TAG = AdGainCustomerInit.TAG;
    private NativeUnifiedAd nativeUnifiedAd;
    private int ecpm;

    @Override
    public void load(Context context, AdSlot adSlot, MediationCustomServiceConfig serviceConfig) {

        try {
            if (serviceConfig == null) {
                callLoadFail(40000, "serviceConfig 为 null");
                return;
            }
            Log.e(TAG, "load custom native ad----- " + serviceConfig.getADNNetworkSlotId() + "  " + getBiddingType());

            Map<String, Object> options = new HashMap<>();

            AdRequest adRequest = new AdRequest.Builder()
                    .setCodeId(serviceConfig.getADNNetworkSlotId())
                    .setBidFloor(AdGainCustomerInit.getBidFloor(serviceConfig.getCustomAdapterJson()))
                    .setExtOption(options)
                    .build();
            nativeUnifiedAd = new NativeUnifiedAd(adRequest, new NativeAdLoadListener() {

                @Override
                public void onAdLoad(List<NativeAdData> list) {
                    if (list != null && !list.isEmpty()) {
                        List<AdGainNativeAdRender> tempList = new ArrayList<>();
                        for (NativeAdData feedAd : list) {
                            AdGainNativeAdRender nativeAd = new AdGainNativeAdRender(context, feedAd, nativeUnifiedAd);
                            nativeAd.setExpressAd(feedAd.getFeedView() != null);
                            ecpm = feedAd.getPrice();
                            Log.e(TAG, "ecpm:" + ecpm);
                            if (isClientBidding())
                                nativeAd.setBiddingPrice(ecpm); //回传竞价广告价格
                            tempList.add(nativeAd);
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
            GMBiddingUtil.addNotifyBiddingListener(this);
            nativeUnifiedAd.loadAd();
        } catch (Exception e) {
            callLoadFail(40000, "Exception " + e.getMessage());
        }
    }

    public boolean isClientBidding() {
        return getBiddingType() == MediationConstant.AD_TYPE_CLIENT_BIDING;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GMBiddingUtil.removeNotifyBiddingListener(this);
    }

    @Override
    public void notifyBiddingResult(Object object) {
        if (object instanceof TTFeedAd && nativeUnifiedAd != null && nativeUnifiedAd.isReady()) {// 有填充才进行竞败回传
            String gmEcpm = ((TTFeedAd) object).getMediationManager().getShowEcpm().getEcpm();
            try {
                if (ecpm < Double.parseDouble(gmEcpm)) {
                    GMBiddingUtil.adgainNotifyLoss(nativeUnifiedAd, gmEcpm, this);
                }
            } catch (Exception e) {
            }
        }
    }
}
