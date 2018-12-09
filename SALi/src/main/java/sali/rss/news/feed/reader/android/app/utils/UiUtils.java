package sali.rss.news.feed.reader.android.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import sali.rss.news.feed.reader.android.app.MainApplication;
import sali.rss.news.feed.reader.android.app.R;
import sali.rss.news.feed.reader.android.app.provider.FeedData;

public class UiUtils {

    static private final LongSparseArray<Bitmap> FAVICON_CACHE = new LongSparseArray<>();

    static public void setPreferenceTheme(Activity a) {
        a.setTheme(getAppTheme());
    }

    public static int getAppTheme() {
        int resId = R.style.AppBaseTheme;
        String themeName = PrefUtils.getString(PrefUtils.APP_THEME, "AppBaseTheme");
        if (themeName.equals("BlackTheme")) {
            resId = R.style.BlackTheme;
        } else if (themeName.equals("BlueTheme")) {
            resId = R.style.BlueTheme;
        } else if (themeName.equals("GreenTheme")) {
            resId = R.style.GreenTheme;
        } else if (themeName.equals("RedTheme")) {
            resId = R.style.RedTheme;
        } else if (themeName.equals("OrangeTheme")) {
            resId = R.style.OrangeTheme;
        } else if (themeName.equals("PinkTheme")) {
            resId = R.style.PinkTheme;
        } else if (themeName.equals("VioletTheme")) {
            resId = R.style.VioletTheme;
        }
        return resId;
    }

    static public int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MainApplication.getContext().getResources().getDisplayMetrics());
    }

    static public void addEmptyFooterView(ListView listView, int dp) {
        View view = new View(listView.getContext());
        view.setMinimumHeight(dpToPixel(dp));
        view.setClickable(true);
        listView.addFooterView(view);
    }

    static public void showMessage(@NonNull Activity activity, @StringRes int messageId) {
        showMessage(activity, activity.getString(messageId));
    }

    static public void showMessage(@NonNull Activity activity, @NonNull String message) {
        View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
        Snackbar snackbar = Snackbar.make((coordinatorLayout != null ? coordinatorLayout : activity.findViewById(android.R.id.content)), message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundResource(R.color.material_grey_900);
        snackbar.show();
    }

    static public Bitmap getFaviconBitmap(long feedId, Cursor cursor, int iconCursorPos) {
        Bitmap bitmap = UiUtils.FAVICON_CACHE.get(feedId);
        if (bitmap == null) {
            byte[] iconBytes = cursor.getBlob(iconCursorPos);
            if (iconBytes != null && iconBytes.length > 0) {
                bitmap = UiUtils.getScaledBitmap(iconBytes, 18);
                UiUtils.FAVICON_CACHE.put(feedId, bitmap);
            }
        }
        return bitmap;
    }

    static public Bitmap getScaledBitmap(byte[] iconBytes, int sizeInDp) {
        if (iconBytes != null && iconBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
            if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
                int bitmapSizeInDip = UiUtils.dpToPixel(sizeInDp);
                if (bitmap.getHeight() != bitmapSizeInDip) {
                    Bitmap tmp = bitmap;
                    bitmap = Bitmap.createScaledBitmap(tmp, bitmapSizeInDip, bitmapSizeInDip, false);
                    tmp.recycle();
                }

                return bitmap;
            }
        }

        return null;
    }

    public static void showDeleteFeedDialog(final Activity activity, String title, final long feedId) {
        new AlertDialog.Builder(activity) //
                .setIcon(android.R.drawable.ic_dialog_alert) //
                .setTitle(title) //
                .setMessage(R.string.question_delete_feed) //
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                ContentResolver cr = activity.getContentResolver();
                                cr.delete(FeedData.FeedColumns.CONTENT_URI(feedId), null, null);
                            }
                        }.start();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    public static void populateShareData(String url, String subject, Activity activity) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.app_name));
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.settings_app_share)));
    }

    public static int getColorAttrFromTheme(Context context, int attrId){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        int color = typedValue.data;
        return color;
    }
}
