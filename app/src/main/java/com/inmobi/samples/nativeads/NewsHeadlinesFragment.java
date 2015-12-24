package com.inmobi.samples.nativeads;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class NewsHeadlinesFragment extends ListFragment {

    public static final String ARGS_PLACE_NATIVE_ADS = "should_place_native_ads";

    private static final String TAG = NewsHeadlinesFragment.class.getSimpleName();
    private static final int MAX_ADS = 50;

    @NonNull private final Handler mHandler = new Handler();
    private Map<NewsSnippet, WeakReference<InMobiNative>> mNativeAdMap = new HashMap<>();
    private List<NewsSnippet> mItemList = new ArrayList<>();
    private InMobiNative[] mNativeAds = new InMobiNative[MAX_ADS];
    private FeedAdapter mAdapter;

    private static final String FEED_URL = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=30&q=http://rss.nytimes.com/services/xml/rss/nyt/World.xml";
    private static final String FALLBACK_IMAGE_URL = "http://www.darrickbynum.com/wp-content/uploads/2014/07/News729x6581.jpg";
    private static final int[] AD_PLACEMENT_POSITIONS = new int[]{2, 4, 8, 13, 18};
    private static final long YOUR_PLACEMENT_ID = 1442498043574L;

    private OnHeadlineSelectedListener mCallback;

    public interface OnHeadlineSelectedListener {
        void onArticleSelected(int position);
    }

    static final class NewsSnippet {
        String title;
        String imageUrl;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new FeedAdapter(getActivity(), mItemList);
        setListAdapter(mAdapter);
        getHeadlines();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void getHeadlines() {
        new DataFetcher().getFeed(FEED_URL, new DataFetcher.OnFetchCompletedListener() {
            @Override
            public void onFetchCompleted(@Nullable final String data, @Nullable final String message) {
                if (null == data) {
                    Log.e(TAG, "Fetching headlines failed with error: " + message);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // finish();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadHeadlines(data);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int position, final long id) {
                AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getActivity());
                confirmationDialog.setTitle("Delete Item?");

                confirmationDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NewsSnippet newsSnippet = mItemList.get(position);
                        mItemList.remove(newsSnippet);
                        WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.remove(newsSnippet);
                        if (nativeAdRef != null) {
                            InMobiNative nativeAd = nativeAdRef.get();
                            if (nativeAd != null) {
                                InMobiNative.unbind(view);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
                confirmationDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                confirmationDialog.show();

                return true;
            }
        });
    }

    private void loadHeadlines(String data) {
        try {
            JSONArray feed = new JSONObject(data).
                    getJSONObject("responseData").
                    getJSONObject("feed").
                    getJSONArray("entries");
            for (int i = 0; i < feed.length(); i++) {
                JSONObject item = feed.getJSONObject(i);
                Log.v(TAG, item.toString());
                NewsSnippet feedEntry = new NewsSnippet();
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
            Bundle args = getArguments();
            boolean shouldPlaceNativeAds = args.getBoolean(ARGS_PLACE_NATIVE_ADS, false);
            if (shouldPlaceNativeAds) {
                placeNativeAds();
            }
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
                        NewsSnippet item = new NewsSnippet();
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
            mCallback = (OnHeadlineSelectedListener) activity;
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // if the item at this position is an ad handle this
        NewsSnippet newsSnippet = mItemList.get(position);
        final WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(newsSnippet);
        if (nativeAdRef != null && nativeAdRef.get() != null) {
            nativeAdRef.get().reportAdClickAndOpenLandingPage(null);
        }

        mCallback.onArticleSelected(position);
        getListView().setItemChecked(position, true);
    }

    class ViewHolder {
        TextView headline;
        TextView content;
        TextView tag;
        SimpleDraweeView icon;
    }

    public class FeedAdapter extends ArrayAdapter<NewsSnippet> {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<NewsSnippet> mItems;

        public FeedAdapter(Context context, List<NewsSnippet> items) {
            super(context, R.layout.news_headline_view, items);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (null == rowView) {
                rowView = mInflater.inflate(R.layout.news_headline_view, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.headline = (TextView) rowView.findViewById(R.id.caption);
                viewHolder.content = (TextView) rowView.findViewById(R.id.content);
                viewHolder.icon = (SimpleDraweeView) rowView.findViewById(R.id.photo);
                viewHolder.tag = (TextView) rowView.findViewById(R.id.sponsored);
                rowView.setTag(viewHolder);
            }

            final NewsSnippet newsSnippet = mItems.get(position);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.headline.setText(newsSnippet.title);
            holder.content.setText(newsSnippet.content);
            holder.icon.setImageURI(Uri.parse(newsSnippet.imageUrl));

            WeakReference<InMobiNative> nativeAdRef = mNativeAdMap.get(newsSnippet);
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
