package sali.rss.news.feed.reader.android.app.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import sali.rss.news.feed.reader.android.app.Constants;
import sali.rss.news.feed.reader.android.app.R;
import sali.rss.news.feed.reader.android.app.adapter.FiltersCursorAdapter;
import sali.rss.news.feed.reader.android.app.loader.BaseLoader;
import sali.rss.news.feed.reader.android.app.provider.FeedData.FeedColumns;
import sali.rss.news.feed.reader.android.app.provider.FeedData.FilterColumns;
import sali.rss.news.feed.reader.android.app.provider.FeedDataContentProvider;
import sali.rss.news.feed.reader.android.app.utils.Dog;
import sali.rss.news.feed.reader.android.app.utils.NetworkUtils;
import sali.rss.news.feed.reader.android.app.utils.PrefUtils;
import sali.rss.news.feed.reader.android.app.utils.StringUtils;
import sali.rss.news.feed.reader.android.app.utils.UiUtils;

public class EditFeedActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TAB_FEED = 0;
    private static final int TAB_GOOGLE = 1;
    private static final int TAB_FILTERS = 2;

    static final String FEED_SEARCH_TITLE = "title";
    static final String FEED_SEARCH_URL = "feedId";
    static final String FEED_SEARCH_DESC = "description";
    private static final String STATE_CURRENT_TAB = "STATE_CURRENT_TAB";
    private static final String[] FEED_PROJECTION = new String[]{FeedColumns.NAME, FeedColumns.URL, FeedColumns.RETRIEVE_FULLTEXT, FeedColumns.IS_GROUP};
    private static final int[] TOPIC_NAME = new int[]{R.string.google_news_top_stories, R.string.google_news_world, R.string.google_news_business,
            R.string.google_news_technology, R.string.google_news_entertainment, R.string.google_news_sports, R.string.google_news_science, R.string.google_news_health};

    private static final String[] TOPIC_CODES = new String[]{null, "w", "b", "t", "e", "s", "snc", "m"};

    private static final int[] CB_IDS = new int[]{R.id.cb_top_stories, R.id.cb_world, R.id.cb_business, R.id.cb_technology, R.id.cb_entertainment,
            R.id.cb_sports, R.id.cb_science, R.id.cb_health};

    private final ActionMode.Callback mFilterActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.edit_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_edit:
                    Cursor c = mFiltersCursorAdapter.getCursor();
                    if (c.moveToPosition(mFiltersCursorAdapter.getSelectedFilter())) {
                        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_edit, null);
                        final EditText filterText = (EditText) dialogView.findViewById(R.id.filterText);
                        final CheckBox regexCheckBox = (CheckBox) dialogView.findViewById(R.id.regexCheckBox);
                        final RadioButton applyTitleRadio = (RadioButton) dialogView.findViewById(R.id.applyTitleRadio);
                        final RadioButton applyContentRadio = (RadioButton) dialogView.findViewById(R.id.applyContentRadio);
                        final RadioButton acceptRadio = (RadioButton) dialogView.findViewById(R.id.acceptRadio);
                        final RadioButton rejectRadio = (RadioButton) dialogView.findViewById(R.id.rejectRadio);

                        filterText.setText(c.getString(c.getColumnIndex(FilterColumns.FILTER_TEXT)));
                        regexCheckBox.setChecked(c.getInt(c.getColumnIndex(FilterColumns.IS_REGEX)) == 1);
                        if (c.getInt(c.getColumnIndex(FilterColumns.IS_APPLIED_TO_TITLE)) == 1) {
                            applyTitleRadio.setChecked(true);
                        } else {
                            applyContentRadio.setChecked(true);
                        }
                        if (c.getInt(c.getColumnIndex(FilterColumns.IS_ACCEPT_RULE)) == 1) {
                            acceptRadio.setChecked(true);
                        } else {
                            rejectRadio.setChecked(true);
                        }

                        final long filterId = mFiltersCursorAdapter.getItemId(mFiltersCursorAdapter.getSelectedFilter());
                        new AlertDialog.Builder(EditFeedActivity.this) //
                                .setTitle(R.string.filter_edit_title) //
                                .setView(dialogView) //
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                String filter = filterText.getText().toString();
                                                if (!filter.isEmpty()) {
                                                    ContentResolver cr = getContentResolver();
                                                    ContentValues values = new ContentValues();
                                                    values.put(FilterColumns.FILTER_TEXT, filter);
                                                    values.put(FilterColumns.IS_REGEX, regexCheckBox.isChecked());
                                                    values.put(FilterColumns.IS_APPLIED_TO_TITLE, applyTitleRadio.isChecked());
                                                    values.put(FilterColumns.IS_ACCEPT_RULE, acceptRadio.isChecked());
                                                    if (cr.update(FilterColumns.CONTENT_URI, values, FilterColumns._ID + '=' + filterId, null) > 0) {
                                                        cr.notifyChange(
                                                                FilterColumns.FILTERS_FOR_FEED_CONTENT_URI(getIntent().getData().getLastPathSegment()),
                                                                null);
                                                    }
                                                }
                                            }
                                        }.start();
                                    }
                                }).setNegativeButton(android.R.string.cancel, null).show();
                    }

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_delete:
                    final long filterId = mFiltersCursorAdapter.getItemId(mFiltersCursorAdapter.getSelectedFilter());
                    new AlertDialog.Builder(EditFeedActivity.this) //
                            .setIcon(android.R.drawable.ic_dialog_alert) //
                            .setTitle(R.string.filter_delete_title) //
                            .setMessage(R.string.question_delete_filter) //
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            ContentResolver cr = getContentResolver();
                                            if (cr.delete(FilterColumns.CONTENT_URI, FilterColumns._ID + '=' + filterId, null) > 0) {
                                                cr.notifyChange(FilterColumns.FILTERS_FOR_FEED_CONTENT_URI(getIntent().getData().getLastPathSegment()),
                                                        null);
                                            }
                                        }
                                    }.start();
                                }
                            }).setNegativeButton(android.R.string.no, null).show();

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFiltersCursorAdapter.setSelectedFilter(-1);
            mFiltersListView.invalidateViews();
        }
    };
    private TabHost mTabHost;
    private EditText mNameEditText, mUrlEditText, mKeywordEditText;
    private ListView mFiltersListView;
    private FiltersCursorAdapter mFiltersCursorAdapter;
    private Spinner languages;
    private Spinner languagesG;
    private LinearLayout feedLangContainer,feedManualContainer,feedSearchContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mNameEditText = (EditText) findViewById(R.id.feed_title);
        mUrlEditText = (EditText) findViewById(R.id.feed_url);
        mKeywordEditText = (EditText) findViewById(R.id.feed_keyword);
        mFiltersListView = (ListView) findViewById(android.R.id.list);
        languages = (Spinner) findViewById(R.id.languages);
        languagesG = (Spinner) findViewById(R.id.languagesG);
        feedLangContainer = (LinearLayout)findViewById(R.id.feed_lang_container);
        feedManualContainer = (LinearLayout)findViewById(R.id.feed_manual);
        feedSearchContainer = (LinearLayout)findViewById(R.id.feed_search);
        setDefaultLanguage();

        mTabHost.setup();
        mTabHost.addTab(mTabHost.newTabSpec("feedTab").setIndicator(getString(R.string.tab_feed_title)).setContent(R.id.feed_tab));
        mTabHost.addTab(mTabHost.newTabSpec("googleTab").setIndicator(getString(R.string.tab_google_title)).setContent(R.id.google_tab));
        mTabHost.addTab(mTabHost.newTabSpec("filtersTab").setIndicator(getString(R.string.tab_filters_title)).setContent(R.id.filters_tab));

        mKeywordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    validateFeed();
                    return true;
                }
                return false;
            }
        });

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                setDefaultLanguage();
                invalidateOptionsMenu();
            }
        });

        if (savedInstanceState != null) {
            mTabHost.setCurrentTab(savedInstanceState.getInt(STATE_CURRENT_TAB));
        }

        if (intent.getAction().equals(Intent.ACTION_INSERT) || intent.getAction().equals(Intent.ACTION_SEND)) {
            setTitle(R.string.new_feed_title);

            mTabHost.getTabWidget().getChildTabViewAt(TAB_FILTERS).setVisibility(View.GONE);
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                feedSearchContainer.setVisibility(View.GONE);
                mUrlEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
            else{
                feedManualContainer.setVisibility(View.GONE);
            }
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            setTitle(R.string.new_feed_title);

            mTabHost.getTabWidget().getChildTabViewAt(TAB_FILTERS).setVisibility(View.GONE);
            feedSearchContainer.setVisibility(View.GONE);

            mUrlEditText.setText(intent.getDataString());
        } else if (intent.getAction().equals(Intent.ACTION_EDIT)) {
            setTitle(R.string.edit_feed_title);

            mTabHost.getTabWidget().getChildTabViewAt(TAB_GOOGLE).setVisibility(View.GONE);
            feedLangContainer.setVisibility(View.GONE);
            feedSearchContainer.setVisibility(View.GONE);

            mFiltersCursorAdapter = new FiltersCursorAdapter(this, Constants.EMPTY_CURSOR);
            mFiltersListView.setAdapter(mFiltersCursorAdapter);
            mFiltersListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    startSupportActionMode(mFilterActionModeCallback);
                    mFiltersCursorAdapter.setSelectedFilter(position);
                    mFiltersListView.invalidateViews();
                    return true;
                }
            });

            getLoaderManager().initLoader(0, null, this);

            if (savedInstanceState == null) {
                Cursor cursor = getContentResolver().query(intent.getData(), FEED_PROJECTION, null, null, null);

                if (cursor != null && cursor.moveToNext()) {
                    mNameEditText.setText(cursor.getString(0));
                    mUrlEditText.setText(cursor.getString(1));
                    if (cursor.getInt(3) == 1) { // if it's a group, we cannot edit it
                        finish();
                    }
                } else {
                    UiUtils.showMessage(EditFeedActivity.this, R.string.error);
                    finish();
                }

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_TAB, mTabHost.getCurrentTab());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            String url = mUrlEditText.getText().toString();
            ContentResolver cr = getContentResolver();

            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(FeedColumns.CONTENT_URI, FeedColumns.PROJECTION_ID,
                        FeedColumns.URL + Constants.DB_ARG, new String[]{url}, null);

                if (cursor != null && cursor.moveToFirst() && !getIntent().getData().getLastPathSegment().equals(cursor.getString(0))) {
                    UiUtils.showMessage(EditFeedActivity.this, R.string.error_feed_url_exists);
                } else {
                    ContentValues values = new ContentValues();

                    if (!url.startsWith(Constants.HTTP_SCHEME) && !url.startsWith(Constants.HTTPS_SCHEME)) {
                        url = Constants.HTTP_SCHEME + url;
                    }
                    values.put(FeedColumns.URL, url);

                    String name = mNameEditText.getText().toString();

                    values.put(FeedColumns.NAME, name.trim().length() > 0 ? name : null);
                    values.put(FeedColumns.RETRIEVE_FULLTEXT, 1);
                    values.put(FeedColumns.FETCH_MODE, 0);
                    values.putNull(FeedColumns.ERROR);

                    cr.update(getIntent().getData(), values, null, null);
                }
            } catch (Exception ignored) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_feed, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String action = getIntent() == null ? "" : getIntent().getAction();
        if (mTabHost.getCurrentTab() == TAB_FILTERS) {
            menu.findItem(R.id.menu_add_filter).setVisible(true);
        } else {
            menu.findItem(R.id.menu_add_filter).setVisible(false);
        }

        if (action.equals(Intent.ACTION_EDIT)) {
            menu.findItem(R.id.menu_validate).setVisible(false); // only in insert mode
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_validate: // only in insert mode
                if (mTabHost.getCurrentTab() == TAB_FEED) {
                    validateFeed();
                } else {
                    validateGoogle();
                }
                return true;
            case R.id.menu_add_filter: {
                validateFilter();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDefaultLanguage() {
        String lang = PrefUtils.getString(PrefUtils.FEEDS_LANG, Locale.getDefault().getLanguage());
        int langId = StringUtils.getLanguageIdByCode(lang);
        languages.setSelection(langId);
        languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] langs = getResources().getStringArray(R.array.feed_languages);
                PrefUtils.putString(PrefUtils.FEEDS_LANG, langs[i].substring(0, 2).toLowerCase());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        languagesG.setSelection(langId);
        languagesG.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] langs = getResources().getStringArray(R.array.feed_languages);
                PrefUtils.putString(PrefUtils.FEEDS_LANG, langs[i].substring(0, 2).toLowerCase());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void validateGoogle() {
        for (int topic = 0; topic < TOPIC_NAME.length; topic++) {
            if (((CheckBox) findViewById(CB_IDS[topic])).isChecked()) {
                String url = "http://news.google.com/news?hl=" + PrefUtils.getString(PrefUtils.FEEDS_LANG, Locale.getDefault().getLanguage()) + "&output=rss";
                if (TOPIC_CODES[topic] != null) {
                    url += "&topic=" + TOPIC_CODES[topic];
                }
                FeedDataContentProvider.addFeed(this, url, getString(TOPIC_NAME[topic]), true);
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void validateFilter() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_edit, null);

        new AlertDialog.Builder(this) //
                .setTitle(R.string.filter_add_title) //
                .setView(dialogView) //
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String filterText = ((EditText) dialogView.findViewById(R.id.filterText)).getText().toString();
                        if (filterText.length() != 0) {
                            String feedId = getIntent().getData().getLastPathSegment();

                            ContentValues values = new ContentValues();
                            values.put(FilterColumns.FILTER_TEXT, filterText);
                            values.put(FilterColumns.IS_REGEX, ((CheckBox) dialogView.findViewById(R.id.regexCheckBox)).isChecked());
                            values.put(FilterColumns.IS_APPLIED_TO_TITLE, ((RadioButton) dialogView.findViewById(R.id.applyTitleRadio)).isChecked());
                            values.put(FilterColumns.IS_ACCEPT_RULE, ((RadioButton) dialogView.findViewById(R.id.acceptRadio)).isChecked());

                            ContentResolver cr = getContentResolver();
                            cr.insert(FilterColumns.FILTERS_FOR_FEED_CONTENT_URI(feedId), values);
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        }).show();
    }

    private void validateFeed() {
        final String name = mNameEditText.getText().toString().trim();
        final String urlOrSearch = feedManualContainer.getVisibility() == View.GONE ? mKeywordEditText.getText().toString().trim() :  mUrlEditText.getText().toString().trim();

        if (urlOrSearch.isEmpty()) {
            UiUtils.showMessage(EditFeedActivity.this, R.string.error_feed_error);
        }

        if (!urlOrSearch.contains(".") || !urlOrSearch.contains("/") || urlOrSearch.contains(" ")) {
            final ProgressDialog pd = new ProgressDialog(EditFeedActivity.this);
            pd.setMessage(getString(R.string.loading));
            pd.setCancelable(true);
            pd.setIndeterminate(true);
            pd.show();

            getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>>() {

                @Override
                public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int id, Bundle args) {
                    String encodedSearchText = urlOrSearch;
                    try {
                        encodedSearchText = URLEncoder.encode(urlOrSearch, Constants.UTF8);
                    } catch (UnsupportedEncodingException ignored) {
                    }

                    return new GetFeedSearchResultsLoader(EditFeedActivity.this, encodedSearchText);
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<HashMap<String, String>>> loader, final ArrayList<HashMap<String, String>> data) {
                    pd.cancel();

                    if (data == null) {
                        UiUtils.showMessage(EditFeedActivity.this, R.string.error);
                    } else if (data.isEmpty()) {
                        UiUtils.showMessage(EditFeedActivity.this, R.string.no_result);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditFeedActivity.this);
                        builder.setTitle(R.string.feed_search);

                        // create the grid item mapping
                        String[] from = new String[]{FEED_SEARCH_TITLE, FEED_SEARCH_DESC};
                        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

                        // fill in the grid_item layout
                        SimpleAdapter adapter = new SimpleAdapter(EditFeedActivity.this, data, R.layout.item_search_result, from,
                                to);
                        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FeedDataContentProvider.addFeed(EditFeedActivity.this, data.get(which).get(FEED_SEARCH_URL), name.isEmpty() ? data.get(which).get(FEED_SEARCH_TITLE) : name, true);

                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                        builder.show();
                    }
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<HashMap<String, String>>> loader) {
                }
            });
        } else {
            String finalName = name;
            if(finalName.isEmpty()){
               try {
                   URI u = new URI(urlOrSearch);
                   finalName = u.getHost();
               }catch (URISyntaxException ignore){}
            }
            FeedDataContentProvider.addFeed(EditFeedActivity.this, urlOrSearch, finalName, true);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this, FilterColumns.FILTERS_FOR_FEED_CONTENT_URI(getIntent().getData().getLastPathSegment()),
                null, null, null, FilterColumns.IS_ACCEPT_RULE + Constants.DB_DESC);
        cursorLoader.setUpdateThrottle(Constants.UPDATE_THROTTLE_DELAY);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFiltersCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFiltersCursorAdapter.swapCursor(Constants.EMPTY_CURSOR);
    }
}

/**
 * A custom Loader that loads feed search results from the feedly WS.
 */
class GetFeedSearchResultsLoader extends BaseLoader<ArrayList<HashMap<String, String>>> {
    private final String mSearchText;

    public GetFeedSearchResultsLoader(Context context, String searchText) {
        super(context);
        mSearchText = searchText;
    }

    /**
     * This is where the bulk of our work is done. This function is called in a background thread and should generate a new set of data to be
     * published by the loader.
     */
    @Override
    public ArrayList<HashMap<String, String>> loadInBackground() {
        try {
            HttpURLConnection conn = NetworkUtils.setupConnection("http://feedly.com/v3/search/feeds?count=20&locale="+PrefUtils.getString(PrefUtils.FEEDS_LANG, Locale.getDefault().getLanguage())+"&query=" + mSearchText);
            try {
                final ArrayList<HashMap<String, String>> results = new ArrayList<>();
                addGoogleFeed(results);
                String jsonStr = new String(NetworkUtils.getBytes(conn.getInputStream()));
                // Parse results
                JSONArray entries = new JSONObject(jsonStr).getJSONArray("results");
                for (int i = 0; i < entries.length(); i++) {
                    try {
                        JSONObject entry = (JSONObject) entries.get(i);
                        String url = entry.get(EditFeedActivity.FEED_SEARCH_URL).toString();
                        if (!url.isEmpty()) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(EditFeedActivity.FEED_SEARCH_TITLE, Html.fromHtml(entry.get(EditFeedActivity.FEED_SEARCH_TITLE).toString())
                                    .toString());
                            map.put(EditFeedActivity.FEED_SEARCH_URL, url.replaceFirst("feed/", ""));
                            //map.put(EditFeedActivity.FEED_SEARCH_DESC, Html.fromHtml(entry.get(EditFeedActivity.FEED_SEARCH_DESC).toString()).toString());
                            map.put(EditFeedActivity.FEED_SEARCH_DESC, map.get(EditFeedActivity.FEED_SEARCH_URL));

                            results.add(map);
                        }
                    } catch (Exception ignored) {
                    }
                }

                return results;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Dog.e("Error", e);
            return null;
        }
    }

    private void addGoogleFeed(ArrayList<HashMap<String, String>> results) throws UnsupportedEncodingException {
        String url = "http://news.google.com/news?hl=" + PrefUtils.getString(PrefUtils.FEEDS_LANG, Locale.getDefault().getLanguage()) + "&output=rss&q=" + URLEncoder.encode(mSearchText, "UTF-8");

        HashMap<String, String> map = new HashMap<>();
        map.put(EditFeedActivity.FEED_SEARCH_TITLE, "Google News: " + mSearchText);
        map.put(EditFeedActivity.FEED_SEARCH_URL, url);
        map.put(EditFeedActivity.FEED_SEARCH_DESC, map.get(EditFeedActivity.FEED_SEARCH_URL));

        results.add(map);
    }
}
