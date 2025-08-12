package com.ad.taku.adgainadapter;

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
        Map<String, Object> map = new HashMap<>();
        map.put(IBidding.EXPECT_COST_PRICE, costPrice);
        if (secondPrice != costPrice) // 如果只有AdGain 有填充，两个值一样，收集没有意义，tobid 是 二价为0
            map.put(IBidding.HIGHEST_LOSS_PRICE, (int) Math.round(secondPrice));
        map.put(IBidding.THIRD_MEDIATION, "taku");
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
        map.put(IBidding.WIN_PRICE, winPrice);
        map.put(IBidding.THIRD_MEDIATION, "taku");
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
