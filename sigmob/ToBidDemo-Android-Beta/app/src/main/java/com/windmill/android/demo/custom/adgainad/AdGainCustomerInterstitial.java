package com.windmill.android.demo.custom.adgainad;

import android.app.Activity;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.InterstitialAd;
import com.adgain.sdk.api.InterstitialAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomInterstitialAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class AdGainCustomerInterstitial extends WMCustomInterstitialAdapter implements InterstitialAdListener {
    private static final String TAG = "AdGainCustomerInter";

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
                    .setCodeId(unitId)
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
            ad.sendWinNotification(AdGainAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(AdGainAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
    }

    @Override
    public void onInterstitialAdLoadError(AdError adError) {
        Log.d(TAG, "onInterstitialAdLoadError: " + " err: " + adError);
        callLoadFail(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onInterstitialAdLoadSuccess() {
        Log.d(TAG, "onInterstitialAdLoadSuccess: ");
        Log.d(TAG, "onInterstitialAdLoadSuccess: bidtype: " + getBiddingType());
        if (interstitialAd != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(interstitialAd.getBidPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onInterstitialAdLoadCached() {

    }

    @Override
    public void onInterstitialAdShow() {
        Log.d(TAG, "onInterstitialAdShow: ");
        callVideoAdShow();
    }

    @Override
    public void onInterstitialAdPlayEnd() {
        Log.d(TAG, "onInterstitialAdPLayEnd: ");
        callVideoAdPlayComplete();
    }

    @Override
    public void onInterstitialAdClick() {
        Log.d(TAG, "onInterstitialAdClick: ");
        callVideoAdClick();
    }

    @Override
    public void onInterstitialAdClosed() {
        Log.d(TAG, "onInterstitialAdClosed: ");
        callVideoAdClosed();
    }

    @Override
    public void onInterstitialAdShowError(AdError adError) {
        Log.d(TAG, "onInterstitialAdShowError: " + " err: " + adError);
        callVideoAdPlayError(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }
}
