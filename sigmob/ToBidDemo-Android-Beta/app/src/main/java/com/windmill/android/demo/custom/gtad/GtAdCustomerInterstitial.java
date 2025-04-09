package com.windmill.android.demo.custom.gtad;

import android.app.Activity;
import android.util.Log;

import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.GTAdInfo;
import com.gt.sdk.api.InterstitialAd;
import com.gt.sdk.api.InterstitialAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomInterstitialAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class GtAdCustomerInterstitial extends WMCustomInterstitialAdapter implements InterstitialAdListener {
    private static final String TAG = "GtAdCustomerInter";

    private InterstitialAd interstitialAd;
    @Override
    public void loadAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s: " + serverExtra);
        try {
            // 这个数值来自sigmob后台广告位ID的配置
            String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
            Map<String, Object> options = new HashMap<>(serverExtra);
            if (localExtra != null) {
                options.putAll(localExtra);
            }
            AdRequest adRequest = new AdRequest.Builder()
                    .setAdUnitID(unitId)
                    .setExtOption(options)
                    .build();
            interstitialAd = new InterstitialAd(adRequest, this);
            interstitialAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception: ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(),
                    "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public void showAd(Activity activity, HashMap<String, String> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "showAd: l: " + localExtra);
        Log.d(TAG, "showAd: s: " + serverExtra);
        try {
            interstitialAd.showAd(activity);
        } catch (Throwable tr) {
            Log.e(TAG, "showAd exception: ", tr);
            callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(),
                    "catch GtAd presentVideoAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        return interstitialAd != null && interstitialAd.isReady();
    }

    @Override
    public void destroyAd() {
        Log.d(TAG, "destroyAd");
        if (interstitialAd != null) {
            interstitialAd.destroyAd();
            interstitialAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        super.notifyBiddingResult(isWin, price, referBidInfo);
        InterstitialAd ad = interstitialAd;
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
    public void onInterstitialAdLoadError(String adUnitID, AdError adError) {
        Log.d(TAG, "onInterstitialAdLoadError: " + adUnitID + " err: " + adError);
        callLoadFail(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onInterstitialAdLoadSuccess(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdLoadSuccess: " + adUnitID + " gtAdInfo: " + gtAdInfo);
        Log.d(TAG, "onInterstitialAdLoadSuccess: bidtype: " + getBiddingType());
        if (gtAdInfo != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(gtAdInfo.getPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onInterstitialAdLoadCached(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdLoadCached: " + adUnitID + " adInfo: " + gtAdInfo);
    }

    @Override
    public void onInterstitialAdShow(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdShow: " + adUnitID + " adInfo: " + gtAdInfo);
        callVideoAdShow();
    }

    @Override
    public void onInterstitialAdPlayStart(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdPlayStart: " + adUnitID + " adInfo: " + gtAdInfo);
    }

    @Override
    public void onInterstitialAdPLayEnd(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdPLayEnd: " + adUnitID + " adInfo: " + gtAdInfo);
        callVideoAdPlayComplete();
    }

    @Override
    public void onInterstitialAdClick(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdClick: " + adUnitID + " adInfo: " + gtAdInfo);
        callVideoAdClick();
    }

    @Override
    public void onInterstitialAdClosed(String adUnitID, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onInterstitialAdClosed: " + adUnitID + " adInfo: " + gtAdInfo);
        callVideoAdClosed();
    }

    @Override
    public void onInterstitialAdShowError(String adUnitID, AdError adError) {
        Log.d(TAG, "onInterstitialAdShowError: " + adUnitID + " err: " + adError);
        callVideoAdPlayError(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }
}
