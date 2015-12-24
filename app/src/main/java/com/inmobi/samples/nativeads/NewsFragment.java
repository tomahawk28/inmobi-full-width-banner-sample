package com.inmobi.samples.nativeads;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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

public class NewsFragment extends Fragment {

    private static final String TAG = NewsFragment.class.getSimpleName();
    private static final int MAX_ADS = 50;

    private Handler mHandler = new Handler();
    private Map<NewsTileItem, WeakReference<InMobiNative>> mNativeAdMap = new HashMap<>();
    private List<NewsTileItem> mItemList = new ArrayList<>();
    private InMobiNative[] mNativeAds = new InMobiNative[MAX_ADS];
    private FeedAdapter mAdapter;

    private static final String FEED_URL = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=30&q=http://rss.nytimes.com/services/xml/rss/nyt/World.xml";
    private static final String FALLBACK_IMAGE_URL = "http://www.darrickbynum.com/wp-content/uploads/2014/07/News729x6581.jpg";
    private static final int[] AD_PLACEMENT_POSITIONS = new int[]{2, 4, 8, 13, 18};
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    private OnTileSelectedListener mCallback;

    public interface OnTileSelectedListener {
        void onTileSelected(int position);
    }

    static final class NewsTileItem {
        String imageUrl;
        String title;
        String content;
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_board, container, false);
        final GridView gridView = (GridView) rootView.findViewById(R.id.news_grid);
        mAdapter = new FeedAdapter(getActivity(), mItemList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // if the item at this position is an ad handle this
                NewsTileItem newsTile = mItemList.get(position);
                final WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(newsTile);
                if (nativeAdRef != null && nativeAdRef.get() != null) {
                    nativeAdRef.get().reportAdClickAndOpenLandingPage(null);
                }

                mCallback.onTileSelected(position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    gridView.setItemChecked(position, true);
                }
            }
        });

        getTiles();
        return rootView;
    }

    private void getTiles() {
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
                            loadNewsTiles(data);
                        }
                    });
                }
            }
        });
    }

    private void loadNewsTiles(String data) {
        try {
            JSONArray feed = new JSONObject(data)
                    .getJSONObject("responseData")
                    .getJSONObject("feed")
                    .getJSONArray("entries");
            int length = feed.length();
            for (int i = 0; i < length; i++) {
                JSONObject item = feed.getJSONObject(i);
                Log.v(TAG, item.toString());
                NewsTileItem feedEntry = new NewsTileItem();
                try {
                    feedEntry.title = item.getString("title");
                    if (item.isNull("mediaGroups")) {
                        feedEntry.imageUrl = FALLBACK_IMAGE_URL;
                    } else {
                        feedEntry.imageUrl = item.getJSONArray("mediaGroups").getJSONObject(0).getJSONArray("contents").getJSONObject(0).getString("url");
                    }
                    feedEntry.landingUrl = item.getString("link");
                    feedEntry.content = item.getString("contentSnippet");
                    mItemList.add(feedEntry);
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }

            mAdapter.notifyDataSetChanged();
            placeNativeAds();
        } catch (JSONException e) {
            e.printStackTrace();
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
                        NewsTileItem item = new NewsTileItem();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTileSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSettingsSavedListener");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        mNativeAdMap.clear();
        mItemList.clear();

        super.onDetach();
    }


    class ViewHolder {
        TextView headline;
        TextView content;
        TextView tag;
        SimpleDraweeView icon;
    }

    public class FeedAdapter extends ArrayAdapter<NewsTileItem> {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<NewsTileItem> mItems;

        public FeedAdapter(Context context, List<NewsTileItem> items) {
            super(context, R.layout.news_tile_view, items);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (null == rowView) {
                rowView = mInflater.inflate(R.layout.news_tile_view, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.headline = (TextView) rowView.findViewById(R.id.caption);
                viewHolder.content = (TextView) rowView.findViewById(R.id.content);
                viewHolder.icon = (SimpleDraweeView) rowView.findViewById(R.id.photo);
                viewHolder.tag = (TextView) rowView.findViewById(R.id.sponsored);
                rowView.setTag(viewHolder);
            }

            final NewsTileItem newsTileItem = mItems.get(position);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.headline.setText(newsTileItem.title);
            holder.content.setText(newsTileItem.content);
            holder.icon.setImageURI(Uri.parse(newsTileItem.imageUrl));

            WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(newsTileItem);
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
