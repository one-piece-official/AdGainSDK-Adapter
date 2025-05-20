package com.windmill.android.demo.custom.adgainad;

import android.app.Activity;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.RewardAd;
import com.adgain.sdk.api.RewardAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomRewardAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class AdGainCustomerReward extends WMCustomRewardAdapter implements RewardAdListener {
    private static final String TAG = "GtAdCustomerReward";

    private RewardAd rewardAd;

    @Override
    public void loadAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s" + serverExtra);
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
            rewardAd = new RewardAd(adRequest, this);
            rewardAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(),
                    "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public void showAd(Activity activity, HashMap<String, String> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "showAd: l" + localExtra);
        Log.d(TAG, "showAd: s" + serverExtra);
        try {
            rewardAd.showAd(activity);
        } catch (Throwable tr) {
            Log.e(TAG, "showAd exception: ", tr);
            callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(),
                    "catch GtAd presentVideoAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        Log.d(TAG, "isReady: ad=" + rewardAd);
        if (rewardAd != null) Log.d(TAG, "isReady: ad ready = " + rewardAd.isReady());
        return rewardAd != null && rewardAd.isReady();
    }

    @Override
    public void destroyAd() {
        if (rewardAd != null) {
            rewardAd.destroyAd();
            rewardAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        RewardAd ad = rewardAd;
        Log.d(TAG, "notifyBiddingResult: win: " + isWin + " price: " + price + " refer: " + referBidInfo + " ad: " + ad);
        if (null == ad) {
            return;
        }
        if (isWin) {
            ad.sendWinNotification(AdGainAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(AdGainAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
    }

    @Override
    public void onRewardAdLoadSuccess() {
        Log.d(TAG, "onRewardAdLoadSuccess: ");
        if (rewardAd != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(rewardAd.getBidPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onRewardAdLoadCached() {
        Log.d(TAG, "onRewardAdLoadCached: ");
    }

    @Override
    public void onRewardAdShow() {
        Log.d(TAG, "onRewardAdShow: ");
        callVideoAdShow();
    }

    @Override
    public void onRewardAdPlayStart() {

    }

    @Override
    public void onRewardAdPlayEnd() {
        Log.d(TAG, "onRewardAdPLayEnd: ");
        callVideoAdPlayComplete();
    }

    @Override
    public void onRewardAdClick() {
        Log.d(TAG, "onRewardAdClick: ");
        callVideoAdClick();
    }

    @Override
    public void onRewardAdClosed() {
        Log.d(TAG, "onRewardAdClosed: ");
        callVideoAdClosed();
    }

    @Override
    public void onRewardAdLoadError(AdError adError) {
        Log.d(TAG, "onRewardAdLoadError: " + "adError: " + adError);
        callLoadFail(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onRewardAdShowError(AdError adError) {
        Log.d(TAG, "onRewardAdShowError: " + "error: " + adError);
        callVideoAdPlayError(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onRewardVerify() {
        Log.d(TAG, "onReward: ");
        callVideoAdReward(true);
    }

    @Override
    public void onAdSkip() {

    }
}
