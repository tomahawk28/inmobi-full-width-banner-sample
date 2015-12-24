package com.inmobi.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.inmobi.sdk.InMobiSdk;

public class MainActivity extends ActionBarActivity
        implements AdUnitsFragment.OnAdUnitSelectedListener {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_units);
        Fresco.initialize(this);

        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        InMobiSdk.init(this, "<your account id here>");

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            AdUnitsFragment adUnitsFragment = new AdUnitsFragment();
            adUnitsFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, adUnitsFragment).commit();
        }
    }

    public void onAdUnitSelected(int position) {
        switch (position) {
            default:
            case 0:
                startActivity(new Intent(MainActivity.this, BannerAdsActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, InterstitialAdsActivity.class));
                break;
            case 2:
                startActivity(new Intent(MainActivity.this, NativeAdsActivity.class));
                break;
        }
    }
}
