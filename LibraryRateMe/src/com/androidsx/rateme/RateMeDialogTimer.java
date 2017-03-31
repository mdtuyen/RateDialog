package com.androidsx.rateme;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

/**
 * Timer to schedule the rate-me after a number of application launches.
 */
public class RateMeDialogTimer {
    private static final String TAG = RateMeDialogTimer.class.getSimpleName();
    
    private static final String PREF_NAME = "RateThisApp";
    private static final String KEY_INSTALL_DATE = "rta_install_date";
    private static final String KEY_LAUNCH_TIMES = "rta_launch_times";
    private static final String KEY_OPT_OUT = "rta_opt_out";

    private static Date mInstallDate = new Date();
    private static int mLaunchTimes = 0;
    private static boolean mOptOut = false;

    /**
     * Note to pre-1.2 users: installDate and launchTimes are now parameters in
     * {@link #shouldShowRateDialog}.
     */
    public RateMeDialogTimer() {
        // Intentionally empty. See the javadoc comment
    }

    public static void onStart(Context context, Bundle savedInstanceState) {
        // Only use FIRST launch of the activity
        if (savedInstanceState != null) {
            return;
        }
        saveInPreferences(context);
    }

    public static void onStart(Context context) {
        saveInPreferences(context);
    }

    private static void saveInPreferences(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
            Date now = new Date();
            editor.putLong(KEY_INSTALL_DATE, now.getTime());
            Log.d(TAG, "First install: " + now.toString());
        }
        // Increment launch times
        int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        launchTimes++;
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
        Log.d(TAG, "Launch times; " + launchTimes);

        editor.apply();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
    }

    public static boolean shouldShowRateDialog(final Context context, int installDays, int launchTimes) {
        if (mOptOut) {
            return false;
        } else {
            if (mLaunchTimes >= launchTimes) {
                clearSharedPreferences(context);
                return true;
            }
            final long thresholdMillis = installDays * 24 * 60 * 60 * 1000L;
            if (new Date().getTime() - mInstallDate.getTime() >= thresholdMillis) {
                clearSharedPreferences(context);
                return true;
            } else {
                return false;
            }
        }
    }

    public static void clearSharedPreferences(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .remove(KEY_INSTALL_DATE)
                .remove(KEY_LAUNCH_TIMES)
                .apply();
    }

    /**
     * Set opt out flag. If it is true, the rate dialog will never shown unless app data is cleared.
     */
    public static void setOptOut(final Context context, boolean optOut) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_OPT_OUT, optOut)
                .apply();
    }

    public static boolean wasRated(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_OPT_OUT, false);
    }
}
