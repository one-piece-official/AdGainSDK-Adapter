package com.gromore.adapter.adgain;

import android.util.Log;

import com.adgain.sdk.api.IBidding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GMBiddingUtil {

    private static final CopyOnWriteArrayList<NotifyBiddingListener> listeners = new CopyOnWriteArrayList<>();

    protected interface NotifyBiddingListener {
        void notifyBiddingResult(Object object);
    }

    // 开发者调用的方法
    public static void gmNotifyLoss(Object object) {
        try {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).notifyBiddingResult(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("GMBiddingUtil", "notifyBiddingResult: Exception " + e.getMessage());
        }
    }

    protected static void addNotifyBiddingListener(NotifyBiddingListener listener) {
        if (listeners != null) listeners.add(listener);
    }

    protected static void removeNotifyBiddingListener(NotifyBiddingListener listener) {
        if (listeners != null) listeners.remove(listener);
    }

    protected static void clearNotifyBiddingListener() {
        if (listeners != null) listeners.clear();
    }

    // 插屏激励Adapter 调用的方法
    protected static void adgainNotifyLoss(IBidding gtBaseAd, String winPrice, NotifyBiddingListener listener) {
        Map<String, Object> map = new HashMap<>();
        Log.d("GMBiddingUtil", "adgainNotifyLoss: winPrice " + winPrice + " " + gtBaseAd);
        map.put(IBidding.WIN_PRICE, winPrice);
        if (gtBaseAd != null) {
            gtBaseAd.sendLossNotification(map);
        }
        GMBiddingUtil.removeNotifyBiddingListener(listener);
    }
}
