package com.hipla.smartoffice_tcs.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.application.MainApplication;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.databinding.ActivitySplashBinding;
import com.hipla.smartoffice_tcs.model.ZoneInfo;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.utils.MarshmallowPermissionHelper;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private ActivitySplashBinding binding;
    private static final int ALL_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        setUpZoneData();
        initView();
    }

    private void initView() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                checkNavinginePermissions();

            }
        }, 2000L);

       // binding.content.startRippleAnimation();

    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , SplashActivity.this, ALL_PERMISSION)) {
                if (!MainApplication.isNavigineInitialized) {
                    (new InitTask(SplashActivity.this)).execute();
                } else {
                    if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }else{
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }
                }
            }
        } else {
            if (!MainApplication.isNavigineInitialized) {
                (new InitTask(SplashActivity.this)).execute();
            } else {
                if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                    startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //binding.content.stopRippleAnimation();
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
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d("AAAAAA", "Initialized!");
            if (!NavigineSDK.loadLocation(MainApplication.LOCATION_ID, 30)) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result.booleanValue()) {
                // Starting main activity

            } else {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }

            if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                supportFinishAfterTransition();
            }else{
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                supportFinishAfterTransition();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

    private void setUpZoneData() {
        Db_Helper db_helper = new Db_Helper(getApplicationContext());

        //For FNSPL Zone
        db_helper.insert_zone(new ZoneInfo(1,"Conference Room", "832,1131","21,23.5","25,23.5","25,17.8","21,17.8"));
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case ALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {

                    if (!MainApplication.isNavigineInitialized) {
                        (new InitTask(this)).execute();
                    }

                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

}
