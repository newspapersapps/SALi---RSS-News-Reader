package sali.rss.news.feed.reader.android.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import sali.rss.news.feed.reader.android.app.Constants;
import sali.rss.news.feed.reader.android.app.MainApplication;
import sali.rss.news.feed.reader.android.app.R;

public class Ads {

    public static void populateAd(final Activity activity, final AdView adview, final InterstitialAd interstitialAd, int adId, final Intent intent) {

        MobileAds.initialize(activity, activity.getString(R.string.app_id));

        AdRequest.Builder adsRqBuilder = new AdRequest.Builder();
        final AdRequest adRequest = adsRqBuilder.build();

        if (PrefUtils.getString(PrefUtils.AD_TYPE, Constants.INTERSTITIAL).equals(Constants.BANNER)) {
            LinearLayout bannerLin = (LinearLayout) activity.findViewById(adId);
            adview.setAdUnitId(activity.getString(R.string.banner));
            adview.setAdSize(AdSize.SMART_BANNER);
            adview.loadAd(adRequest);
            adview.setVisibility(View.GONE);
            adview.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    adview.setVisibility(View.VISIBLE);
                }
            });
            bannerLin.addView(adview);
        } else {
            interstitialAd.setAdUnitId(activity.getString(R.string.interstitial));
            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    if (intent != null)
                        activity.getApplicationContext().startActivity(intent);
                    interstitialAd.loadAd(adRequest);
                }
            });
        }
    }

    public static void showAd(Activity activity, final InterstitialAd interstitialAd, final Intent intent) {
        int clickedLinksCnt = ((MainApplication) activity.getApplicationContext()).getClickedLinksCnt();
        clickedLinksCnt++;
        if (interstitialAd != null && interstitialAd.isLoaded() && clickedLinksCnt >= Constants.LINK_CLICK_COUNT) {
            interstitialAd.show();
            clickedLinksCnt = 0;
        } else {
            if (intent != null)
                activity.getApplicationContext().startActivity(intent);
        }
        ((MainApplication) activity.getApplicationContext()).setClickedLinksCnt(clickedLinksCnt);
    }
}
