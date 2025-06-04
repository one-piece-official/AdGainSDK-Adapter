package com.tobid.adapter.adgain;

import com.adgain.sdk.api.IBidding;
import com.windmill.sdk.base.WMBidUtil;

import java.util.HashMap;
import java.util.Map;

public class AdGainAdapterUtil {
    // referBidInfo: https://doc.sigmob.com/ToBid%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/SDK%E9%9B%86%E6%88%90%E8%AF%B4%E6%98%8E/Android/%E9%AB%98%E7%BA%A7%E8%AE%BE%E7%BD%AE/%E8%87%AA%E5%AE%9A%E4%B9%89%E5%B9%BF%E5%91%8A%E7%BD%91%E7%BB%9C/#_5-2-1-referbidinfo-%E5%AD%97%E6%AE%B5%E5%AE%9A%E4%B9%89%E8%AF%B4%E6%98%8E

    public static Map<String, Object> getBidingWinNoticeParam(String price, Map<String, Object> referBidInfo) {
        Map<String, Object> map = new HashMap<>();
        if (referBidInfo != null) {
            map.putAll(referBidInfo);
            Object winnerEcpm = referBidInfo.get(WMBidUtil.WINNER_ECPM);
            if (winnerEcpm != null) {
                map.put(IBidding.EXPECT_COST_PRICE, String.valueOf(winnerEcpm));
            }
            Object biddingEcpm = referBidInfo.get("bidding_ecpm");
            if (biddingEcpm != null) {
                map.put(IBidding.HIGHEST_LOSS_PRICE, String.valueOf(biddingEcpm));
            }
        }
        if (!map.containsKey(IBidding.EXPECT_COST_PRICE) && price != null) {
            map.put(IBidding.EXPECT_COST_PRICE, price);
        }
        return map;
    }

    public static Map<String, Object> getBidingLossNoticeParam(String price, Map<String, Object> referBidInfo) {
        Map<String, Object> map = new HashMap<>();
        if (referBidInfo != null) {
            map.putAll(referBidInfo);
            Object winnerEcpm = referBidInfo.get(WMBidUtil.WINNER_ECPM);
            if (winnerEcpm != null) {
                map.put(IBidding.WIN_PRICE, String.valueOf(winnerEcpm));
            }
            Object winnerChannel = referBidInfo.get(WMBidUtil.WINNER_CHANNEL);
            if (winnerChannel != null) {
                map.put(IBidding.ADN_ID, String.valueOf(winnerChannel));
            }
        }
        if (!map.containsKey(IBidding.WIN_PRICE) && price != null) {
            map.put(IBidding.WIN_PRICE, price);
        }
        return map;
    }
}
