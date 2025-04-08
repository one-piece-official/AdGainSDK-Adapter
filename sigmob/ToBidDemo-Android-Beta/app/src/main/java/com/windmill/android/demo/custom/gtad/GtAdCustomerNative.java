package com.windmill.android.demo.custom.gtad;

import android.content.Context;
import android.util.Log;

import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.NativeAdData;
import com.gt.sdk.api.NativeAdLoadListener;
import com.gt.sdk.api.NativeUnifiedAd;
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

public class GtAdCustomerNative extends WMCustomNativeAdapter implements NativeAdLoadListener {
    private static final String TAG = "GtAdCustomerNative";

    private NativeUnifiedAd nativeUnifiedAd;
    private final List<WMNativeAdData> wmNativeAdDataList = new ArrayList<>();

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s" + serverExtra);
        try {
            wmNativeAdDataList.clear();
            // 这个数值来自sigmob后台广告位ID的配置
            String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
            Map<String, Object> options = new HashMap<>(localExtra);
            AdRequest adRequest = new AdRequest.Builder()
                    .setAdUnitID(unitId)
                    .setExtOption(options)
                    .build();
            nativeUnifiedAd = new NativeUnifiedAd(adRequest, this);
            nativeUnifiedAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception: ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(),
                    "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        Log.d(TAG, "isReady: " + nativeUnifiedAd);
        if (nativeUnifiedAd != null) {
            Log.d(TAG, "isReady: ready: " + nativeUnifiedAd.isReady());
        }
        return nativeUnifiedAd != null && nativeUnifiedAd.isReady();
    }

    @Override
    public void destroyAd() {
        if (nativeUnifiedAd != null) {
            nativeUnifiedAd.destroyAd();
            nativeUnifiedAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        super.notifyBiddingResult(isWin, price, referBidInfo);
        NativeUnifiedAd ad = nativeUnifiedAd;
        Log.d(TAG, "notifyBiddingResult: win: " + isWin + " price: " + price + " refer: " + referBidInfo + " ad: " + ad);
        if (null == ad) {
            return;
        }

        if (isWin) {
            // 竞价成功
            ad.sendWinNotification(GtAdAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(GtAdAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
    }

    @Override
    public List<WMNativeAdData> getNativeAdDataList() {
        Log.d(TAG, "getNativeAdDataList: " + wmNativeAdDataList);
        return wmNativeAdDataList;
    }

    @Override
    public void onAdError(String codeId, AdError error) {
        Log.d(TAG, "onAdError: " + codeId + " error: " + error);
        callLoadFail(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onAdLoad(String codeId, List<NativeAdData> adDataList) {
        Log.d(TAG, "onAdLoad: " + codeId + " dataList: " + adDataList);
        if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            if (adDataList != null && !adDataList.isEmpty()) {
                NativeAdData adData = adDataList.get(0);
                // biding
                if (adData != null) {
                    BidPrice bidPrice = new BidPrice(String.valueOf(adData.getPrice()));
                    callLoadBiddingSuccess(bidPrice);
                }
            }
        }
        if (adDataList != null) {
            for (NativeAdData data: adDataList) {
                wmNativeAdDataList.add(new GtAdNativeAdData(data, this));
            }
        }
        callLoadSuccess(wmNativeAdDataList);
    }
}
