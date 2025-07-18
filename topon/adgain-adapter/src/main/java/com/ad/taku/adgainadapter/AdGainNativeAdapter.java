package com.ad.taku.adgainadapter;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdLoadListener;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdGainNativeAdapter extends CustomNativeAdapter {

    private static final String TAG = AdGainInitManager.TAG;

    NativeUnifiedAd nativeAd;

    String mAppId;
    String codeId;

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
        codeId = ATInitMediation.getStringFromMap(serverExtra, "slot_id");
        Log.d(TAG, "----native initRequestParams    " + mAppId + "   codeId = " + codeId +" " + serverExtra);

        if (TextUtils.isEmpty(codeId)) {
            codeId = ATInitMediation.getStringFromMap(serverExtra, "unit_id");
        }

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

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        try {
            // adgain load 阶段不区分是自渲染 还是 模板，是根据返回区分的
            loadRenderingAd(context.getApplicationContext(), serverExtra);
        } catch (Throwable e) {
            notifyATLoadFail("", e.getMessage());
        }
    }

    private void loadRenderingAd(final Context context, Map<String, Object> serverExtra) {

        Map<String, Object> options = new HashMap<>(serverExtra);
        options.put("native_test_option_key", "native_test_option_value");

        AdRequest adRequest = new AdRequest
                .Builder()
                .setCodeId(codeId)
                .setBidFloor(AdGainInitManager.getBidFloor(serverExtra))
                .build();

        nativeAd = new NativeUnifiedAd(adRequest, new NativeAdLoadListener() {

            @Override
            public void onAdError(AdError error) {
                notifyATLoadFail(error.getErrorCode() + "", error.getMessage());
            }

            @Override
            public void onAdLoad(List<NativeAdData> list) {
                if (list != null && !list.isEmpty()) {

                    if (isC2SBidding) {

                        NativeAdData unifiedADData = list.get(0);

                        if (unifiedADData != null && mBiddingListener != null) {
                            double price = unifiedADData.getPrice();

                            AdGainNativeAd gdtNativeAd = new AdGainNativeAd(context, unifiedADData, mVideoMuted, mVideoAutoPlay, mVideoDuration);

                            AdGainBiddingNotice notice = new AdGainBiddingNotice(nativeAd);

                            Log.d(TAG, "onAdLoad: onC2SBiddingResultWithCache price = " + price);

                            mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", notice, ATAdConst.CURRENCY.RMB_CENT), gdtNativeAd);
                        }

                        return;
                    }

                    List<CustomNativeAd> resultList = new ArrayList<>();

                    for (NativeAdData unifiedADData : list) {
                        AdGainNativeAd gdtNativeAd = new AdGainNativeAd(context, unifiedADData, mVideoMuted, mVideoAutoPlay, mVideoDuration);
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

        });

        nativeAd.loadAd();
    }

    @Override
    public String getNetworkName() {
        return AdGainInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return codeId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdGainInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return AdGainInitManager.getInstance();
    }

    @Override
    public void destory() {
        if (nativeAd != null) {
            nativeAd.destroyAd();
        }
    }

}
