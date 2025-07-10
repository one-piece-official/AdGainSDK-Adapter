package com.union_test.toutiao.mediation.java;

import static com.union_test.toutiao.utils.UIUtils.dp2px;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.mediation.ad.IMediationAdSlot;
import com.bytedance.sdk.openadsdk.mediation.ad.IMediationNativeToBannerListener;
import com.bytedance.sdk.openadsdk.mediation.ad.IMediationSplashRequestInfo;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationExpressRenderListener;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationNativeManager;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.mediation.java.utils.Const;
import com.union_test.toutiao.mediation.java.utils.FeedAdUtils;
import com.union_test.toutiao.utils.UIUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 融合demo，Feed流广告使用示例（模板和自渲染）。更多功能参考接入文档。
 * <p>
 * 注意：每次加载的广告，只能展示一次
 * <p>
 * 接入步骤：
 * 1、创建AdSlot对象
 * 2、创建TTAdNative对象
 * 3、创建加载、展示监听器
 * 4、加载广告
 * 5、加载成功后，展示广告
 * 6、在onDestroy中销毁广告
 */
public class MediationFeedActivity extends Activity {

    public String mMediaId; // 融合广告位

    private TTFeedAd mTTFeedAd; // Feed广告对象

    private TTAdNative.FeedAdListener mFeedAdListener; // 广告加载监听器
    private MediationExpressRenderListener mExpressAdInteractionListener; // 模板广告展示监听器

    private TTNativeAd.AdInteractionListener mAdInteractionListener; // 自渲染广告展示监听器

    private FrameLayout mFeedContainer;
    private String tag = "-----MediationFeedActivity";
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediation_activity_feed);
        context = this;
        // 聚合广告位（在GroMore平台的广告位，注意不是adn的代码位）
        mMediaId = getResources().getString(R.string.feed_native_express_media_id);
//        mMediaId = getResources().getString(R.string.feed_native_express_media_id);

        RadioGroup radioGroup = findViewById(R.id.typeRG);
        RadioButton expressRadio = findViewById(R.id.expressId);
        expressRadio.setChecked(true);
        expressRadio.setText("模板ID: " + mMediaId);
        RadioButton nativeRadio = findViewById(R.id.nativeId);
        nativeRadio.setText("自渲染ID: " + getResources().getString(R.string.feed_native_media_id));
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.expressId:
                        mMediaId = getResources().getString(R.string.feed_native_express_media_id);
                        break;
                    case R.id.nativeId:
                        mMediaId = getResources().getString(R.string.feed_native_media_id);
                        break;
                }
            }
        });

        // feed流广告容器
        mFeedContainer = findViewById(R.id.fl_content);

        // 广告加载
        findViewById(R.id.bt_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFeedAd();
            }
        });

        // 广告展示
        findViewById(R.id.bt_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedAd();
            }
        });
    }

    private void loadFeedAd() {
        /** 1、创建AdSlot对象 */

        Log.d(tag, "-------loadFeedAd:id  " + mMediaId);
        mTTFeedAd = null;
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(this.mMediaId)
                .setImageAcceptedSize(UIUtils.getScreenWidthInPx(this), dp2px(this, 340)) // 单位px
                .setAdCount(1) // 请求广告数量为1到3条 （优先采用平台配置的数量）
                .setMediationAdSlot(new IMediationAdSlot() {
                    @Override
                    public boolean isSplashShakeButton() {
                        return false;
                    }

                    @Override
                    public boolean isSplashPreLoad() {
                        return false;
                    }

                    @Override
                    public boolean isMuted() {
                        return false;
                    }

                    @Override
                    public float getVolume() {
                        return 0;
                    }

                    @Override
                    public boolean isUseSurfaceView() {
                        return false;
                    }

                    @Nullable
                    @Override
                    public Map<String, Object> getExtraObject() {
                        return Collections.emptyMap();
                    }

                    @Override
                    public boolean isBidNotify() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public String getScenarioId() {
                        return "";
                    }

                    @Override
                    public boolean isAllowShowCloseBtn() {
                        return false;
                    }

                    @Nullable
                    @Override
                    public IMediationNativeToBannerListener getMediationNativeToBannerListener() {
                        return null;
                    }

                    @Override
                    public float getShakeViewWidth() {
                        return 0;
                    }

                    @Override
                    public float getShakeViewHeight() {
                        return 0;
                    }

                    @Nullable
                    @Override
                    public String getWxAppId() {
                        return "";
                    }

                    @Nullable
                    @Override
                    public IMediationSplashRequestInfo getMediationSplashRequestInfo() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getRewardName() {
                        return "";
                    }

                    @Override
                    public int getRewardAmount() {
                        return 0;
                    }
                })
                .build();

        /** 2、创建TTAdNative对象 */

        TTAdNative adNativeLoader = TTAdManagerHolder.get().createAdNative(this);

        /** 3、创建加载、展示监听器 */
        initListeners();

        /** 4、加载广告 */
        if (adNativeLoader != null) {

            adNativeLoader.loadFeedAd(adSlot, mFeedAdListener);
        }
    }

    // 广告加载成功后，展示广告
    private void showFeedAd() {
        if (this.mTTFeedAd == null) {
            Log.i(tag, "请先加载广告或等待广告加载完毕后再调用show方法");
            return;
        }
        mTTFeedAd.uploadDislikeEvent("mediation_dislike_event");
        /** 5、展示广告 */
        MediationNativeManager manager = mTTFeedAd.getMediationManager();
        if (manager != null) {
            Log.d(tag, "--showFeedAd: " + manager.isExpress());
            if (manager.isExpress()) { // --- 模板feed流广告
                mTTFeedAd.setExpressRenderListener(mExpressAdInteractionListener);
                mTTFeedAd.render(); // 调用render方法进行渲染，在onRenderSuccess中展示广告

            } else {                   // --- 自渲染feed流广告

                // 自渲染广告返回的是广告素材，开发者自己将其渲染成view
                View feedView = FeedAdUtils.getFeedAdFromFeedInfo(mTTFeedAd, this, null, mAdInteractionListener);
                if (feedView != null) {
                    UIUtils.removeFromParent(feedView);
                    mFeedContainer.removeAllViews();
                    mFeedContainer.addView(feedView);
                }
            }
        }
    }

    private void initListeners() {
        // 广告加载监听器

        this.mFeedAdListener = new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int i, String s) {
                Log.d(tag, "feed load fail, errCode: " + i + ", errMsg: " + s);
            }

            @Override

            public void onFeedAdLoad(List<TTFeedAd> list) {
                if (list != null && list.size() > 0) {
                    Log.d(tag, "feed load success");
                    Toast.makeText(MediationFeedActivity.this, "success: " + list.size(), Toast.LENGTH_SHORT).show();
                    mTTFeedAd = list.get(0);
                } else {
                    Log.d(tag, "feed load success, but list is null");
                }
            }
        };
        // 模板广告展示监听器
        this.mExpressAdInteractionListener = new MediationExpressRenderListener() {
            @Override
            public void onRenderFail(View view, String s, int i) {
                Log.d(tag, "feed express render fail, errCode: " + i + ", errMsg: " + s);
            }

            @Override
            public void onAdClick() {
                Log.d(tag, "feed express click");
                Toast.makeText(context, "express onAdClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdShow() {
                Log.d(tag, "feed express show");
                Toast.makeText(context, "express show", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRenderSuccess(View view, float v, float v1, boolean b) {
                Log.d(tag, "feed express render success");
                if (mTTFeedAd != null) {
                    View expressFeedView = mTTFeedAd.getAdView(); // *** 注意不要使用onRenderSuccess参数中的view ***
                    UIUtils.removeFromParent(expressFeedView);
                    mFeedContainer.removeAllViews();
                    mFeedContainer.addView(expressFeedView);
                    mFeedContainer.setPadding(dp2px(context, 15), dp2px(context, 5), dp2px(context, 15), dp2px(context, 5));
                }
            }
        };
        // 自渲染广告展示监听器

        this.mAdInteractionListener = new TTNativeAd.AdInteractionListener() {
            @Override

            public void onAdClicked(View view, TTNativeAd ttNativeAd) {
                Log.d(tag, "feed click");
                Toast.makeText(MediationFeedActivity.this, "feed click", Toast.LENGTH_SHORT).show();
            }

            @Override

            public void onAdCreativeClick(View view, TTNativeAd ttNativeAd) {
                Log.d(tag, "feed creative click");
            }

            @Override

            public void onAdShow(TTNativeAd ttNativeAd) {
                Log.d(tag, "feed show");
                Toast.makeText(MediationFeedActivity.this, "曝光成功", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** 6、在onDestroy中销毁广告 */
        if (mTTFeedAd != null) {
            mTTFeedAd.destroy();
        }
    }
}
