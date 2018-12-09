package sali.rss.news.feed.reader.android.app.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import sali.rss.news.feed.reader.android.app.MainApplication;
import sali.rss.news.feed.reader.android.app.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class StringUtils {

    private static final DateFormat TIME_FORMAT = android.text.format.DateFormat.getTimeFormat(MainApplication.getContext());
    private static final int SIX_HOURS = 21600000; // six hours in milliseconds
    private static DateFormat DATE_SHORT_FORMAT = null;

    static {
        // getBestTimePattern() is only available in API 18 and up (Android 4.3 and better)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            DATE_SHORT_FORMAT = new SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(MainApplication.getContext().getResources().getConfiguration().locale, "d MMM"));
        } else {
            DATE_SHORT_FORMAT = android.text.format.DateFormat.getDateFormat(MainApplication.getContext());
        }
    }

    static public String getDateTimeString(long timestamp) {
        String outString;

        Date date = new Date(timestamp);
        Calendar calTimestamp = Calendar.getInstance();
        calTimestamp.setTimeInMillis(timestamp);
        Calendar calCurrent = Calendar.getInstance();

        if (calCurrent.getTimeInMillis() - timestamp < SIX_HOURS || calCurrent.get(Calendar.DAY_OF_MONTH) == calTimestamp.get(Calendar.DAY_OF_MONTH)) {
            outString = TIME_FORMAT.format(date);
        } else {
            outString = DATE_SHORT_FORMAT.format(date) + ' ' + TIME_FORMAT.format(date);
        }

        return outString;
    }

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static int getLanguageIdByCode(String code){
        String [] langs = MainApplication.getContext().getResources().getStringArray(R.array.feed_languages);
        for(int i=0;i<langs.length;i++){
            String lang = langs[i];
            if(lang.substring(0,2).equalsIgnoreCase(code)){
                return i;
            }
        }
        return 0;
    }
}
