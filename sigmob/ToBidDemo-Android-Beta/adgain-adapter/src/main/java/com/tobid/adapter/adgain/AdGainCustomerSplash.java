package com.tobid.adapter.adgain;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.SplashAd;
import com.adgain.sdk.api.SplashAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomSplashAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class AdGainCustomerSplash extends WMCustomSplashAdapter implements SplashAdListener {

    private static final String TAG = "AdGainCustomerSplash";
    private SplashAd splashAd;


    @Override
    public void loadAd(Activity activity, ViewGroup viewGroup, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra + " s:" + serverExtra);
        try {
            // 这个数值来自sigmob后台广告位ID的配置
            String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
            Map<String, Object> options = new HashMap<>(serverExtra);
            if (localExtra != null) {
                options.putAll(localExtra);
            }
            int w = activity.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
            int h = activity.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
            AdRequest adRequest = new AdRequest.Builder().setCodeId(unitId)
                    .setWidth(w).setHeight(h).setExtOption(options).build();
            splashAd = new SplashAd(adRequest, this);
            splashAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception: ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public void showAd(Activity activity, ViewGroup viewGroup, Map<String, Object> serverExtra) {
        try {
            splashAd.showAd(viewGroup);
        } catch (Throwable tr) {
            Log.e(TAG, "showAd exception: ", tr);
            callSplashAdShowError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), "catch GtAd presentVideoAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        return splashAd != null && splashAd.isReady();
    }

    @Override
    public void destroyAd() {
        if (splashAd != null) {
            splashAd.destroyAd();
            splashAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        if (splashAd != null) {
            Log.d(TAG, "notifyBiddingResult: win: " + isWin + " price: " + price + " refer: " + referBidInfo);
            if (isWin) {
                // 竞价成功
                splashAd.sendWinNotification(AdGainAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
            } else {
                splashAd.sendLossNotification(AdGainAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
            }
        }
    }

    @Override
    public void onAdLoadSuccess() {
        Log.d(TAG, "onSplashAdLoadSuccess: bidtype: " + getBiddingType());
        if (splashAd != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(splashAd.getBidPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
    }

    @Override
    public void onAdCacheSuccess() {
        callLoadSuccess();
    }

    @Override
    public void onSplashAdLoadFail(AdError error) {
        callLoadFail(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdShow() {
        callSplashAdShow();
    }

    @Override
    public void onSplashAdShowError(AdError error) {
        callSplashAdShowError(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdClick() {
        callSplashAdClick();
    }

    @Override
    public void onSplashAdClose(boolean isSkip) {
        callSplashAdClosed();
    }
}
