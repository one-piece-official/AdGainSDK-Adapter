package com.test.ad.demo.customize;


import android.util.Log;

import com.adgain.sdk.api.IBidding;
import com.adgain.sdk.api.InterstitialAd;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.adgain.sdk.api.RewardAd;
import com.adgain.sdk.api.SplashAd;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingNotice;
import com.anythink.core.api.ATInitMediation;

import java.util.HashMap;
import java.util.Map;

public class AdGainBiddingNotice implements ATBiddingNotice {

    private static final String TAG = AdGainInitManager.TAG;

    IBidding gtBaseAd;

    protected AdGainBiddingNotice(IBidding adObject) {
        this.gtBaseAd = adObject;
    }

    // costPrice：竞胜价格
    // secondPrice: 第一位 竞败 的价格, 即 竞胜方后一位的价格（二价）   单位分
    @Override
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
    @Override
    public void notifyBidLoss(String lossCode, double winPrice, Map<String, Object> extra) {

        Log.d(TAG, "\n\n  notifyBidLoss adType = " + getAdType() + "     lossCode = " + lossCode + "   winPrice = " + winPrice + "  extra = " + extra);

        Map<String, Object> map = new HashMap<>(4);

        String gdtLossReason = "";

        switch (lossCode) {
            case ATAdConst.BIDDING_TYPE.BIDDING_LOSS_WITH_BIDDING_TIMEOUT:
                gdtLossReason = "timeout";
                break;

            case ATAdConst.BIDDING_TYPE.BIDDING_LOSS_WITH_LOW_PRICE_IN_HB:
                gdtLossReason = "low_price_hb";
                break;

            case ATAdConst.BIDDING_TYPE.BIDDING_LOSS_WITH_LOW_PRICE_IN_NORMAL:
                gdtLossReason = "low_price_normal";
                break;

            case ATAdConst.BIDDING_TYPE.BIDDING_LOSS_WITH_EXPIRE:
                gdtLossReason = "expire";
                break;

            case ATAdConst.BIDDING_TYPE.BIDDING_LOSS_WITH_LOW_FLOOR:
                gdtLossReason = "low_floor";
                break;
        }


        int adnId = ATInitMediation.getIntFromMap(extra, ATBiddingNotice.ADN_ID, -1);

        try {
            switch (adnId) {
                case ATAdConst.BIDDING_ADN_ID.LOSE_TO_NORMAL_IN_SAME_ADN:
                    break;
                case ATAdConst.BIDDING_ADN_ID.LOSE_TO_HB_IN_SAME_ADN:
                    break;
                case ATAdConst.BIDDING_ADN_ID.LOSE_TO_OWN_ADN:
                    break;
                case ATAdConst.BIDDING_ADN_ID.LOSE_TO_OTHER_ADN:
                    break;
            }

        } catch (Throwable ignored) {
        }

        map.put(IBidding.WIN_PRICE, winPrice);
        map.put(IBidding.LOSS_REASON, gdtLossReason);
        map.put(IBidding.ADN_ID, adnId);

        if (gtBaseAd != null) {
            gtBaseAd.sendLossNotification(map);
        }
    }

    // isWinner：是否为竞胜方
    // displayPrice：正在曝光的广告的价格
    @Override
    public void notifyBidDisplay(boolean isWinner, double displayPrice) {

        Log.d(TAG, "\n\n notifyBidDisplay   adType = " + getAdType() + "    isWinner = " + isWinner + "   displayPrice = " + displayPrice);
    }

    @Override
    public ATAdConst.CURRENCY getNoticePriceCurrency() {
        return ATAdConst.CURRENCY.RMB_CENT;
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
