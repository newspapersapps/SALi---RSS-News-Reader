package sali.rss.news.feed.reader.android.app.activity;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;

import sali.rss.news.feed.reader.android.app.MainApplication;
import sali.rss.news.feed.reader.android.app.provider.FeedData;
import sali.rss.news.feed.reader.android.app.provider.FeedDataContentProvider;
import sali.rss.news.feed.reader.android.app.service.FetcherService;


public class ShareIntentFilter extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (intent.getType().equals("text/plain")) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);
            String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            if (url != null) {
                if (title == null)
                    title = url;
               // addBookmark(url, title);
                addFeed(url, title);
                Toast.makeText(getApplicationContext(), "Added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private void addBookmark(final String url, final String title) {
        CharSequence colors[] = new CharSequence[]{"Save web page", "Subscribe"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    addWebPage(url, title);
                } else if (which == 1) {
                    addFeed(url, title);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.show();
    }

    private void addFeed(String url, String title) {
        String finalName = title;
        if (title.equals(url)) {
            try {
                URI u = new URI(url);
                finalName = u.getHost();
            } catch (URISyntaxException ignore) {
            }
        }
        FeedDataContentProvider.addFeed(ShareIntentFilter.this, url, finalName, true);
    }

    private void addWebPage(String url, String title) {
        ContentValues values = new ContentValues();
        values.put(FeedData.EntryColumns.TITLE, title);
        values.put(FeedData.EntryColumns.LINK, url);
        values.put(FeedData.EntryColumns.IS_FAVORITE, true);
        values.put(FeedData.EntryColumns.IS_READ, false);
        values.put(FeedData.EntryColumns.FEED_ID, 0);

        ContentResolver cr = MainApplication.getContext().getContentResolver();
        Uri uri = cr.insert(FeedData.EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(0), values);

        long[] ids = new long[1];
        ids[0] = Long.valueOf(uri.getLastPathSegment());
        FetcherService.addEntriesToMobilize(ids);
    }
}
