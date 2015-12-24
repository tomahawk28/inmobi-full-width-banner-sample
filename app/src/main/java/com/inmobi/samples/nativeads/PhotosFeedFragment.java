package com.inmobi.samples.nativeads;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.samples.R;
import com.inmobi.samples.utils.DataFetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotosFeedFragment extends ListFragment {

    private static final String TAG = PhotosFeedFragment.class.getSimpleName();
    private static final int MAX_ADS = 50;

    @NonNull private final Handler mHandler = new Handler();
    private Map<PhotosFeedItem, WeakReference<InMobiNative>> mNativeAdMap = new HashMap<>();
    private List<PhotosFeedItem> mItemList = new ArrayList<>();
    private InMobiNative[] mNativeAds = new InMobiNative[MAX_ADS];
    private FeedAdapter mAdapter;

    private static final String FEED_URL = "https://api.instagram.com/v1/users/595017071/media/recent?client_id=8ff39eb66c424c89ad26adfb0dd1ca2c";
    private static final int[] AD_PLACEMENT_POSITIONS = new int[]{2, 4, 8, 13, 18};
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    static final class PhotosFeedItem {
        String title;
        String imageUrl;
        String landingUrl;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new FeedAdapter(getActivity(), mItemList);
        setListAdapter(mAdapter);
        getPhotos();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void getPhotos() {
        new DataFetcher().getFeed(FEED_URL, new DataFetcher.OnFetchCompletedListener() {
            @Override
            public void onFetchCompleted(final String data, final String message) {
                if (null == data) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadPhotos(data);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // if the item at this position is an ad handle this
        // if the item at this position is an ad handle this
        PhotosFeedItem photoItem = mItemList.get(position);
        final WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(photoItem);
        if (nativeAdRef != null && nativeAdRef.get() != null) {
            nativeAdRef.get().reportAdClickAndOpenLandingPage(null);
        }

        getListView().setItemChecked(position, true);
    }

    private void loadPhotos(String data) {
        try {
            JSONArray feed = new JSONObject(data).getJSONArray("data");
            for (int i = 0; i < feed.length(); i++) {
                JSONObject item = feed.getJSONObject(i);
                Log.v(TAG, item.toString());
                PhotosFeedItem feedEntry = new PhotosFeedItem();
                try {
                    feedEntry.title = item.getJSONObject("caption").getString("text");
                    feedEntry.imageUrl=item.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                    feedEntry.landingUrl=item.getString("link");
                    mItemList.add(feedEntry);
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }

            mAdapter.notifyDataSetChanged();
            placeNativeAds();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void placeNativeAds() {
        for (int i = 0; i < AD_PLACEMENT_POSITIONS.length; i++) {
            final int position = AD_PLACEMENT_POSITIONS[i];
            InMobiNative nativeAd = new InMobiNative(YOUR_PLACEMENT_ID, new InMobiNative.NativeAdListener() {
                @Override
                public void onAdLoadSucceeded(final InMobiNative inMobiNative) {
                    try {
                        JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                        PhotosFeedItem item = new PhotosFeedItem();
                        item.title = content.getString("title");
                        item.landingUrl = content.getString("click_url");
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

    class ViewHolder {
        TextView title;
        SimpleDraweeView image;
        TextView tag;
    }

    public class FeedAdapter extends ArrayAdapter<PhotosFeedItem> {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<PhotosFeedItem> mItems;

        public FeedAdapter(Context context, List<PhotosFeedItem> items) {
            super(context, R.layout.photos_item_view, items);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (null == rowView) {
                rowView = mInflater.inflate(R.layout.photos_item_view, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView) rowView.findViewById(R.id.caption);
                viewHolder.image = (SimpleDraweeView) rowView.findViewById(R.id.photo);
                viewHolder.tag = (TextView) rowView.findViewById(R.id.sponsored);
                rowView.setTag(viewHolder);
            }

            final PhotosFeedItem photosFeedItem = mItems.get(position);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.title.setText(photosFeedItem.title);
            holder.image.setImageURI(Uri.parse(photosFeedItem.imageUrl));

            WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(photosFeedItem);
            if (null == nativeAdRef) {
                holder.tag.setVisibility(View.GONE);
                InMobiNative.unbind(rowView);
            } else {
                // we have an ad at this position
                InMobiNative nativeAd = nativeAdRef.get();
                if (nativeAd != null) {
                    holder.tag.setVisibility(View.VISIBLE);
                    holder.tag.setText("Sponsored");
                    InMobiNative.bind(rowView, nativeAd);
                }
            }

            return rowView;
        }
    }

}
