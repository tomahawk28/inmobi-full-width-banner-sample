package com.inmobi.samples;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;

import java.util.Map;

public class InterstitialAdsActivity extends ActionBarActivity {

    private static final String TAG = InterstitialAdsActivity.class.getSimpleName();
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    private InMobiInterstitial mInterstitialAd;
    private Button mLoadAdButton;
    private Button mShowAdButton;
    private Button mShowAdWithAnimation;

    @Override
    public void onResume() {
        super.onResume();
        if (mShowAdButton != null) {
            mShowAdButton.setVisibility(View.GONE);
            mShowAdWithAnimation.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_ads);

        mLoadAdButton = (Button) findViewById(R.id.button_load_ad);
        mShowAdButton = (Button) findViewById(R.id.button_show_ad);
        mShowAdWithAnimation = (Button) findViewById(R.id.button_show_ad_with_animation);

        mLoadAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.load();
            }
        });

        mShowAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show();
            }
        });

        mShowAdWithAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show(R.anim.right_in, R.anim.left_out);
            }
        });

        mInterstitialAd = new InMobiInterstitial(InterstitialAdsActivity.this, YOUR_PLACEMENT_ID,
                new InMobiInterstitial.InterstitialAdListener() {
                    @Override
                    public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {

                    }

                    @Override
                    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {

                    }

                    @Override
                    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {

                    }

                    @Override
                    public void onAdInteraction(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {

                    }

                    @Override
                    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                        if (inMobiInterstitial.isReady()) {
                            if (mShowAdButton != null) {
                                mShowAdButton.setVisibility(View.VISIBLE);
                                mShowAdWithAnimation.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        Log.w(TAG, "Unable to load interstitial ad (error message: " +
                                inMobiAdRequestStatus.getMessage() + ")");
                    }

                    @Override
                    public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {

                    }
                });
    }
}
