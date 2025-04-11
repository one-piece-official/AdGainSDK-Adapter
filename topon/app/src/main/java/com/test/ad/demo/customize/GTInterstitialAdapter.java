

package com.test.ad.demo.customize;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.GTAdInfo;
import com.gt.sdk.api.InterstitialAd;
import com.gt.sdk.api.InterstitialAdListener;

import java.util.HashMap;
import java.util.Map;


public class GTInterstitialAdapter extends CustomInterstitialAdapter {
    public static String TAG = GTInitManager.TAG;

    InterstitialAd mGTInterstitialAd;

    String mAppId;
    String mADUnitId;

    boolean isC2SBidding;

    // 参考 ： https://help.takuad.com/docs/qJT7q0
    // serverExtra : Taku后台配置的 Json字符串 中的 key-value
    // localExtra  :  ATInterstitial#setLocalExtra()  本次加载传入自定义参数
    @Override
    public boolean startBiddingRequest(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {
        Log.d(TAG, "\n inter startBiddingRequest   serverExtra = " + serverExtra + "   localExtra = " + localExtra + "   biddingListener = " + biddingListener);

        isC2SBidding = true;

        loadCustomNetworkAd(context, serverExtra, localExtra);

        return true;
    }

    //loadCustomNetworkAd: serverExtra = {ad_format=3, ad_type=-1, gdpr_consent=true, app_coppa_switch=false, gm_currency=RMB, forbid_gm_refresh=true, anythink_stk_info=com.anythink.core.common.f.a(Unknown Source:321),com.anythink.core.common.f.b(Unknown Source:1428),com.anythink.interstitial.a.a.a(Unknown Source:233),com.anythink.interstitial.api.ATInterstitial.load(Unknown Source:250),com.anythink.interstitial.api.ATInterstitial.load(Unknown Source:245),com.anythink.interstitial.api.ATInterstitial.load(Unknown Source:237),com.anythink.interstitial.api.ATInterstitial.load(Unknown Source:233),com.test.ad.demo.InterstitialAdActivity.loadAd(InterstitialAdActivity.java:266),com.test.ad.demo.InterstitialAdActivity.onClick(InterstitialAdActivity.java:317),android.view.View.performClick(View.java:7642),android.view.View.performClickInternal(View.java:7619),android.view.View.-$$Nest$mperformClickInternal(Unknown Source:0),android.view.View$PerformClick.run(View.java:29908),android.os.Handler.handleCallback(Handler.java:942),android.os.Handler.dispatchMessage(Handler.java:99),android.os.Looper.loopOnce(Looper.java:223),android.os.Looper.loop(Looper.java:324),android.app.ActivityThread.main(ActivityThread.java:8599),java.lang.reflect.Method.invoke(Native Method),com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:582),com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1061), sys_sp=2, app_ccpa_switch=false, anythink_mediation_wf_id=b67eb584dc7d2e_662720_0_e7ba7e002dda68cb9d6a7b2f9974714a, test_key=test_value, ad_s_reqf_mode=1, app_id=1105}   localExtra = {}
    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra) {
        Log.d(TAG, "loadCustomNetworkAd: serverExtra = " + serverExtra + "   localExtra = " + localExtra);

        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mADUnitId = ATInitMediation.getStringFromMap(serverExtra, "unit_id");
        Log.d(TAG, "loadCustomNetworkAd: mAppId = " + mAppId + "  mADUnitId = " + mADUnitId + "   isC2SBidding =  " + isC2SBidding);

        if (TextUtils.isEmpty(mAppId)) {
            notifyATLoadFail("", "GT app_id is empty.");
            return;
        }

        GTInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                loadInterstitial(serverExtra, localExtra);
            }

            @Override
            public void onFail(String errorMsg) {
                notifyATLoadFail("", errorMsg);
            }
        });
    }

    private void loadInterstitial(Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        Map<String, Object> options = new HashMap<>(serverExtra);
        options.put("inter_test_option_key", "inter_test_option_value");

        AdRequest adRequest = new AdRequest.Builder()
                //.setAdUnitID("1195")
                .setAdUnitID(mADUnitId)
                .setExtOption(options)
                .build();

        mGTInterstitialAd = new InterstitialAd(adRequest, new InterstitialAdListener() {

            @Override
            public void onInterstitialAdLoadSuccess(String s, GTAdInfo gtAdUnit) {

                if (isC2SBidding) {

                    if (mBiddingListener != null) {
                        int ecpm = mGTInterstitialAd.getBidPrice();

                        GTBiddingNotice biddingNotice = new GTBiddingNotice(mGTInterstitialAd);

                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(ecpm, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }

                } else if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onInterstitialAdLoadCached(String s, GTAdInfo gtAdInfo) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onInterstitialAdLoadError(String s, AdError adError) {
                notifyATLoadFail(String.valueOf(adError.getErrorCode()), adError.getMessage());

            }


            @Override
            public void onInterstitialAdShow(String s, GTAdInfo gtAdUnit) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onInterstitialAdPlayStart(String s, GTAdInfo gtAdUnit) {

                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }

            }

            @Override
            public void onInterstitialAdPLayEnd(String s, GTAdInfo gtAdUnit) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onInterstitialAdClick(String s, GTAdInfo gtAdUnit) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onInterstitialAdClosed(String s, GTAdInfo gtAdUnit) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onInterstitialAdShowError(String s, AdError adError) {

                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoError(adError.getErrorCode() + "", adError.getMessage());
                }

            }
        });

        mGTInterstitialAd.loadAd();
    }

    @Override
    public boolean isAdReady() {
        if (mGTInterstitialAd != null) {
            return mGTInterstitialAd.isReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {

        if (mGTInterstitialAd == null) {
            return;
        }

        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            mGTInterstitialAd.showAd(activity);

        } else {
            mGTInterstitialAd.showAd();
        }
    }

    @Override
    public String getNetworkName() {
        return GTInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mADUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return GTInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return GTInitManager.getInstance();
    }

    @Override
    public void destory() {
        if (mGTInterstitialAd != null) {
            mGTInterstitialAd.setInterstitialAdListener(null);
            mGTInterstitialAd.destroyAd();
            mGTInterstitialAd = null;
        }
    }

}
