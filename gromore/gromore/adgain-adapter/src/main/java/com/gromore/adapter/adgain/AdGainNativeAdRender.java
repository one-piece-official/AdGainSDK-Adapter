package com.gromore.adapter.adgain;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adgain.sdk.api.AdAppInfo;
import com.adgain.sdk.api.AdGainImage;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdEventListener;
import com.adgain.sdk.api.NativeAdInteractiveType;
import com.adgain.sdk.api.NativeAdPatternType;
import com.adgain.sdk.api.NativeUnifiedAd;
import com.adgain.sdk.base.natives.AdgainNativeAdMediaView;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationNativeAdAppInfo;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationViewBinder;
import com.bytedance.sdk.openadsdk.mediation.bridge.custom.native_ad.MediationCustomNativeAd;

import java.util.ArrayList;
import java.util.List;

public class AdGainNativeAdRender extends MediationCustomNativeAd {

    private static final String TAG = AdGainCustomerInit.TAG;

    private NativeUnifiedAd mNativeAD;

    private NativeAdData mNativeUnifiedADData;

    private Context mContext;

    public AdGainNativeAdRender(Context context, NativeAdData feedAd, NativeUnifiedAd nativeAd) {
        try {

            mContext = context;
            mNativeAD = nativeAd;
            mNativeUnifiedADData = feedAd;

            AdAppInfo info = mNativeUnifiedADData.getAdAppInfo();

            MediationNativeAdAppInfo nativeAdAppInfo = new MediationNativeAdAppInfo();
            if (info != null) {
                nativeAdAppInfo.setAppName(info.getAppName());
                nativeAdAppInfo.setAuthorName(info.getAuthorName());
                nativeAdAppInfo.setPackageSizeBytes(info.getAppSize());
                nativeAdAppInfo.setPermissionsUrl(info.getPermissionsUrl());
                nativeAdAppInfo.setPrivacyAgreement(info.getPrivacyUrl());
                nativeAdAppInfo.setVersionName(info.getVersionName());
            }

            setNativeAdAppInfo(nativeAdAppInfo);

            setTitle(mNativeUnifiedADData.getTitle());
            setDescription(mNativeUnifiedADData.getDesc());
            setActionText(mNativeUnifiedADData.getCTAText());
            setIconUrl(mNativeUnifiedADData.getIconUrl());

            List<AdGainImage> list = mNativeUnifiedADData.getImageList();
            if (list != null && !list.isEmpty()) {
                AdGainImage image = list.get(0);
                Log.d(TAG, "setAdData main image =  " + image.toString());
                setImageUrl(image.getImageUrl());
                setImageWidth(image.getWidth());
                setImageHeight(image.getHeight());
            }

            setImageList(getImgUrls(mNativeUnifiedADData));

            setStarRating(5.0);

            setSource(mNativeUnifiedADData.getTitle());

            if (mNativeUnifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_VIDEO_AD) {
                setAdImageMode(TTAdConstant.IMAGE_MODE_VIDEO);

            } else if (mNativeUnifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_BIG_IMAGE_AD) {
                setAdImageMode(TTAdConstant.IMAGE_MODE_LARGE_IMG);

            } else if (mNativeUnifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_GROUP_IMAGE_AD) {
                setAdImageMode(TTAdConstant.IMAGE_MODE_GROUP_IMG);
            }

            if (isAPPAD(mNativeUnifiedADData)) {
                setInteractionType(TTAdConstant.INTERACTION_TYPE_DOWNLOAD);
            } else {
                setInteractionType(TTAdConstant.INTERACTION_TYPE_LANDING_PAGE);
            }
        } catch (Exception e) {

        }
    }

    private static List<String> getImgUrls(NativeAdData adData) {
        List<String> imageUrlList = new ArrayList<>();

        if (adData == null) {
            return imageUrlList;
        }

        List<AdGainImage> imageList = adData.getImageList();

        if (imageList != null && !imageList.isEmpty()) {
            for (int i = 0; i < imageList.size(); i++) {
                AdGainImage image = imageList.get(i);
                if (image != null) {
                    String url = image.getImageUrl();
                    if (!TextUtils.isEmpty(url)) {
                        imageUrlList.add(url);
                    }
                }
            }

        }
        return imageUrlList;
    }

    private boolean isAPPAD(NativeAdData data) {
        boolean appDownloadType = data.getAdInteractiveType() == NativeAdInteractiveType.NATIVE_DOWNLOAD;

        AdAppInfo info = data.getAdAppInfo();

        if (info != null) {
            Log.d(TAG, "isAPPAD: package_name  = " + info.getPackageName() + "    app_size = " + info.getAppSize() + "  downloadType = " + appDownloadType);
        }

        return appDownloadType && info != null && !TextUtils.isEmpty(info.getPackageName());
    }

    @Override
    public void registerView(Activity activity,
                             ViewGroup container,
                             List<View> clickViews,
                             List<View> creativeViews,
                             List<View> directDownloadViews,
                             MediationViewBinder viewBinder) {

        ThreadUtils.runOnUIThreadByThreadPool(() -> {

            if (mNativeUnifiedADData != null && container instanceof FrameLayout) {

                FrameLayout nativeAdView = (FrameLayout) container;

                if (clickViews != null) {
                    clickViews.add(container);
                    clickViews.addAll(creativeViews);
                    fillChildView(container, clickViews);
                }

                mNativeUnifiedADData.bindViewForInteraction(container, clickViews, new NativeAdEventListener() {
                    @Override
                    public void onAdExposed() {
                        Log.d(TAG, "onADExposed");
                        callAdShow();
                    }

                    @Override
                    public void onAdClicked() {
                        Log.d(TAG, "onADClicked");
                        callAdClick();
                    }

                    @Override
                    public void onAdRenderFail(com.adgain.sdk.api.AdError error) {
                        callRenderFail(container, error.getErrorCode(), error.getMessage());

                    }
                });

                View targetView = null;

                if (clickViews != null) {
                    for (View view : clickViews) {
                        // Look for FrameLayout that can be used as video container
                        if (view instanceof FrameLayout) {
                            targetView = view;
                            break;
                        }
                    }
                }

                if (targetView instanceof FrameLayout && mNativeUnifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_VIDEO_AD) {

                    FrameLayout ttMediaView = (FrameLayout) targetView;

                    AdgainNativeAdMediaView gdtMediaView = new AdgainNativeAdMediaView(mContext);
                    ttMediaView.removeAllViews();
                    ttMediaView.addView(gdtMediaView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    mNativeUnifiedADData.bindMediaView(gdtMediaView, new NativeAdData.NativeAdMediaListener() {

                        @Override
                        public void onVideoLoad() {
                            Log.d(TAG, "onVideoLoad: ");
                        }

                        @Override
                        public void onVideoError(com.adgain.sdk.api.AdError adError) {
                            if (adError != null) {
                                Log.i(TAG, "onVideoError errorCode = " + adError.getErrorCode() + " errorMessage = " + adError.getMessage());
                                callVideoError(adError.getErrorCode(), adError.getMessage());
                            } else {
                                callVideoError(99999, "video error");
                            }
                        }

                        @Override
                        public void onVideoStart() {
                            Log.d(TAG, "onVideoStart");
                            callVideoStart();
                        }

                        @Override
                        public void onVideoPause() {
                            Log.d(TAG, "onVideoPause: ");
                            callVideoPause();
                        }

                        @Override
                        public void onVideoResume() {
                            Log.d(TAG, "onVideoResume");
                            callVideoResume();
                        }

                        @Override
                        public void onVideoCompleted() {
                            Log.d(TAG, "onVideoCompleted");
                            callVideoCompleted();
                        }
                    });
                }
            }
        });
    }

    private void fillChildView(View parentView, List<View> childViews) {
        if (parentView instanceof ViewGroup && !(parentView instanceof AdgainNativeAdMediaView)) {
            ViewGroup viewGroup = (ViewGroup) parentView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                fillChildView(child, childViews);
            }
        } else {
            childViews.add(parentView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "native onPause ");
        if (mNativeUnifiedADData != null) {
            mNativeUnifiedADData.pauseVideo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "native onResume: ");

        if (mNativeUnifiedADData != null) {
            mNativeUnifiedADData.resumeVideo();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "native  onDestroy ");

        if (mNativeUnifiedADData != null) {
            mNativeUnifiedADData.destroy();
            mNativeUnifiedADData = null;
        }
    }


    @Override
    public MediationConstant.AdIsReadyStatus isReadyCondition() {
        return mNativeAD != null && mNativeAD.isReady() ?
                MediationConstant.AdIsReadyStatus.AD_IS_READY
                : MediationConstant.AdIsReadyStatus.AD_IS_NOT_READY;
    }
}
