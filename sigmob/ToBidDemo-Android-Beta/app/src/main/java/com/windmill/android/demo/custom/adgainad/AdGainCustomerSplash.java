package com.windmill.android.demo.custom.adgainad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Size;
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
    private static final String LOCAL_EXTRA_LOAD_TIMEOUT_MS = "load_timeout_ms";

    private SplashAd splashAd;


    @Override
    public void loadAd(Activity activity, ViewGroup viewGroup, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        Log.d(TAG, "loadAd: l: " + localExtra);
        Log.d(TAG, "loadAd: s: " + serverExtra);
        try {
            // 这个数值来自sigmob后台广告位ID的配置
            String unitId = (String) serverExtra.get(WMConstants.PLACEMENT_ID);
            Map<String, Object> options = new HashMap<>(serverExtra);
            if (localExtra != null) {
                options.putAll(localExtra);
            }
            Size size = getSizeParam(viewGroup);
            long loadTimeout = getLoadTimeParam(localExtra);
            AdRequest adRequest = new AdRequest.Builder()
                    .setCodeId(unitId)
                    .setWidth(size.getWidth())
                    .setHeight(size.getHeight())
                    .setExtOption(options)
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
            ad.sendWinNotification(AdGainAdapterUtil.getBidingWinNoticeParam(price, referBidInfo));
        } else {
            ad.sendLossNotification(AdGainAdapterUtil.getBidingLossNoticeParam(price, referBidInfo));
        }
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

    @Override
    public void onAdLoadSuccess() {
        Log.d(TAG, "onSplashAdLoadSuccess: " );
        Log.d(TAG, "onSplashAdLoadSuccess: bidtype: " + getBiddingType());
        if (splashAd != null && getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            BidPrice bidPrice = new BidPrice(String.valueOf(splashAd.getBidPrice()));
            callLoadBiddingSuccess(bidPrice);
        }
        callLoadSuccess();
    }

    @Override
    public void onAdCacheSuccess() {

    }

    @Override
    public void onSplashAdLoadFail(AdError error) {
        Log.d(TAG, "onSplashAdLoadFail: "  + " err: " + error);
        callLoadFail(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdShow() {
        Log.d(TAG, "onSplashAdShow: ");
        callSplashAdShow();
    }

    @Override
    public void onSplashAdShowError(AdError error) {
        Log.d(TAG, "onSplashAdShowError: "  + " err: " + error);
        callSplashAdShowError(new WMAdapterError(error.getErrorCode(), error.getMessage()));
    }

    @Override
    public void onSplashAdClick() {
        Log.d(TAG, "onSplashAdClick: ");
        callSplashAdClick();
    }

    @Override
    public void onSplashAdClose(boolean isSkip) {
        Log.d(TAG, "onSplashAdClose: ");
        callSplashAdClosed();
    }
}
