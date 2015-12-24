# inmobi-full-width-banner-sample
InMobi Full-width banner sample app



###Screenshot
####Landscape
![Landscape](http://tomahawk28.github.io/images/landscape.gif)
####Portrait
![Portrait](http://tomahawk28.github.io/images/portrait.gif)

---
###How to integrate
---

####1. 삽입될 XML Layout 코드를 아래처럼 바꿔주세요

**Case: SDK 4.X.X**

```xml
<RelativeLayout
        android:id="@+id/layout_bottom"
        android:layout_width="match_parent"
        android:layout_height = "match_parent"
    >
  <TextView
            android:id = "@+id/but_left"
            android:layout_width = "wrap_content"
            android:layout_height = "wrap_content"
            android:text="w"
            android:layout_alignParentLeft = "true"
            android:background="@color/black"
            android:visibility="invisible"/>
  <com.inmobi.monetization.IMBanner
            android:id="@+id/bannerView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_toLeftOf = "@+id/but_right"
            android:layout_toStartOf="@+id/but_right"
            android:layout_toRightOf = "@id/but_left"
            android:layout_toEndOf="@+id/but_left"
             />
  <TextView
            android:id = "@+id/but_right"
            android:layout_width = "wrap_content"
            android:layout_height = "wrap_content"
            android:text="h"
            android:layout_alignParentRight = "true"
            android:background="@color/black"
            android:visibility="invisible"/>
</RelativeLayout>
```

**Case: SDK 5.X.X**

>ads namespace는 부모 Element에 `xmlns:ads="http://schemas.android.com/apk/lib/com.inmobi.ads"` 속성을 추가해야 합니다

```xml
<TextView
            android:id = "@+id/but_left"
            android:layout_width = "wrap_content"
            android:layout_height = "wrap_content"
            android:text="w"
            android:layout_alignParentLeft = "true"
            android:background="@color/black"
            android:visibility="invisible"/>
<com.inmobi.ads.InMobiBanner
            android:id="@+id/bannerView"
            ads:placementId="1442498043574"
            android:layout_width="320dp"
            android:layout_height="50dp"
            android:layout_toLeftOf = "@+id/but_right"
            android:layout_toStartOf="@+id/but_right"
            android:layout_toRightOf = "@id/but_left"
            android:layout_toEndOf="@+id/but_left"
            />
<TextView
            android:id = "@+id/but_right"
            android:layout_width = "wrap_content"
            android:layout_height = "wrap_content"
            android:text="h"
            android:layout_alignParentRight = "true"
            android:background="@color/black"
            android:visibility="invisible"/>
```
__invisible__  항목이 추가되걸 보실 수 있습니다.


그 후에는 `Java` 코드에 `but_left` 와 `but_right`, 양 옆 Text Element의 Width 사이즈를 계산해 줄 코드가 필요합니다.

####2. 아래 2개 메소드를 `Activity`가 포함된 `Java` 파일에 넣어주세요

**Both SDK 4.X.X and 5.X.X**

```java
private int getHeight(int bannerHeight) {
	final float scale = getResources().getDisplayMetrics().density;

	int height = (int)(bannerHeight * scale + 0.5f);

	return height;
}

private int getMarginWidth(int bannerWidth) {
	// Get Screen width
	Display display = getWindowManager().getDefaultDisplay();
	Point size = new Point();
	display.getSize(size);
	int screenWidth = size.x;

	//Get Banner actual width
	final float scale = getResources().getDisplayMetrics().density;
	int width = (int)(bannerWidth * scale + 0.5f);
	return (screenWidth - width) / 2;
}
```

####2. `onCreate` 메소드 또는 상응하는 메소드에 아래 코드를 넣어줍니다. 

**Both SDK 4.X.X and 5.X.X**

```java
rightTextView = (TextView) findViewById(R.id.but_right);
leftTextView = (TextView) findViewById(R.id.but_left);

int margin = getMarginWidth(320);
int height = getHeight(50);
ViewGroup.LayoutParams leftLayout = leftTextView.getLayoutParams();
ViewGroup.LayoutParams rightLayout = rightTextView.getLayoutParams();

leftLayout.width = margin;
leftLayout.height = height;
rightLayout.width = margin;
rightLayout.height = height;

// Update Params
leftTextView.setLayoutParams(leftLayout);
rightTextView.setLayoutParams(rightLayout);
 
```

####3. IMBanner 객체, 또는 InMobiBanner 객체의  Callback Listener에 아래와 같은 코드를 넣습니다.

**Case: SDK 4.X.X**

```java
bannerAdView.setIMBannerListener(new IMBannerListener() {...@Override
	public void onBannerRequestFailed(IMBanner imBanner, IMErrorCode imErrorCode) {
		rightTextView.setVisibility(View.INVISIBLE);
		leftTextView.setVisibility(View.INVISIBLE);
		bannerAdView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onBannerRequestSucceeded(IMBanner imBanner) {
		AlphaAnimation alpha = new AlphaAnimation(0.0F, 0.4F);
		alpha.setDuration(0);
		alpha.setFillAfter(true);
		rightTextView.setVisibility(View.VISIBLE);
		leftTextView.setVisibility(View.VISIBLE);

		rightTextView.startAnimation(alpha);
		leftTextView.startAnimation(alpha);

		bannerAdView.setVisibility(View.VISIBLE);
		// little_grey -> #8e8e8e
		bannerAdView.setBackgroundResource(R.color.little_grey);
	}

});
```

**Case: SDK 5.X.X**
```java
mBannerAd.setListener(new InMobiBanner.BannerAdListener() {@Override
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
		Log.w(TAG, "Banner ad failed to load with error: " + inMobiAdRequestStatus.getMessage());
	}
}
```

위의 코드는 광고가 성공적으로 불러들러올 경우만 여백의 바탕색이 나타나도록 바꾼 것입니다.
광고 불러오기 실패의 경우엔 주변 배경이 모두 사라집니다.
