package com.windmill.android.demo.custom.gtad;

import android.app.Activity;
import android.util.Log;

import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.GTAdInfo;
import com.gt.sdk.api.RewardAd;
import com.gt.sdk.api.RewardAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomRewardAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class GtAdCustomerReward extends WMCustomRewardAdapter implements RewardAdListener {
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
                    .setAdUnitID(unitId)
                    .setExtOption(options)
                    .setPortrait(false)
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
            ad.sendWinNotification(GtAdAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(GtAdAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
    }

    @Override
    public void onRewardAdLoadSuccess(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdLoadSuccess: " + unitId + "gtAdInfo: " + gtAdInfo);
        if (gtAdInfo != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(gtAdInfo.getPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onRewardAdLoadCached(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdLoadCached: " + unitId + "gtAdInfo: " + gtAdInfo);
    }

    @Override
    public void onRewardAdShow(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdShow: " + unitId + "gtAdInfo: " + gtAdInfo);
        callVideoAdShow();
    }

    @Override
    public void onRewardAdPlayStart(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdPlayStart: " + unitId + "gtAdInfo: " + gtAdInfo);

    }

    @Override
    public void onRewardAdPLayEnd(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdPLayEnd: " + unitId + "gtAdInfo: " + gtAdInfo);
        callVideoAdPlayComplete();
    }

    @Override
    public void onRewardAdClick(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdClick: " + unitId + "gtAdInfo: " + gtAdInfo);
        callVideoAdClick();
    }

    @Override
    public void onRewardAdClosed(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onRewardAdClosed: " + unitId + "gtAdInfo: " + gtAdInfo);
        callVideoAdClosed();
    }

    @Override
    public void onRewardAdLoadError(String unitId, AdError adError) {
        Log.d(TAG, "onRewardAdLoadError: " + unitId + "adError: " + adError);
        callLoadFail(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onRewardAdShowError(String unitId, AdError adError) {
        Log.d(TAG, "onRewardAdShowError: " + unitId + "error: " + adError);
        callVideoAdPlayError(new WMAdapterError(adError.getErrorCode(), adError.getMessage()));
    }

    @Override
    public void onReward(String unitId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onReward: " + unitId + "gtAdInfo: " + gtAdInfo);
        callVideoAdReward(true);
    }
}
