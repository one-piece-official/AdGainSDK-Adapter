package com.test.ad.demo.customize;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adgain.sdk.api.AdAppInfo;
import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdGainImage;
import com.adgain.sdk.api.NativeAdData;
import com.adgain.sdk.api.NativeAdEventListener;
import com.adgain.sdk.api.NativeAdInteractiveType;
import com.adgain.sdk.api.NativeAdPatternType;
import com.adgain.sdk.base.natives.GtNativeAdMediaView;
import com.anythink.nativead.api.ATNativePrepareExInfo;
import com.anythink.nativead.api.ATNativePrepareInfo;
import com.anythink.nativead.api.NativeAdInteractionType;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//Self-rendering 2.0
public class GTNativeAd extends CustomNativeAd {

    private static final String TAG = AdGainInitManager.TAG;

    WeakReference<Context> mContext;

    Context mApplicationContext;

    NativeAdData mUnifiedAdData;

    boolean mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;

    View mClickView;

    //0:not set,    1:set mute,   2:not mute
    int mMuteApiSet = 0;

    GtNativeAdMediaView mMediaView;

    //Self-rendering 2.0 must be used
    ViewGroup mContainer;

    protected GTNativeAd(Context context, NativeAdData gdtAd, boolean videoMuted, int videoAutoPlay, int videoDuration) {

        mApplicationContext = context.getApplicationContext();
        mContext = new WeakReference<>(context);

        mVideoMuted = videoMuted;
        mVideoAutoPlay = videoAutoPlay;
        mVideoDuration = videoDuration;

        mUnifiedAdData = gdtAd;

        setAdData(mUnifiedAdData);
    }

    public String getCallToAction(NativeAdData ad) {

        if (!TextUtils.isEmpty(ad.getCTAText())) {
            return ad.getCTAText();
        }

        return "点击查看";
    }

    private void setAdData(final NativeAdData unifiedADData) {
        setTitle(unifiedADData.getTitle());
        setDescriptionText(unifiedADData.getDesc());

        setIconImageUrl(unifiedADData.getIconUrl());

        setCallToActionText(getCallToAction(unifiedADData));

        List<AdGainImage> list = unifiedADData.getImageList();
        if (list != null && !list.isEmpty()) {
            AdGainImage image = list.get(0);

            Log.d(TAG, "setAdData main image =  " + image.toString());

            setMainImageUrl(image.getImageUrl());
            setMainImageWidth(image.getWidth());
            setMainImageHeight(image.getHeight());
        }

        setStarRating(5.0);

        setNativeInteractionType(isAPPAD(unifiedADData) ? NativeAdInteractionType.APP_DOWNLOAD_TYPE : NativeAdInteractionType.UNKNOW);

        setAdAppInfo(new GTDownloadAppInfo(unifiedADData.getAdAppInfo(), "5000"));
        setAppPrice(unifiedADData.getPrice());

        setImageUrlList(getImgUrls(unifiedADData));

        setVideoDuration(unifiedADData.getVideoDuration());

        Log.d(TAG, " type === " + unifiedADData.getAdPatternType());

        if (unifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_VIDEO_AD) {
            mAdSourceType = NativeAdConst.VIDEO_TYPE;
            Log.d(TAG, " ===== VIDEO_TYPE  ====");

        } else {
            mAdSourceType = NativeAdConst.IMAGE_TYPE;
        }

         //setNetworkInfoMap(unifiedADData.getExtraInfo());
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
    public View getAdMediaView(Object... object) {
        if (mUnifiedAdData != null) {

            if (mUnifiedAdData.getAdPatternType() != NativeAdPatternType.NATIVE_VIDEO_AD) {
                return super.getAdMediaView(object);
            }

            if (mMediaView == null) {
                mMediaView = new GtNativeAdMediaView(mApplicationContext);
//                mMediaView.setBackgroundColor(0xff000000);
                ViewGroup.LayoutParams _params = mMediaView.getLayoutParams();
                if (_params == null) {
                    _params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                mMediaView.setLayoutParams(_params);

            }

            return mMediaView;
        }

        return super.getAdMediaView(object);
    }

    @Override
    public boolean isNativeExpress() {
        return false;
    }

    @Override
    public void prepare(View view, ATNativePrepareInfo nativePrepareInfo) {
        if (mUnifiedAdData != null && mContainer != null) {

            List<View> clickViewList = nativePrepareInfo.getClickViewList();

            if (clickViewList == null || clickViewList.isEmpty()) {
                clickViewList = new ArrayList<>();
                fillChildView(view, clickViewList);
            }

            List<View> downloadDirectlyClickViews = new ArrayList<>();
            if (nativePrepareInfo instanceof ATNativePrepareExInfo) {
                List<View> creativeClickViewList = ((ATNativePrepareExInfo) nativePrepareInfo).getCreativeClickViewList();
                if (creativeClickViewList != null) {
                    downloadDirectlyClickViews.addAll(creativeClickViewList);
                }
            }

            mUnifiedAdData.bindViewForInteraction(view, clickViewList, downloadDirectlyClickViews, null, null, new NativeAdEventListener() {
                @Override
                public void onAdExposed() {
                    notifyAdImpression();
                }

                @Override
                public void onAdClicked() {
                    mClickView = view;
                    notifyAdClicked();
                }

                @Override
                public void onAdRenderFail(AdError error) {

                }
            });

            try {
                if (mMediaView == null) {
                    return;
                }

                bindMediaView();

                if (mMuteApiSet > 0) {
                    boolean result = mMuteApiSet == 1;
                    Log.d(TAG, "mMuteApiSet  setVideoMute " + result);

                    mUnifiedAdData.setVideoMute(result);

                } else {
                    Log.d(TAG, " setVideoMute " + mVideoMuted);
                    mUnifiedAdData.setVideoMute(mVideoMuted);
                }

            } catch (Throwable e) {
                Log.d(TAG, "prepare: exception = " + Log.getStackTraceString(e));
            }
        }
    }

    private void bindMediaView() {
        mUnifiedAdData.bindMediaView(mMediaView, new NativeAdData.NativeAdMediaListener() {

            @Override
            public void onVideoLoad() {
                Log.d(TAG, "onVideoLoad: ");
            }

            @Override
            public void onVideoError(AdError error) {
                Log.d(TAG, "onVideoError: " + error);
                notifyAdVideoVideoPlayFail("" + error.getErrorCode(), error.getMessage());
            }

            @Override
            public void onVideoStart() {
                Log.d(TAG, "onVideoStart: ");
                notifyAdVideoStart();
            }

            @Override
            public void onVideoPause() {
                Log.d(TAG, "onVideoPause: ");
            }

            @Override
            public void onVideoResume() {
                Log.d(TAG, "onVideoResume: ");
            }

            @Override
            public void onVideoCompleted() {
                Log.d(TAG, "onVideoCompleted: ");
                notifyAdVideoEnd();
            }
        });
    }

    @Override
    public ViewGroup getCustomAdContainer() {
        if (mUnifiedAdData != null) {
            mContainer = new FrameLayout(mApplicationContext);
        }
        return mContainer;
    }

    private void fillChildView(View parentView, List<View> childViews) {
        if (parentView instanceof ViewGroup && parentView != mMediaView) {
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
    public void clear(View view) {
        unregisterView(view);
    }

    private void unregisterView(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                unregisterView(child);
            }
        } else {
            view.setOnClickListener(null);
            view.setClickable(false);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        if (mUnifiedAdData != null) {
            mUnifiedAdData.resumeVideo();
        }
    }

    @Override
    public void resumeVideo() {
        Log.d(TAG, "resumeVideo: ");
        if (mUnifiedAdData != null) {
            mUnifiedAdData.resumeVideo();
        }
    }

    @Override
    public void pauseVideo() {
        Log.d(TAG, "pauseVideo: ");
        if (mUnifiedAdData != null) {
            mUnifiedAdData.pauseVideo();
        }
    }

    @Override
    public void setVideoMute(boolean isMute) {
        Log.d(TAG, "setVideoMute: isMute = " + isMute);

        mMuteApiSet = isMute ? 1 : 2;

        if (mUnifiedAdData != null) {
            mUnifiedAdData.setVideoMute(isMute);
        }
    }

    @Override
    public double getVideoProgress() {
        if (mUnifiedAdData != null) {
            return mUnifiedAdData.getVideoProgress();
        }
        return super.getVideoProgress();
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

    @Override
    public void destroy() {
        super.destroy();

        Log.d(TAG, "destroy: ");

        if (mUnifiedAdData != null) {
            mUnifiedAdData.destroy();
            mUnifiedAdData = null;
        }
        mMediaView = null;

        mApplicationContext = null;
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }

        if (mContainer != null) {
            mContainer.removeAllViews();
            mContainer = null;
        }
    }
}
