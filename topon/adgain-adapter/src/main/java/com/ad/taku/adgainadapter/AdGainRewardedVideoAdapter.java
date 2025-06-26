
package com.ad.taku.adgainadapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.RewardAd;
import com.adgain.sdk.api.RewardAdListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.HashMap;
import java.util.Map;

public class AdGainRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = AdGainInitManager.TAG;

    RewardAd mRewardVideoAD;

    String mAppId;
    String codeId;

    private int mVideoMuted = 0;

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
        codeId = ATInitMediation.getStringFromMap(serverExtra, "slot_id");
        Log.d("------loadAd", "map " + serverExtra);
        mVideoMuted = ATInitMediation.getIntFromMap(serverExtra, "video_muted", 0);

        if (TextUtils.isEmpty(mAppId)) {
            notifyATLoadFail("", "GT app_id is empty.");
            return;
        }

        AdGainInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
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

        Map<String, Object> options = new HashMap<>(serverExtra);
        options.put("reward_test_option_key", "reward_test_option_value");

        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId(codeId)
                .setBidFloor(AdGainInitManager.getBidFloor(serverExtra))
                .build();

        mRewardVideoAD = new RewardAd(adRequest, new RewardAdListener() {
            @Override
            public void onRewardAdLoadSuccess() {
                if (isC2SBidding) {

                    if (mBiddingListener != null) {
                        int ecpm = mRewardVideoAD.getBidPrice();

                        AdGainBiddingNotice biddingNotice = new AdGainBiddingNotice(mRewardVideoAD);

                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(ecpm, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }

                } else if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onRewardAdLoadCached() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onRewardAdShow() {
            }

            @Override
            public void onRewardAdPlayStart() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onRewardAdPlayEnd() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onRewardAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onRewardAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewardAdLoadError(AdError adError) {
                notifyATLoadFail(adError.getErrorCode() + "", adError.getMessage());

            }

            @Override
            public void onRewardAdShowError(AdError error) {

            }

            @Override
            public void onRewardVerify() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onAdSkip() {

            }

        });

        mRewardVideoAD.loadAd();
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
        return new HashMap<>();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return AdGainInitManager.getInstance();
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
