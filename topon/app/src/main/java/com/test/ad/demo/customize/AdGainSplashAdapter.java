package com.test.ad.demo.customize;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.SplashAd;
import com.adgain.sdk.api.SplashAdListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.HashMap;
import java.util.Map;

public class AdGainSplashAdapter extends CustomSplashAdapter {

    final String TAG = AdGainInitManager.TAG;

    private String mAppId;
    private String mUnitId;

    private boolean isReady;

    private SplashAd splashAD;

    boolean isC2SBidding = false;

    @Override
    public boolean startBiddingRequest(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {

        Log.d(TAG, "\n splash startBiddingRequest   serverExtra = " + serverExtra + "   localExtra = " + localExtra + "   biddingListener = " + biddingListener);

        isC2SBidding = true;

        loadCustomNetworkAd(context, serverExtra, localExtra);

        return true;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra) {

        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");

        // 或者取  slot_id 对应topon后台广告位id
        // "networkUnit": "{\"app_id\":\"1105\",\"slot_id\":\"1194\",\"unit_id\":\"1194\"}",
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "unit_id");

        isReady = false;

        if (TextUtils.isEmpty(mAppId)) {
            notifyATLoadFail("", "AdGain app_id is empty.");
            return;
        }

        AdGainInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, serverExtra);
            }

            @Override
            public void onFail(String errorMsg) {
                notifyATLoadFail("", errorMsg);
            }
        });
    }

    private void startLoadAd(final Context context, Map<String, Object> serverExtra) {

        Map<String, Object> options = new HashMap<>(serverExtra);
        options.put("splash_test_option_key", "splash_test_option_value");

        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId(mUnitId)
//                .setWidth(PxUtils.getDeviceWidthInPixel(context))
//                .setHeight(PxUtils.getDeviceHeightInPixel(context) - PxUtils.dpToPx(context, 100))
                .setExtOption(options)
                .build();

        splashAD = new SplashAd(adRequest, new SplashAdListener() {
            @Override
            public void onAdLoadSuccess() {
                isReady = true;

                if (isC2SBidding) {

                    if (mBiddingListener != null) {

                        if (splashAD != null) {

                            AdGainBiddingNotice biddingNotice = new AdGainBiddingNotice(splashAD);

                            mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(splashAD.getBidPrice(), System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);

                        } else {
                            notifyATLoadFail("", "AdGain SplashAD had been destroy.");
                        }
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }
            }

            @Override
            public void onAdCacheSuccess() {

            }

            @Override
            public void onSplashAdLoadFail(AdError adError) {
                if (adError != null) {
                    notifyATLoadFail(adError.getErrorCode() + "", adError.getMessage());

                    if (mImpressionListener != null) {

                        Log.e(TAG, "GDT Splash show fail:[errorCode:" + adError.getErrorCode() + ",errorMsg:" + adError.getMessage() + "]");
                        mDismissType = ATAdConst.DISMISS_TYPE.SHOWFAILED;
                        mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "" + adError.getErrorCode(), adError.getMessage()));
                        mImpressionListener.onSplashAdDismiss();
                    }

                } else {
                    notifyATLoadFail("", "GDT Splash show fail");

                    if (mImpressionListener != null) {
                        mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "", "GDT Splash show fail"));
                        mImpressionListener.onSplashAdDismiss();
                    }
                }
            }

            @Override
            public void onSplashAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onSplashAdShowError(AdError error) {

            }

            @Override
            public void onSplashAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }

            @Override
            public void onSplashAdClose(boolean isSkip) {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }
        });

        splashAD.loadAd();
    }

    @Override
    public String getNetworkName() {
        return AdGainInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {

        if (splashAD != null) {
            return splashAD.isReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity, ViewGroup container) {

        if (container == null) {
            if (mImpressionListener != null) {
                mDismissType = ATAdConst.DISMISS_TYPE.SHOWFAILED;
                mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "", "Container is null"));
                mImpressionListener.onSplashAdDismiss();
            }
            return;
        }

        if (isReady && splashAD != null) {

            container.post(() -> {

                try {

                    if (splashAD != null) {
                        splashAD.showAd(container);
                    }

                } catch (Throwable t) {

                    if (mImpressionListener != null) {
                        mDismissType = ATAdConst.DISMISS_TYPE.SHOWFAILED;
                        mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "", "AdGain Splash show with exception"));
                        mImpressionListener.onSplashAdDismiss();
                    }
                }
            });
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdGainInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return AdGainInitManager.getInstance();
    }

    private static final String LOCAL_EXTRA_LOAD_TIMEOUT_MS = "load_timeout_ms";

    private long getLoadTimeParam(Map<String, Object> extra) {
        try {
            if (extra != null && extra.containsKey(LOCAL_EXTRA_LOAD_TIMEOUT_MS)) {
                Object obj = extra.get(LOCAL_EXTRA_LOAD_TIMEOUT_MS);
                if (obj instanceof Number) {
                    Number n = (Number) obj;
                    return n.intValue();
                }
            }
        } catch (Throwable tr) {
            Log.e(TAG, "getLoadTimeParam exception", tr);
        }
        return 8 * 1000;
    }

    @Override
    public void destory() {
        if (splashAD != null) {
            splashAD.destroyAd();
            splashAD = null;
        }
    }
}
