package com.test.ad.demo.customize;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.NativeAdData;
import com.gt.sdk.api.NativeAdLoadListener;
import com.gt.sdk.api.NativeUnifiedAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GTNativeAdapter extends CustomNativeAdapter {

    private static final String TAG = GTInitManager.TAG;

    NativeUnifiedAd nativeAd;

    String mAppId;
    String mUnitId;

    int mUnitType;

    boolean mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;

    boolean isC2SBidding = false;

    @Override
    public boolean startBiddingRequest(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {
        Log.d(TAG, "\n native startBiddingRequest   serverExtra = " + serverExtra + "   localExtra = " + localExtra + "   biddingListener = " + biddingListener);

        isC2SBidding = true;

        loadCustomNetworkAd(context, serverExtra, localExtra);

        return true;
    }

    void initRequestParams(Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "unit_id");
        mUnitType = ATInitMediation.getIntFromMap(serverExtra, "unit_type");

        mVideoMuted = ATInitMediation.getIntFromMap(serverExtra, "video_muted", 0) == 1;
        mVideoAutoPlay = ATInitMediation.getIntFromMap(serverExtra, "video_autoplay", 1);
        mVideoDuration = ATInitMediation.getIntFromMap(serverExtra, "video_duration", -1);
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        initRequestParams(serverExtra, localExtra);

        if (TextUtils.isEmpty(mAppId)) {
            notifyATLoadFail("", "GTD appid is empty...  GTNativeAdapter.class");
            return;
        }

        GTInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
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

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        try {
            switch (mUnitType) {

                case 0:   // self rendering
                    loadSelfRenderingAd(context.getApplicationContext(),serverExtra);
                    break;

                case 1:   //Native Express
                default:
                    defaultLoad(context);
                    break;
            }
        } catch (Throwable e) {
            notifyATLoadFail("", e.getMessage());
        }
    }

    private void loadSelfRenderingAd(final Context context,Map<String, Object> serverExtra) {

        Map<String, Object> options = new HashMap<>(serverExtra);
        options.put("native_test_option_key", "native_test_option_value");

        AdRequest adRequest = new AdRequest
                .Builder()
                .setAdUnitID(mUnitId)
                .setExtOption(options)
                .build();

        nativeAd = new NativeUnifiedAd(adRequest, new NativeAdLoadListener() {

            @Override
            public void onAdLoad(String codeId, List<NativeAdData> list) {

                if (list != null && !list.isEmpty()) {

                    if (isC2SBidding) {

                        NativeAdData unifiedADData = list.get(0);

                        if (unifiedADData != null && mBiddingListener != null) {
                            double price = unifiedADData.getPrice();

                            GTNativeAd gdtNativeAd = new GTNativeAd(context, unifiedADData, mVideoMuted, mVideoAutoPlay, mVideoDuration);

                            GTBiddingNotice notice = new GTBiddingNotice(nativeAd);

                            Log.d(TAG, "onAdLoad: onC2SBiddingResultWithCache price = " + price);

                            mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", notice, ATAdConst.CURRENCY.RMB_CENT), gdtNativeAd);
                        }

                        return;
                    }

                    List<CustomNativeAd> resultList = new ArrayList<>();

                    for (NativeAdData unifiedADData : list) {
                        GTNativeAd gdtNativeAd = new GTNativeAd(context, unifiedADData, mVideoMuted, mVideoAutoPlay, mVideoDuration);
                        resultList.add(gdtNativeAd);
                    }

                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);

                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(customNativeAds);
                    }

                } else {
                    notifyATLoadFail("", "Ad list is empty");
                }
            }

            @Override
            public void onAdError(String codeId, AdError error) {
                notifyATLoadFail(error.getErrorCode() + "", error.getMessage());
            }

        });

        nativeAd.loadAd();
    }

    private void defaultLoad(Context context) {
       /* GDTATNativeLoadListener loadListener = new GDTATNativeLoadListener() {

            @Override
            public void notifyLoaded(CustomNativeAd... customNativeAds) {
                if (isC2SBidding && customNativeAds[0] instanceof GDTATNativeExpressAd) {
                    GDTATNativeExpressAd gdtatNativeExpressAd = (GDTATNativeExpressAd) customNativeAds[0];
                    if (mBiddingListener != null) {
                        int ecpm = gdtatNativeExpressAd.mNativeExpressADView.getECPM();
                        double price = ecpm;
                        GDTATBiddingNotice gdtatBiddingNotice = new GDTATBiddingNotice(gdtatNativeExpressAd);
                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", gdtatBiddingNotice, ATAdConst.CURRENCY.RMB_CENT), gdtatNativeExpressAd);
                    }
                    return;
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(customNativeAds);
                }
            }

            @Override
            public void notifyError(String errorCode, String errorMsg) {
                notifyATLoadFail(errorCode, errorMsg);
            }
        };

        if (mUnitType == 3) {//Patch, template
            GDTATNativeExpressPatchAd gdtatNativeExpressPatchAd = new GDTATNativeExpressPatchAd(context, mUnitId, mAdWidth, mAdHeight, mVideoMuted, mVideoAutoPlay, mVideoDuration, mPayload);
            gdtatNativeExpressPatchAd.loadAD(loadListener, GDTATInitManager.getInstance().getLoadAdParams());
        } else {
            //Picture + video template
            GDTATNativeExpressAd gdtatNativeExpressAd = new GDTATNativeExpressAd(context, mUnitId, mAdWidth, mAdHeight, mVideoMuted, mVideoAutoPlay, mVideoDuration, mPayload);
            gdtatNativeExpressAd.loadAD(loadListener, GDTATInitManager.getInstance().getLoadAdParams());
        }*/
    }


    @Override
    public String getNetworkName() {
        return GTInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
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
        if (nativeAd != null) {
            nativeAd.destroyAd();
        }
    }

}
