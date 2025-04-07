package com.test.ad.demo.customize;

import android.content.Context;

import com.gt.sdk.api.NativeAdData;

public class GTNativePatchAd extends GTNativeAd {

    protected GTNativePatchAd(Context context, NativeAdData gdtAd, boolean videoMuted, int videoAutoPlay, int videoDuration) {
        super(context, gdtAd, videoMuted, videoAutoPlay, videoDuration);
    }

    @Override
    public int getNativeType() {
        return NativeType.PATCH;
    }
}