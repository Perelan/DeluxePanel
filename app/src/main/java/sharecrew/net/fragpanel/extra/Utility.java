package sharecrew.net.fragpanel.extra;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {

    public static final String KEY = "fragpanel123";
    public static final String WEBSITE = "https://fragdeluxe.com/deluxepanel/";

    /**
     * This method is used to convert a input timestamp (default: current timestamp from sql), which is in
     * this format: yyyy-MM-dd HH:mm:ss (i.e: 2015-12-27 21:00:16) to find secs, mins, hours and days
     * since it were registered.
     * @param date input time to calculate
     * @return sec, min, hour days.
     */
    public static String convert_time(String date){
        try{
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Calendar c = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            Date d = df.parse(date);
            c.setTime(d);

            long diff        = (start.getTime().getTime() - c.getTime().getTime());
            long diffDays    = diff / (24 * 60 * 60 * 1000);
            long diffHours   = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffSeconds = diff / 1000 % 60;

            if(diffDays == 0 && diffHours == 0) {
                return String.format("%s mins, %s secs ago", diffMinutes, diffSeconds);
            }else if(diffDays == 0){
                return String.format("%s hrs, %s mins ago", diffHours, diffMinutes);
            }else{
                return String.format("%s days, %s hrs ago", diffDays, diffHours);
            }

        }catch(ParseException e){
            e.getStackTrace();
        }

        return "ERROR";
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * A method i took for the web to change the color of the values inside of the NumberPicker
     */
    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    Log.w("setNumberPickerText", e);
                }
            }
        }
        return false;
    }
}
