package com.test.ad.demo.customize;


import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.api.ATNativePrepareExInfo;
import com.anythink.nativead.api.ATNativePrepareInfo;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.gt.sdk.api.AdError;
import com.gt.sdk.api.GtImage;
import com.gt.sdk.api.NativeAdData;
import com.gt.sdk.api.NativeAdEventListener;
import com.gt.sdk.api.NativeAdPatternType;
import com.gt.sdk.base.natives.GtNativeAdMediaView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class GTNativeAd extends CustomNativeAd {

    private static final String TAG = GTInitManager.TAG;

    WeakReference<Context> mContext;

    Context mApplicationContext;

    //NativeUnifiedADData mUnifiedAdData;
    //Self-rendering 2.0
    NativeAdData mUnifiedAdData;

    int mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;

    //0:not set,1:set mute,2:not mute
    View mClickView;
    int mMuteApiSet = 0;

    //    MediaView mMediaView;
    GtNativeAdMediaView mMediaView;

    //Self-rendering 2.0 must be used
    ViewGroup mContainer;

    protected GTNativeAd(Context context, NativeAdData gdtAd, int videoMuted, int videoAutoPlay, int videoDuration) {

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
        setAppPrice(unifiedADData.getPrice());

        setCallToActionText(getCallToAction(unifiedADData));

        // todo
//        setStarRating((double) unifiedADData.getAppScore());
//        setMainImageUrl(unifiedADData.getImgUrl());
//        setMainImageWidth(unifiedADData.getPictureWidth());
//        setMainImageHeight(unifiedADData.getPictureHeight());
//        setNativeInteractionType(unifiedADData.isAppAd() ? NativeAdInteractionType.APP_DOWNLOAD_TYPE : NativeAdInteractionType.UNKNOW);

        setImageUrlList(getImgUrls(unifiedADData));

        setVideoDuration(unifiedADData.getVideoDuration());


        if (unifiedADData.getAdAppInfo() != null) {
            setAdAppInfo(new GTDownloadAppInfo(unifiedADData.getAdAppInfo(), "5000"));
        }

        if (unifiedADData.getAdPatternType() == NativeAdPatternType.NATIVE_VIDEO_AD) {
            mAdSourceType = NativeAdConst.VIDEO_TYPE;
        } else {
            mAdSourceType = NativeAdConst.IMAGE_TYPE;
        }

        // setNetworkInfoMap(unifiedADData.getExtraInfo());
    }

    @Override
    public View getAdMediaView(Object... object) {
        if (mUnifiedAdData != null) {

            if (mUnifiedAdData.getAdPatternType() != NativeAdPatternType.NATIVE_VIDEO_AD) {
                return super.getAdMediaView(object);
            }

            if (mMediaView == null) {
                mMediaView = new GtNativeAdMediaView(mApplicationContext);
                mMediaView.setBackgroundColor(0xff000000);
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

            FrameLayout.LayoutParams layoutParams = nativePrepareInfo.getChoiceViewLayoutParams();

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

            //mUnifiedAdData.bindAdToView(view.getContext(), mContainer, layoutParams, clickViewList, downloadDirectlyClickViews);

            try {
                if (mMediaView == null) {
                    return;
                }

                bindMediaView();

                // todo
                /*if (mMuteApiSet > 0) {
                    mUnifiedAdData.setVideoMute(mMuteApiSet == 1);
                }*/

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void bindMediaView() {
        mUnifiedAdData.bindMediaView(mMediaView, new NativeAdData.NativeAdMediaListener() {

            @Override
            public void onVideoLoad() {

            }

            @Override
            public void onVideoError(AdError error) {
                notifyAdVideoVideoPlayFail("" + error.getErrorCode(), error.getMessage());
            }

            @Override
            public void onVideoStart() {
                notifyAdVideoStart();
            }

            @Override
            public void onVideoPause() {
            }

            @Override
            public void onVideoResume() {
            }

            @Override
            public void onVideoCompleted() {
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
        if (mUnifiedAdData != null) {
            mUnifiedAdData.resumeVideo();
        }
    }

    @Override
    public void resumeVideo() {
        if (mUnifiedAdData != null) {
            mUnifiedAdData.resumeVideo();
        }
    }

    @Override
    public void pauseVideo() {
        if (mUnifiedAdData != null) {
            mUnifiedAdData.pauseVideo();
        }
    }

    @Override
    public void setVideoMute(boolean isMute) {
        // todo
        /*mMuteApiSet = isMute ? 1 : 2;
        if (mUnifiedAdData != null) {
            mUnifiedAdData.setVideoMute(isMute);
        }*/
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

        List<GtImage> imageList = adData.getImageList();

        if (imageList != null && !imageList.isEmpty()) {
            for (int i = 0; i < imageList.size(); i++) {
                GtImage image = imageList.get(i);
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
