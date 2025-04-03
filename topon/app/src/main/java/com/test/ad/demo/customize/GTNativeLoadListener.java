

package com.test.ad.demo.customize;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;

public interface GTNativeLoadListener {
    void notifyLoaded(CustomNativeAd... customNativeAds);

    void notifyError(String errorCode, String errorMsg);
}
