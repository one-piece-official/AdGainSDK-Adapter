package com.tobid.adapter.adgain;

import android.util.Log;

import com.adgain.sdk.api.IBidding;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.base.WMBidUtil;
import com.windmill.sdk.custom.WMAdBaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class AdGainAdapterUtil {
    // referBidInfo: https://doc.sigmob.com/ToBid%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/SDK%E9%9B%86%E6%88%90%E8%AF%B4%E6%98%8E/Android/%E9%AB%98%E7%BA%A7%E8%AE%BE%E7%BD%AE/%E8%87%AA%E5%AE%9A%E4%B9%89%E5%B9%BF%E5%91%8A%E7%BD%91%E7%BB%9C/#_5-2-1-referbidinfo-%E5%AD%97%E6%AE%B5%E5%AE%9A%E4%B9%89%E8%AF%B4%E6%98%8E

    public static Map<String, Object> getBidingWinNoticeParam(String price, Map<String, Object> referBidInfo) {
        Map<String, Object> map = new HashMap<>();
        try {
            Log.d("-----AdGainAdapterUtil", "---win " + price + " map: " + referBidInfo);
            if (referBidInfo != null) { // //13 头条  16 GDT    19 快手  21 百度  22--Gromore
                Object winnerEcpm = referBidInfo.get(WMBidUtil.WINNER_ECPM);
                if (winnerEcpm != null && Double.parseDouble(winnerEcpm.toString()) > 0) {
                    map.put(IBidding.EXPECT_COST_PRICE, String.valueOf(winnerEcpm));
                } else if (price != null) {
                    map.put(IBidding.EXPECT_COST_PRICE, price);
                } else {
                    map.putAll(referBidInfo);
                }
                Object biddingEcpm = referBidInfo.get("bidding_ecpm");
                Object waterfallEcpm = referBidInfo.get("waterfall_ecpm");
                if (waterfallEcpm != null && Double.parseDouble(waterfallEcpm.toString()) > 0 && biddingEcpm != null && Double.parseDouble(biddingEcpm.toString()) > 0) {
                    if (Double.parseDouble(waterfallEcpm.toString()) > Double.parseDouble(biddingEcpm.toString())) {
                        map.put(IBidding.HIGHEST_LOSS_PRICE, waterfallEcpm);
                    } else {
                        map.put(IBidding.HIGHEST_LOSS_PRICE, biddingEcpm);
                    }
                } else if (biddingEcpm != null && Double.parseDouble(biddingEcpm.toString()) > 0) {
                    map.put(IBidding.HIGHEST_LOSS_PRICE, biddingEcpm);
                } else if (waterfallEcpm != null && Double.parseDouble(waterfallEcpm.toString()) > 0) {
                    map.put(IBidding.HIGHEST_LOSS_PRICE, waterfallEcpm);
                }
            }
        } catch (Exception e) {
        }
        map.put(IBidding.THIRD_MEDIATION, "tobid");
        return map;
    }

    public static Map<String, Object> getBidingLossNoticeParam(String price, Map<String, Object> referBidInfo) {
        Map<String, Object> map = new HashMap<>();
        try {
            if (referBidInfo != null) {
                Object winnerEcpm = referBidInfo.get(WMBidUtil.WINNER_ECPM);
                Object biddingEcpm = referBidInfo.get("bidding_ecpm");
                if (winnerEcpm != null && Double.parseDouble(winnerEcpm.toString()) > 0) {
                    map.put(IBidding.WIN_PRICE, winnerEcpm);
                } else if (biddingEcpm != null && Double.parseDouble(biddingEcpm.toString()) > 0) {
                    map.put(IBidding.WIN_PRICE, biddingEcpm);
                } else if (price != null && Double.parseDouble(price.toString()) > 0) {
                    map.put(IBidding.WIN_PRICE, price);
                } else {
                    map.putAll(referBidInfo);
                }
                Object winnerChannel = referBidInfo.get(WMBidUtil.WINNER_CHANNEL);
                if (winnerChannel != null) { //13 头条  16 GDT    19 快手  21 百度  22--Gromore
                    map.put(IBidding.ADN_ID, String.valueOf(winnerChannel));
                }
            }
        } catch (Exception e) {
        }
        map.put(IBidding.THIRD_MEDIATION, "tobid");
        return map;
    }

    public static int getBidFloor(WMAdBaseAdapter adapter, Map<String, Object> serverExtra) {
        int floor = 0;

        try {
            return adapter.getBidFloor();

        } catch (Throwable ignore) {

            try {
                Object bidFloor = serverExtra.get(WMConstants.BID_FLOOR);
                if (bidFloor != null) {
                    floor = (Integer) bidFloor;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return floor;
    }
}
