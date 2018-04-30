package com.hipla.smartoffice_tcs.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;

import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import java.util.Locale;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 2/2/2018.
 */

public class MainApplication extends Application{

    public static final String TAG           = "Navigine.Demo";
    public static final String SERVER_URL    = "https://api.navigine.com";
    //public static final String USER_HASH     = "C213-85E7-2265-3F4B";
    public static final String USER_HASH     = "0F17-DAE1-4D0A-1778";
    //public static final int LOCATION_ID   = 2409;
    public static final int LOCATION_ID   = 1894;

    public static NavigationThread Navigation    = null;
    public static boolean isNavigineInitialized = false;

    // Screen parameters
    public static float DisplayWidthPx            = 0.0f;
    public static float DisplayHeightPx           = 0.0f;
    public static float DisplayWidthDp            = 0.0f;
    public static float DisplayHeightDp           = 0.0f;
    public static float DisplayDensity            = 0.0f;

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Paper.init(getApplicationContext());

    }

    public static boolean initialize(Context context)
    {
        NavigineSDK.setParameter(context, "debug_level", 2);
        NavigineSDK.setParameter(context, "apply_server_config_enabled",  false);
        NavigineSDK.setParameter(context, "actions_updates_enabled",      false);
        NavigineSDK.setParameter(context, "location_updates_enabled",     true);
        NavigineSDK.setParameter(context, "location_update_timeout",      30);
        NavigineSDK.setParameter(context, "post_beacons_enabled",         true);
        NavigineSDK.setParameter(context, "post_messages_enabled",        true);
        if (!NavigineSDK.initialize(context, USER_HASH, SERVER_URL)) {
            return false;
        }else{
            isNavigineInitialized=true;
        }

        Navigation = NavigineSDK.getNavigation();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                DisplayWidthPx, DisplayHeightPx,
                DisplayWidthDp, DisplayHeightDp,
                DisplayDensity));

        return true;
    }

    public static void finish()
    {
        isNavigineInitialized=false;
        if (Navigation == null)
            return;

        NavigineSDK.finish();
        Navigation = null;
    }

}
