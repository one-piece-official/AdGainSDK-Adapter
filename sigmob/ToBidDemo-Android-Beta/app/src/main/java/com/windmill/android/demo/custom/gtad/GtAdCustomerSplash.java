package com.windmill.android.demo.custom.gtad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Size;
import android.view.ViewGroup;

import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.GTAdInfo;
import com.gt.sdk.api.SplashAd;
import com.gt.sdk.api.SplashAdListener;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomSplashAdapter;
import com.windmill.sdk.models.BidPrice;

import java.util.HashMap;
import java.util.Map;

public class GtAdCustomerSplash extends WMCustomSplashAdapter implements SplashAdListener {

    private static final String TAG = "GtAdCustomerSplash";
    private static final String LOCAL_EXTRA_LOAD_TIMEOUT_MS = "load_timeout_ms";

    private SplashAd splashAd;


    @Override
    public void loadAd(Activity activity, ViewGroup viewGroup, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s: " + serverExtra);
        try {
            // 这个数值来自sigmob后台广告位ID的配置
            String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
            Map<String, Object> options = new HashMap<>(localExtra);
            Size size = getSizeParam(viewGroup);
            long loadTimeout = getLoadTimeParam(localExtra);
            AdRequest adRequest = new AdRequest.Builder()
                    .setAdUnitID(unitId)
                    .setWidth(size.getWidth())
                    .setHeight(size.getHeight())
                    .setExtOption(options)
                    .setSplashAdLoadTimeoutMs(loadTimeout)
                    .build();
            splashAd = new SplashAd(adRequest, this);
            splashAd.loadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "loadAd exception: ", tr);
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(),
                    "catch GtAd loadAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public void showAd(Activity activity, ViewGroup viewGroup, Map<String, Object> serverExtra) {
        Log.d(TAG, "showAd: s: " + serverExtra);
        try {
            splashAd.showAd(viewGroup);
        } catch (Throwable tr) {
            Log.e(TAG, "showAd exception: ", tr);
            callSplashAdShowError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(),
                    "catch GtAd presentVideoAd error " + Log.getStackTraceString(tr)));
        }
    }

    @Override
    public boolean isReady() {
        return splashAd != null && splashAd.isReady();
    }

    @Override
    public void destroyAd() {
        Log.d(TAG, "destroyAd");
        if (splashAd != null) {
            splashAd.destroyAd();
            splashAd = null;
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {
        super.notifyBiddingResult(isWin, price, referBidInfo);
        SplashAd ad = splashAd;
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
    public void onSplashAdLoadSuccess(String codeId, GTAdInfo gtAdInfo) {
        Log.d(TAG, "onSplashAdLoadSuccess: " + codeId + " gtadInfo: " + gtAdInfo);
        Log.d(TAG, "onSplashAdLoadSuccess: bidtype: " + getBiddingType());
        if (gtAdInfo != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(gtAdInfo.getPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onSplashAdLoadFail(String codeId, AdError error) {
        Log.d(TAG, "onSplashAdLoadFail: " + codeId + " err: " + error);
        callLoadFail(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdShow(String codeId, GTAdInfo adInfo) {
        Log.d(TAG, "onSplashAdShow: " + codeId + " gtadInfo: " + adInfo);
        callSplashAdShow();
    }

    @Override
    public void onSplashAdShowError(String codeId, AdError error) {
        Log.d(TAG, "onSplashAdShowError: " + codeId + " err: " + error);
        callSplashAdShowError(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdClick(String codeId, GTAdInfo adInfo) {
        Log.d(TAG, "onSplashAdClick: " + codeId + " gtadInfo: " + adInfo);
        callSplashAdClick();
    }

    @Override
    public void onSplashAdClose(String codeId, GTAdInfo adInfo) {
        Log.d(TAG, "onSplashAdClose: " + codeId + " gtadInfo: " + adInfo);
        callSplashAdClosed();
    }

    private Size getSizeParam(ViewGroup viewGroup) {
        int w = 1080;
        int h = 0;
        if (viewGroup != null) {
            w = viewGroup.getWidth();
            h = viewGroup.getHeight();

            if (w == 0 || h == 0) {
                Context context = viewGroup.getContext();
                if (context != null) {
                    w = context.getResources().getDisplayMetrics().widthPixels;
                    h = context.getResources().getDisplayMetrics().heightPixels;
                }
            }
        }
        return new Size(w, h);
    }

    private long getLoadTimeParam(Map<String, Object> localExtra) {
        try {
            if (localExtra != null && localExtra.containsKey(LOCAL_EXTRA_LOAD_TIMEOUT_MS)) {
                Object obj = localExtra.get(LOCAL_EXTRA_LOAD_TIMEOUT_MS);
                if (obj instanceof Number) {
                    Number n = (Number) obj;
                    return n.intValue();
                }
            }
        } catch (Throwable tr) {
            Log.e(TAG, "getLoadTimeParam exception", tr);
        }
        return 0;
    }
}
