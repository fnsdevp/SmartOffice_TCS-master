package com.hipla.smartoffice_tcs.services;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 3/29/2018.
 */

public class ScheduleETADetectionService extends JobService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, StringRequestListener {

    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private long FASTEST_INTERVAL = 10000; /* 10 secs */
    private JobParameters parameters;
    private boolean isLoadingData = false;
    private double latitude, longitude;
    private float DISPLACEMENT = 10;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public boolean onStartJob(JobParameters params) {
        parameters = params;
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                    String.format(getString(R.string.meeting_going_to_start), meetingDetail.getFromtime()), "Info");

            initializeLocationManager();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        stopLocationUpdates();

        return false;
    }

    private void getETA(double latitude, double longitude) {

        ScheduleETADetectionService.this.latitude = latitude;
        ScheduleETADetectionService.this.longitude = longitude;

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            String DESTINATION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + latitude + "," + longitude + "&destinations=" + CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG +
                    "&key=" + CONST.DISTANCE_API_KEY;


            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleETADetectionService.this, "upcomingMeetings",
                        DESTINATION_URL);
            }

        } catch (Exception ex) {

        } finally {
            isLoadingData = true;
        }
    }

    private void initializeLocationManager() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {

        switch (type) {
            case "upcomingMeetings":

                try {
                    JSONObject response = new JSONObject(result);
                    JSONArray rows = response.getJSONArray("rows");
                    JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

                    JSONObject elementOnject = elements.getJSONObject(0).getJSONObject("duration");

                    int timeTOReachInSeconds = elementOnject.getInt("value");
                    String timeTOReachInMins = elementOnject.optString("text");

                    String distanceInKm = elementOnject.optString("text");

                    if (timeTOReachInSeconds < CONST.TIME_BEFORE_DETECTION_HRS * 60 * 60) {
                        UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                        if (meetingDetail != null) {
                            Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getFromtime());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(meetingDateTime);

                            Calendar cal1 = Calendar.getInstance();
                            cal1.setTime(new Date());

                            long timeDiff = cal.getTimeInMillis() - cal1.getTimeInMillis();
                            int timeInSec = (int) timeDiff / 1000;
                            if (timeInSec < 0) {
                                return;
                            }

                            if(timeTOReachInSeconds<=timeInSec){

                                if(timeTOReachInSeconds<=(timeInSec-((CONST.EXTRA_TIME_BEFORE_DETECTION_MINS+5)*60))){
                                    //reschedule ETA

                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            String.format(getString(R.string.you_will_reach_your_destination_in_mins), "" + timeTOReachInMins), "Info");


                                    CONST.scheduleMeetingETAJob(getApplicationContext(),
                                            (timeInSec - (timeTOReachInSeconds + CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 60)) * 1000,
                                            CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                                }else{
                                    //start geofencing detec tion
                                    CONST.scheduleStartMeetingJob(getApplicationContext(), 1000,
                                            CONST.SCHEDULE_START_MEETING_JOB_ID);

                                    if(timeTOReachInSeconds<=timeInSec-(timeInSec-(CONST.EXTRA_TIME_BEFORE_DETECTION_MINS)*60)){
                                        // show you should start your metting is at .....
                                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                                getString(R.string.you_will_be_late), "Info");
                                    } else if(timeTOReachInSeconds<=timeInSec-((CONST.EXTRA_TIME_BEFORE_DETECTION_MINS-5)*60)){
                                        // show you are late should start your metting is at .....
                                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                                getString(R.string.you_are_running_late), "Info");
                                    }else{
                                        // please start to continune

                                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                                String.format(getString(R.string.you_will_reach_your_destination_in_mins), "" + timeTOReachInMins), "Info");

                                    }

                                }

                            }else{
                                //reschedule
                               // strat timer now

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_will_late_and_reschedule), "Info");

                                CONST.scheduleStartMeetingJob(getApplicationContext(), 1000,
                                        CONST.SCHEDULE_START_MEETING_JOB_ID);

                            }


                            /*//need to change logic at this place
                            if (timeInSec >= (timeTOReachInSeconds)) {

                                CONST.scheduleStartMeetingJob(getApplicationContext(), 1000,
                                        CONST.SCHEDULE_START_MEETING_JOB_ID);
                            } if ((timeInSec+ CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 60) <= (timeTOReachInSeconds)) {
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_are_running_late), "Info");

                                CONST.scheduleStartMeetingJob(getApplicationContext(), 1000,
                                        CONST.SCHEDULE_START_MEETING_JOB_ID);
                            } else {
                                CONST.scheduleStartMeetingJob(getApplicationContext(),
                                        (timeInSec - (timeTOReachInSeconds + CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 60)) * 1000,
                                        CONST.SCHEDULE_START_MEETING_JOB_ID);
                            }*/

                        }
                    } else {
                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                getString(R.string.you_will_late_and_reschedule), "Info");

                        CONST.scheduleStartMeetingJob(getApplicationContext(), 1000, CONST.SCHEDULE_START_MEETING_JOB_ID);
                    }

                    jobFinished(parameters, false);
                    //isLoadingData = false;

                } catch (Exception ex) {
                    ex.printStackTrace();

                    isLoadingData = false;
                    getETA(latitude, longitude);
                }
                break;
        }

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null && !isLoadingData) {
            getETA(mLocation.getLatitude(), mLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && !isLoadingData)
            getETA(location.getLatitude(), location.getLongitude());
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

}
