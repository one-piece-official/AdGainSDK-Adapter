package com.windmill.android.demo.custom.adgainad;

import android.content.Context;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdLoadListener;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomNativeAdapter;
import com.windmill.sdk.models.BidPrice;
import com.windmill.sdk.natives.WMNativeAdData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdGainCustomerNative extends WMCustomNativeAdapter implements NativeAdLoadListener {
    private static final String TAG = "AdGainCustomerNative";

    private NativeUnifiedAd nativeUnifiedAd;
    private final List<WMNativeAdData> wmNativeAdDataList = new ArrayList<>();

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s:" + serverExtra);
        Log.d(TAG, "loadAd: ad:" + nativeUnifiedAd);
        try {
            wmNativeAdDataList.clear();
            // 这个数值来自sigmob后台广告位ID的配置
            if (null == nativeUnifiedAd) {
                String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
                Map<String, Object> options = new HashMap<>(serverExtra);
                if (localExtra != null) {
                    options.putAll(localExtra);
                }
                AdRequest adRequest = new AdRequest.Builder()
                        .setCodeId(unitId)
                        .setExtOption(options)
                        .build();
                nativeUnifiedAd = new NativeUnifiedAd(adRequest, this);
            }
            nativeUnifiedAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception: ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(),
                    "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        return nativeUnifiedAd != null && nativeUnifiedAd.isReady();
    }

    @Override
    public void destroyAd() {
        Log.d(TAG, "destroyAd");
        if (nativeUnifiedAd != null) {
            nativeUnifiedAd.destroyAd();
            nativeUnifiedAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        Log.d(TAG, "notifyBiddingResult: " + isWin + " " + price + " " + referBidInfo);
        super.notifyBiddingResult(isWin, price, referBidInfo);
        NativeUnifiedAd ad = nativeUnifiedAd;
        Log.d(TAG, "notifyBiddingResult: win: " + isWin + " price: " + price + " refer: " + referBidInfo + " ad: " + ad);
        if (null == ad) {
            return;
        }

        if (isWin) {
            // 竞价成功
            ad.sendWinNotification(AdGainAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(AdGainAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
    }

    @Override
    public List<WMNativeAdData> getNativeAdDataList() {
        Log.d(TAG, "getNativeAdDataList: " + wmNativeAdDataList);
        return wmNativeAdDataList;
    }


    public NativeUnifiedAd getNativeAd() {
        return nativeUnifiedAd;
    }

    @Override
    public void onAdError(AdError error) {
        Log.d(TAG, "onAdError: "  + " error: " + error);
        callLoadFail(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onAdLoad(List<NativeAdData> adDataList) {
        Log.d(TAG, "onAdLoad: " + " dataList: " + adDataList);
        if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            if (adDataList != null && !adDataList.isEmpty()) {
                NativeAdData adData = adDataList.get(0);
                // biding
                if (adData != null) {
                    BidPrice bidPrice = new BidPrice(String.valueOf(adData.getPrice()));
                    Log.d(TAG, "invoke callLoadBiddingSuccess: " + bidPrice);
                    callLoadBiddingSuccess(bidPrice);
                }
            }
        }
        if (adDataList != null) {
            for (NativeAdData data: adDataList) {
                wmNativeAdDataList.add(new AdGainNativeAdData(data, this));
            }
        }
        Log.d(TAG, "invoke callLoadSuccess");
        callLoadSuccess(wmNativeAdDataList);
    }
}
