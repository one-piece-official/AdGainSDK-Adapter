
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
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.gt.sdk.api.AdError;
import com.gt.sdk.api.AdRequest;
import com.gt.sdk.api.GTAdInfo;
import com.gt.sdk.api.RewardAd;
import com.gt.sdk.api.RewardAdListener;

import java.util.HashMap;
import java.util.Map;

public class GTRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = GTInitManager.TAG;

    RewardAd mRewardVideoAD;

    String mAppId;
    String mUnitId;

    private int mVideoMuted = 0;

    private Map<String, Object> extraMap;

    private boolean isC2SBidding = false;

    @Override
    public boolean startBiddingRequest(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {

        Log.d(TAG, "\n reward startBiddingRequest   serverExtra = " + serverExtra + "   localExtra = " + localExtra + "   biddingListener = " + biddingListener);

        isC2SBidding = true;

        loadCustomNetworkAd(context, serverExtra, localExtra);

        return true;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "unit_id");

        mVideoMuted = ATInitMediation.getIntFromMap(serverExtra, "video_muted", 0);

        if (TextUtils.isEmpty(mAppId)) {
            notifyATLoadFail("", "GT app_id is empty.");
            return;
        }

        GTInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                loadGTRewardVideo(context, serverExtra);
            }

            @Override
            public void onFail(String errorMsg) {
                notifyATLoadFail("", errorMsg);
            }
        });
    }

    private void loadGTRewardVideo(Context context, Map<String, Object> serverExtra) {

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", "");

        AdRequest adRequest = new AdRequest.Builder()
                .setAdUnitID(mUnitId)  // 1197
                .setExtOption(options)
                .build();

        mRewardVideoAD = new RewardAd(adRequest, new RewardAdListener() {
            @Override
            public void onRewardAdLoadSuccess(String s, GTAdInfo gtAdUnit) {
                if (isC2SBidding) {

                    if (mBiddingListener != null) {
                        int ecpm = mRewardVideoAD.getBidPrice();

                        GTBiddingNotice biddingNotice = new GTBiddingNotice(mRewardVideoAD);

                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(ecpm, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }

                } else if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onRewardAdLoadCached(String s, GTAdInfo gtAdInfo) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onRewardAdShow(String s, GTAdInfo gtAdUnit) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onRewardAdPlayStart(String s, GTAdInfo gtAdUnit) {

            }

            @Override
            public void onRewardAdPLayEnd(String s, GTAdInfo gtAdUnit) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onRewardAdClick(String s, GTAdInfo gtAdUnit) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onRewardAdClosed(String s, GTAdInfo gtAdUnit) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewardAdLoadError(String s, AdError adError) {
                notifyATLoadFail(adError.getErrorCode() + "", adError.getMessage());
            }

            @Override
            public void onRewardAdShowError(String s, AdError adError) {

            }

            @Override
            public void onReward(String s, GTAdInfo gtAdUnit) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }

            }
        });

        mRewardVideoAD.loadAd();
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
    public boolean isAdReady() {
        if (mRewardVideoAD != null) {
            return mRewardVideoAD.isReady();
        }

        return false;
    }

    @Override
    public void show(Activity activity) {

        if (mRewardVideoAD == null) {
            return;
        }

        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            mRewardVideoAD.showAd(activity);

        } else {
            mRewardVideoAD.showAd();
        }
    }

    @Override
    public Map<String, Object> getNetworkInfoMap() {
        return extraMap;
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return GTInitManager.getInstance();
    }

    @Override
    public void destory() {
        if (mRewardVideoAD != null) {
            mRewardVideoAD.setRewardAdListener(null);
            mRewardVideoAD.destroyAd();
            mRewardVideoAD = null;
        }
    }

}
