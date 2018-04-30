package com.hipla.smartoffice_tcs.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hipla.smartoffice_tcs.application.MainApplication;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.model.UserDetails;
import com.hipla.smartoffice_tcs.model.ZoneInfo;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class MyNavigationService extends Service {

    private static final String TAG = "NAVIGINE.Demo";
    public static final String ERROR = "error";
    public static final String DEVICE_LOCATION = "deviceLocation";
    private static final int UPDATE_TIMEOUT = 3000;  // milliseconds
    private static final int ADJUST_TIMEOUT = 5000; // milliseconds
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private static final boolean ORIENTATION_ENABLED = true; // Show device orientation?

    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private boolean mAdjustMode = false;
    private long mErrorMessageTime = 0;
    private Location mLocation = null;
    private DeviceInfo mDeviceInfo = null; // Current device
    private Intent locatonFetch;
    private Intent errorMessage;
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();
    private int currentZone = 0;
    private UserDetails profile;
    private boolean isNotified = true;
    private boolean isMarkedAbsent = false;
    private boolean isClassLaunched = false;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private Db_Helper db_helper;

    public MyNavigationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(1002, new Notification());

        isClassLaunched = false;

        Paper.book().delete(DEVICE_LOCATION);

        if (MainApplication.isNavigineInitialized) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(mRunnable);
                }
            };
            mTimer.schedule(mTimerTask, UPDATE_TIMEOUT, UPDATE_TIMEOUT);
        } else {
            new InitTask(getApplicationContext()).execute();
        }

        db_helper = new Db_Helper(getApplicationContext());
        if (db_helper != null) {

            zoneInfos = db_helper.getAllZoneInfo();

            for (ZoneInfo zoneInfo :
                    zoneInfos) {
                zoneInfoPoint.add(convertToPoints(zoneInfo));
            }
        }

    }

    @Override
    public void onDestroy() {
        //MainApplication.finish();
        if (mTimerTask != null && mTimer != null) {
            mTimerTask.cancel();
            mTimer.cancel();
            mTimerTask = null;
            mTimer = null;
        }

        super.onDestroy();
    }

    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext = null;
        private String mErrorMsg = null;

        public InitTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!MainApplication.initialize(getApplicationContext())) {
                mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d(TAG, "Initialized!");
            if (!NavigineSDK.loadLocation(MainApplication.LOCATION_ID, 30)) {
                mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result.booleanValue()) {
                // Starting main activity

                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(mRunnable);
                    }
                };
                mTimer.schedule(mTimerTask, UPDATE_TIMEOUT, UPDATE_TIMEOUT);

            } else {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

    final Runnable mRunnable =
            new Runnable() {
                public void run() {
                    navigationCalculation();
                }
            };

    private void navigationCalculation() {
        if (MainApplication.Navigation == null) {
            Log.d(TAG, "Sorry, navigation is not supported on your device!");
            return;
        }

        final long timeNow = NavigineSDK.currentTimeMillis();

        if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
            mErrorMessageTime = 0;
        }

        // Start navigation if necessary
        if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
            MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

        // Get device info from NavigationThread
        mDeviceInfo = MainApplication.Navigation.getDeviceInfo();
        //Paper.book().write(DEVICE_LOCATION, mDeviceInfo);

        if (mDeviceInfo.errorCode == 0) {
            mErrorMessageTime = 0;

            calculateZone(mDeviceInfo);

        }
    }

    private void calculateZone(DeviceInfo mDeviceInfo) {
        Log.d(TAG, "X : " + mDeviceInfo.x + " Y: " + mDeviceInfo.y);
        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));

        }
    }

    private PointF[] convertToPoints(ZoneInfo zoneInfo) {
        PointF[] pointFs = new PointF[4];

        String[] pointsA = zoneInfo.getPointA().split(",");
        PointF pointA = new PointF(Float.parseFloat(pointsA[0]), Float.parseFloat(pointsA[1]));
        pointFs[0] = pointA;

        String[] pointsB = zoneInfo.getPointB().split(",");
        PointF pointB = new PointF(Float.parseFloat(pointsB[0]), Float.parseFloat(pointsB[1]));
        pointFs[1] = pointB;

        String[] pointsC = zoneInfo.getPointC().split(",");
        PointF pointC = new PointF(Float.parseFloat(pointsC[0]), Float.parseFloat(pointsC[1]));
        pointFs[2] = pointC;

        String[] pointsD = zoneInfo.getPointD().split(",");
        PointF pointD = new PointF(Float.parseFloat(pointsD[0]), Float.parseFloat(pointsD[1]));
        pointFs[3] = pointD;

        return pointFs;
    }

    public boolean contains(PointF[] points, PointF test) {
        int i;
        int j;
        boolean result = false;

        if (((points[0].x < test.x) && (points[0].y > test.y))) {
            if (((points[3].x < test.x) && (points[3].y < test.y))) {
                if (((points[1].x > test.x) && (points[1].y > test.y))) {
                    if (((points[2].x > test.x) && (points[2].y < test.y))) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

}
