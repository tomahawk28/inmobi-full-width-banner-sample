package com.inmobi.samples.nativeads;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.samples.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoPagesFragment extends Fragment {

    private static final String TAG = PhotoPagesFragment.class.getSimpleName();
    private static final int MAX_ADS = 50;

    private static final int[] SAMPLE_IMAGE_RESOURCE_IDS = { R.drawable.cover1,
            R.drawable.cover2, R.drawable.cover3, R.drawable.cover4,
            R.drawable.cover5, R.drawable.cover6, R.drawable.cover7,
            R.drawable.cover8, R.drawable.cover9, R.drawable.cover10,
            R.drawable.cover11, R.drawable.cover12, R.drawable.cover13,
            R.drawable.cover14 };

    private Map<PageItem, WeakReference<InMobiNative>> mNativeAdMap = new HashMap<>();
    List<PageItem> mItemList = new ArrayList<>();
    private InMobiNative[] mNativeAds = new InMobiNative[MAX_ADS];
    private PagerAdapter mAdapter;

    private static final int[] AD_PLACEMENT_POSITIONS = new int[]{2, 4, 8, 13};
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    static final class PageItem {
        String imageUrl;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (InMobiNative nativeAd : mNativeAds) {
            if (nativeAd != null) {
                nativeAd.resume();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pages, container, false);
        for (int imageResourceId : SAMPLE_IMAGE_RESOURCE_IDS) {
            PageItem item = new PageItem();
            item.imageUrl = "res://" + getActivity().getPackageName() + "/" + imageResourceId;
            mItemList.add(item);
        }

        CustomViewPager pager = (CustomViewPager) rootView.findViewById(R.id.custom_page_view);
        mAdapter = new CustomPagerAdapter(getActivity(), mItemList);
        pager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        placeNativeAds();
        return rootView;
    }

    private void placeNativeAds() {
        for (int i = 0; i < AD_PLACEMENT_POSITIONS.length; i++) {
            final int position = AD_PLACEMENT_POSITIONS[i];
            InMobiNative nativeAd = new InMobiNative(YOUR_PLACEMENT_ID, new InMobiNative.NativeAdListener() {
                @Override
                public void onAdLoadSucceeded(final InMobiNative inMobiNative) {
                    try {
                        JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                        PageItem item = new PageItem();
                        item.imageUrl = content.getJSONObject("image_xhdpi").getString("url");
                        mItemList.add(position, item);
                        mNativeAdMap.put(item, new WeakReference<>(inMobiNative));
                        mAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Placed ad unit (" + inMobiNative.hashCode() +
                                ") at position " + position);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }

                @Override
                public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                    Log.e(TAG, "Failed to load ad. " + inMobiAdRequestStatus.getMessage());
                }

                @Override
                public void onAdDismissed(InMobiNative inMobiNative) {

                }

                @Override
                public void onAdDisplayed(InMobiNative inMobiNative) {

                }

                @Override
                public void onUserLeftApplication(InMobiNative inMobiNative) {

                }
            });
            nativeAd.load();
            mNativeAds[i] = nativeAd;
        }
    }

    @Override
    public void onPause() {
        for (InMobiNative nativeAd: mNativeAds) {
            if (nativeAd != null) {
                nativeAd.pause();
            }
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        mNativeAdMap.clear();
        mItemList.clear();

        super.onDetach();
    }

    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<PageItem> mItems;
        public CustomPagerAdapter(Context context, List<PageItem> items) {
            super();
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public final Object instantiateItem(ViewGroup container, int position) {
            CustomViewPagerItemWrapper coverFlowItem = new CustomViewPagerItemWrapper(mContext);
            View wrappedView = getCoverFlowItem(container, position);
            if (null == wrappedView) {
                throw new NullPointerException("getCoverFlowItem() was expected to return a view, but null was returned.");
            }

            coverFlowItem.addView(wrappedView);
            coverFlowItem.setLayoutParams(wrappedView.getLayoutParams());

            container.addView(coverFlowItem);
            return coverFlowItem;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            InMobiNative.unbind(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        private View getCoverFlowItem(ViewGroup container, int position) {
            View pageView = mInflater.inflate(R.layout.page_item_view, container, false);
            SimpleDraweeView imageView = (SimpleDraweeView) pageView.findViewById(R.id.photo);
            TextView tag = (TextView) pageView.findViewById(R.id.sponsored);
            PageItem item = mItems.get(position);
            imageView.setImageURI(Uri.parse(item.imageUrl));

            WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(item);
            if (null == nativeAdRef) {
                tag.setVisibility(View.GONE);
                InMobiNative.unbind(container);
            } else {
                // we have an ad at this position
                InMobiNative nativeAd = nativeAdRef.get();
                if (nativeAd != null) {
                    tag.setVisibility(View.VISIBLE);
                    tag.setText("Sponsored");
                    InMobiNative.bind(container, nativeAd);
                }
            }

            return pageView;
        }
    }
}
