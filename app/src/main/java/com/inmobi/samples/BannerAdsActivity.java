package com.inmobi.samples;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.samples.nativeads.NewsHeadlinesFragment;

import java.util.Map;

public class BannerAdsActivity extends ActionBarActivity implements
        NewsHeadlinesFragment.OnHeadlineSelectedListener {

    private static final String TAG = BannerAdsActivity.class.getSimpleName(); 
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    private InMobiBanner mBannerAd;
    private TextView rightTextView, leftTextView;
    @Override
    public void onArticleSelected(int position) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_ads);

        //mBannerAd = new InMobiBanner(BannerAdsActivity.this, YOUR_PLACEMENT_ID);
        mBannerAd = (InMobiBanner)findViewById(R.id.bannerView);
        rightTextView = (TextView)findViewById(R.id.but_right);
        leftTextView = (TextView) findViewById(R.id.but_left);

        int margin = getMarginWidth(320);
        int height_t = getHeight(50);
        ViewGroup.LayoutParams leftLayout = leftTextView.getLayoutParams();
        ViewGroup.LayoutParams rightLayout = rightTextView.getLayoutParams();

        leftLayout.width = margin;
        leftLayout.height = height_t;
        rightLayout.width = margin;
        rightLayout.height = height_t;
        // Update Params
        leftTextView.setLayoutParams(leftLayout);
        rightTextView.setLayoutParams(rightLayout);

        RelativeLayout fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container);
        RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.ad_container);
        if (fragmentContainer != null) {
            if (savedInstanceState != null) {
                return;
            }

            mBannerAd.setAnimationType(InMobiBanner.AnimationType.ROTATE_HORIZONTAL_AXIS);
            mBannerAd.setListener(new InMobiBanner.BannerAdListener() {
                @Override
                public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                    AlphaAnimation alpha = new AlphaAnimation(0.0F, 0.4F);
                    alpha.setDuration(0);
                    alpha.setFillAfter(true);
                    rightTextView.setVisibility(View.VISIBLE);
                    leftTextView.setVisibility(View.VISIBLE);

                    rightTextView.startAnimation(alpha);
                    leftTextView.startAnimation(alpha);

                    mBannerAd.setVisibility(View.VISIBLE);
                    // little_gray -> #8e8e8e
                    mBannerAd.setBackgroundResource(R.color.little_gray);
                }

                @Override
                public void onAdLoadFailed(InMobiBanner inMobiBanner,
                                           InMobiAdRequestStatus inMobiAdRequestStatus) {
                    rightTextView.setVisibility(View.INVISIBLE);
                    leftTextView.setVisibility(View.INVISIBLE);
                    mBannerAd.setVisibility(View.INVISIBLE);
                    Log.w(TAG, "Banner ad failed to load with error: " +
                            inMobiAdRequestStatus.getMessage());
                }

                @Override
                public void onAdDisplayed(InMobiBanner inMobiBanner) {
                }

                @Override
                public void onAdDismissed(InMobiBanner inMobiBanner) {
                }

                @Override
                public void onAdInteraction(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                }

                @Override
                public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                }

                @Override
                public void onAdRewardActionCompleted(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                }
            });

            NewsHeadlinesFragment headlinesFragment = new NewsHeadlinesFragment();
            Bundle args = getIntent().getExtras();
            if (null == args) {
                args = new Bundle();
            }
            args.putBoolean(NewsHeadlinesFragment.ARGS_PLACE_NATIVE_ADS, false);
            headlinesFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, headlinesFragment).commit();

           // int width = toPixelUnits(320);
           // int height= toPixelUnits(50);
            //RelativeLayout.LayoutParams bannerLayoutParams =
              //      new RelativeLayout.LayoutParams(width, height);
            //bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
           // bannerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            //adContainer.addView(mBannerAd, bannerLayoutParams);
            mBannerAd.load();
        }
    }

    private int toPixelUnits(int dipUnit) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dipUnit * density);
    }

    private int getHeight(int bannerHeight) {
        final float scale = getResources().getDisplayMetrics().density;

        int height = (int) (bannerHeight * scale + 0.5f);

        return height;
    }

    private int getMarginWidth(int bannerWidth){
        // Get Screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        //Get Banner actual width
        final float scale = getResources().getDisplayMetrics().density;


        int width = (int) (bannerWidth * scale + 0.5f);
        return (screenWidth-width)/2;
    }
}
