package sali.rss.news.feed.reader.android.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import sali.rss.news.feed.reader.android.app.Constants;
import sali.rss.news.feed.reader.android.app.MainApplication;
import sali.rss.news.feed.reader.android.app.R;
import sali.rss.news.feed.reader.android.app.service.AutoRefreshService;
import sali.rss.news.feed.reader.android.app.utils.PrefUtils;
import sali.rss.news.feed.reader.android.app.utils.UiUtils;

public class GeneralPrefsFragment extends PreferenceFragment {

    private Preference.OnPreferenceChangeListener mOnRefreshChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Activity activity = getActivity();
            if (activity != null) {
                AutoRefreshService.initAutoRefresh(activity);
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        setRingtoneSummary();


        Preference preference = findPreference(PrefUtils.REFRESH_ENABLED);
        preference.setOnPreferenceChangeListener(mOnRefreshChangeListener);
        preference = findPreference(PrefUtils.REFRESH_INTERVAL);
        preference.setOnPreferenceChangeListener(mOnRefreshChangeListener);

        preference = findPreference(PrefUtils.APP_THEME);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PrefUtils.putString(PrefUtils.APP_THEME, String.valueOf(newValue));
                android.os.Process.killProcess(android.os.Process.myPid()); // Restart the app
                // this return statement will never be reached
                return true;
            }
        });

        preference = findPreference(PrefUtils.APP_RATE);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PLAY_STORE_URI + getActivity().getPackageName()));
                startActivity(i);
                return false;
            }
        });

        preference = findPreference(PrefUtils.APP_SHARE);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                UiUtils.populateShareData(Constants.PLAY_STORE_URL + getActivity().getPackageName(),getActivity().getString(R.string.app_name), getActivity());
                return false;
            }
        });

        preference = findPreference(PrefUtils.DEV_PAGE);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DEV_PAGE_URI));
                startActivity(i);
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        // The ringtone summary text should be updated using
        // OnSharedPreferenceChangeListener(), but I can't get it to work.
        // Updating in onResume is a very simple hack that seems to work, but is inefficient.
        setRingtoneSummary();

        super.onResume();

    }

    private void setRingtoneSummary() {
        Preference ringtone_preference = findPreference(PrefUtils.NOTIFICATIONS_RINGTONE);
        Uri ringtoneUri = Uri.parse(PrefUtils.getString(PrefUtils.NOTIFICATIONS_RINGTONE, ""));
        if (TextUtils.isEmpty(ringtoneUri.toString())) {
            ringtone_preference.setSummary(R.string.settings_notifications_ringtone_none);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(MainApplication.getContext(), ringtoneUri);
            if (ringtone == null) {
                ringtone_preference.setSummary(R.string.settings_notifications_ringtone_none);
            } else {
                ringtone_preference.setSummary(ringtone.getTitle(MainApplication.getContext()));
            }
        }
    }
}
