package sali.rss.news.feed.reader.android.app;

import android.app.Application;
import android.content.Context;

import sali.rss.news.feed.reader.android.app.utils.PrefUtils;

public class MainApplication extends Application {

    private static Context mContext;
    private int clickedLinksCnt;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init
    }

    public int getClickedLinksCnt() {
        return clickedLinksCnt;
    }

    public void setClickedLinksCnt(int clickedLinksCnt) {
        this.clickedLinksCnt = clickedLinksCnt;
    }
}
