package com.union_test.toutiao.adgain;


import android.util.Log;

import com.adgain.sdk.api.IBidding;
import com.adgain.sdk.api.InterstitialAd;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.adgain.sdk.api.RewardAd;
import com.adgain.sdk.api.SplashAd;

import java.util.HashMap;
import java.util.Map;

public class AdGainBiddingNotice {

    private static final String TAG = AdGainCustomerInit.TAG;

    IBidding gtBaseAd;

    public static void notifyADN(IBidding ad, boolean win, double winnerPrice, int loseReason, Map<String, Object> extra) {

        try {
            AdGainBiddingNotice biddingNotice = new AdGainBiddingNotice(ad);
            if (win) {
                biddingNotice.notifyBidWin(winnerPrice, 1, extra);
            } else {
                biddingNotice.notifyBidLoss(loseReason + "", winnerPrice, extra);
            }
        } catch (Throwable e) {
            Log.e(TAG, "notifyADN: ", e);
        }

    }

    protected AdGainBiddingNotice(IBidding adObject) {
        this.gtBaseAd = adObject;
    }

    // costPrice：竞胜价格
    // secondPrice: 第一位 竞败 的价格, 即 竞胜方后一位的价格（二价）   单位分
    public void notifyBidWin(double costPrice, double secondPrice, Map<String, Object> extra) {

        Log.d(TAG, "\n\n notifyBidWin   adType = " + getAdType() + "    costPrice = " + costPrice + "   secondPrice = " + secondPrice + "  extra = " + extra);

        Map<String, Object> map = new HashMap<>(4);
        map.put(IBidding.EXPECT_COST_PRICE, costPrice);
        map.put(IBidding.HIGHEST_LOSS_PRICE, (int) Math.round(secondPrice));

        if (gtBaseAd != null) {
            gtBaseAd.sendWinNotification(map);
        }
    }

    // lossCode：竞败码  失败原因，参考 ATAdConst.BIDDING_TYPE 类
    // extra参数：可通过Key: ATBiddingNotice.ADN_ID，从extra中获取竞胜方渠道，竞胜方渠道的枚举值，参考ATAdConst.BIDDING_ADN_ID 类
    public void notifyBidLoss(String lossCode, double winPrice, Map<String, Object> extra) {

        Log.d(TAG, "\n\n  notifyBidLoss adType = " + getAdType() + "     lossCode = " + lossCode + "   winPrice = " + winPrice + "  extra = " + extra);

        Map<String, Object> map = new HashMap<>(4);

        map.put(IBidding.WIN_PRICE, winPrice);
        map.put(IBidding.LOSS_REASON, lossCode);
        map.put(IBidding.ADN_ID, "");

        if (gtBaseAd != null) {
            gtBaseAd.sendLossNotification(map);
        }
    }

    private String getAdType() {
        if (gtBaseAd instanceof RewardAd) {
            return "reward";
        }

        if (gtBaseAd instanceof InterstitialAd) {
            return "inter";
        }

        if (gtBaseAd instanceof SplashAd) {
            return "splash";
        }

        if (gtBaseAd instanceof NativeUnifiedAd) {
            return "native";
        }

        return gtBaseAd != null ? gtBaseAd.toString() : "";
    }

}
