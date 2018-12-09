package sali.rss.news.feed.reader.android.app;

import android.app.NotificationManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;

public final class Constants {

    public static final NotificationManager NOTIF_MGR = (NotificationManager) MainApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

    public static final String INTENT_FROM_WIDGET = "fromWidget";

    public static final String FEED_ID = "feedid";

    public static final String DB_COUNT = "COUNT(*)";
    public static final String DB_IS_TRUE = "=1";
    public static final String DB_IS_FALSE = "=0";
    public static final String DB_IS_NULL = " IS NULL";
    public static final String DB_IS_NOT_NULL = " IS NOT NULL";
    public static final String DB_DESC = " DESC";
    public static final String DB_ASC = " ASC";
    public static final String DB_ARG = "=?";
    public static final String DB_AND = " AND ";
    public static final String DB_OR = " OR ";

    public static final String HTTP_SCHEME = "http://";
    public static final String HTTPS_SCHEME = "https://";
    public static final String FILE_SCHEME = "file://";

    public static final String HTML_LT = "&lt;";
    public static final String HTML_GT = "&gt;";
    public static final String LT = "<";
    public static final String GT = ">";

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final String ENCLOSURE_SEPARATOR = "[@]"; // exactly three characters!

    public static final String HTML_QUOT = "&quot;";
    public static final String QUOT = "\"";
    public static final String HTML_APOSTROPHE = "&#39;";
    public static final String APOSTROPHE = "'";
    public static final String AMP = "&";
    public static final String AMP_SG = "&amp;";
    public static final String SLASH = "/";
    public static final String COMMA_SPACE = ", ";

    public static final String UTF8 = "UTF-8";

    public static final String FROM_AUTO_REFRESH = "from_auto_refresh";

    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";

    public static final int UPDATE_THROTTLE_DELAY = 200;

    public static final String FETCH_PICTURE_MODE_WIFI_ONLY_PRELOAD = "WIFI_ONLY_PRELOAD";
    public static final String FETCH_PICTURE_MODE_ALWAYS_PRELOAD = "ALWAYS_PRELOAD";
    public static final MatrixCursor EMPTY_CURSOR = new MatrixCursor(new String[]{BaseColumns._ID});

    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36";

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String PLAY_STORE_URI = "market://details?id=";
    public static final String DEV_PAGE_URI = "market://search?q=pub:Newspapers Apps";

    public static final int INSTALL_DAYS = 3;
    public static final int LAUNCH_TIMES = 10;

    public static final String BANNER = "banner";
    public static final String INTERSTITIAL = "interstitial";
    public static final int LINK_CLICK_COUNT = 12;

}
