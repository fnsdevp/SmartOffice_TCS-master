package com.hipla.smartoffice_tcs.services;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
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
import com.hipla.smartoffice_tcs.database.Db_Helper;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 3/29/2018.
 */

public class ScheduleMeetingProcessService extends JobService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, StringRequestListener {

    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5000;  /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 10 secs */
    private JobParameters parameters;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private boolean isLoadingData = false, isAbleToReach = false;
    private double latitude, longitude;
    private UpcomingMeetings meetingDetail;
    private float DISPLACEMENT = 1;
    private boolean isJobFinished = false;
    private boolean is200MNotificationShown = false, is400MNotificationShown = false, isWelcomeNotificationShown = false, isLateNotificationShown = false;
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private int notificationCount = 0;


    @Override
    public boolean onStartJob(JobParameters params) {
        parameters = params;
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

            meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (!isLoadingData) {
                            Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getFromtime());

                            Date meetingDateTimeEnd = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getTotime());

                            if (new Date().compareTo(meetingDateTime) <= 0) {
                                getETA(latitude, longitude);
                            }

                            if (new Date().compareTo(meetingDateTimeEnd) > 0) {
                                stopLocationUpdates();

                                mTimer.cancel();
                                mTimerTask.cancel();

                                CONST.scheduleEndMeetingJob(ScheduleMeetingProcessService.this, 1300, CONST.SCHEDULE_END_MEETING_JOB_ID);

                                jobFinished(parameters, false);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            mTimer.schedule(mTimerTask, 1 * 60 * 1000, 5 * 60 * 1000);

            initializeLocationManager();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        stopLocationUpdates();

        mTimer.cancel();
        mTimerTask.cancel();

        return false;
    }

    private void getETA(double latitude, double longitude) {

        ScheduleMeetingProcessService.this.latitude = latitude;
        ScheduleMeetingProcessService.this.longitude = longitude;

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            String DESTINATION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + latitude + "," + longitude + "&destinations=" + CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG +
                    "&key=" + CONST.DISTANCE_API_KEY;


            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleMeetingProcessService.this, "upcomingMeetings",
                        DESTINATION_URL);
            }

        } catch (Exception ex) {

        } finally {
            isLoadingData = true;
        }

    }

    private void setGeoFencing(String latitude, String longitude) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", "" + userData.getId());
                requestParameter.put("lat", "" + latitude);
                requestParameter.put("lng", "" + longitude);

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleMeetingProcessService.this, "registerGeoFencing",
                        NetworkUtility.BASEURL + NetworkUtility.REGISTER_GEO_FENCING);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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
                    int timeInSec = 0;

                    JSONObject elementOnject = elements.getJSONObject(0).getJSONObject("duration");
                    JSONObject distanceObj = elements.getJSONObject(0).getJSONObject("duration");

                    int timeTOReachInSeconds = elementOnject.getInt("value");
                    String timeTOReachInMins = elementOnject.getString("text");

                    String distanceInKm = elementOnject.getString("text");

                    UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                    if (meetingDetail != null) {
                        Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                + " " + meetingDetail.getFromtime());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);

                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(new Date());

                        long timeDiff = cal.getTimeInMillis() - cal1.getTimeInMillis();
                        timeInSec = (int) timeDiff / 1000;

                    }

                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (timeTOReachInSeconds < timeInSec && userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                        isAbleToReach = true;

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                String.format(getString(R.string.you_will_reach_your_destination_in_mins), "" + timeTOReachInMins), "Info");

                        double distance = CONST.distance(latitude, longitude,
                                CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                        if (distance >= .200 && distance < .400 && !is400MNotificationShown) {

                            setGeoFencing("" + latitude, "" + longitude);

                            if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        String.format(getString(R.string.you_will_reach_400m), "" + (int) (distance * 1000)), "Info");

                            is400MNotificationShown = true;

                        } else if (distance > .03 && distance < .200 && !is200MNotificationShown) {

                            setGeoFencing("" + latitude, "" + longitude);

                            if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_will_reach_200m), "Info");

                            is200MNotificationShown = true;

                        } else if (distance < .03 && !isWelcomeNotificationShown) {
                            //start indoor navigation detection

                            setGeoFencing("" + latitude, "" + longitude);

                            CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                    getString(R.string.welcome_message), "Info");

                            isWelcomeNotificationShown = true;

                            Paper.book().write(CONST.IS_DETECTION_STARTED, true);

                            if (meetingDetail != null) {
                                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(meetingDateTime);
                                cal.set(Calendar.MINUTE, CONST.TIME_BEFORE_EXTEND_MEETING_IN_MIN);

                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date());

                                CONST.scheduleExtendMeetingJob(getApplicationContext(),
                                        cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                            }

                            Intent intent = new Intent();
                            intent.setAction("intent.start.Navigation");
                            sendBroadcast(intent);

                            isJobFinished = true;
                            jobFinished(parameters, false);
                        }
                    } else {
                        //show notification for late for meeting
                        notificationCount++;

                        if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false)) {
                            if (notificationCount == 1) {
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_will_be_late), "Info");
                            } else if (notificationCount == 2) {
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_are_running_late), "Info");
                            } else {
                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_will_late_and_reschedule), "Info");
                            }
                        }
                        //jobFinished(parameters, false);
                    }

                    isLoadingData = false;

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

        try {
            if (mLocation != null && !isJobFinished) {
                double distance = CONST.distance(mLocation.getLatitude(), mLocation.getLongitude(),
                        CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                ScheduleMeetingProcessService.this.latitude = mLocation.getLatitude();
                ScheduleMeetingProcessService.this.longitude = mLocation.getLongitude();

                if (isAbleToReach) {
                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (meetingDetail != null && userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                        Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                        if (new Date().compareTo(meetingDateTime) < 0) {
                            if (distance >= .200 && distance < .400 && !is400MNotificationShown) {

                                setGeoFencing("" + latitude, "" + longitude);

                                if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            String.format(getString(R.string.you_will_reach_400m), "" + (int) (distance * 1000)), "Info");

                                is400MNotificationShown = true;

                            } else if (distance > .03 && distance < .200 && !is200MNotificationShown) {

                                setGeoFencing("" + latitude, "" + longitude);

                                if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_will_reach_200m), "Info");

                                is200MNotificationShown = true;

                            } else if (distance < .03 && !isWelcomeNotificationShown) {
                                //start indoor navigation detection
                                setGeoFencing("" + latitude, "" + longitude);

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.welcome_message), "Info");

                                isWelcomeNotificationShown = true;

                                Paper.book().write(CONST.IS_DETECTION_STARTED, true);

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(meetingDateTime);
                                cal.set(Calendar.MINUTE, -10);

                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date());

                                CONST.scheduleExtendMeetingJob(getApplicationContext(),
                                        cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);

                                Intent intent = new Intent();
                                intent.setAction("intent.start.Navigation");
                                sendBroadcast(intent);

                                isJobFinished = true;
                                jobFinished(parameters, false);
                            }

                        }
                    }
                }

            } else {
                if (mLocation != null)
                    stopLocationUpdates();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        try {
            if (location != null && !isJobFinished) {
                double distance = CONST.distance(location.getLatitude(), location.getLongitude(),
                        CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                ScheduleMeetingProcessService.this.latitude = location.getLatitude();
                ScheduleMeetingProcessService.this.longitude = location.getLongitude();

                if (isAbleToReach) {
                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (meetingDetail != null && userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                        Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                        if (new Date().compareTo(meetingDateTime) < 0) {
                            if (distance >= .200 && distance < .400 && !is400MNotificationShown) {

                                setGeoFencing("" + latitude, "" + longitude);

                                if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            String.format(getString(R.string.you_will_reach_400m), "" + (int) (distance * 1000)), "Info");

                                is400MNotificationShown = true;

                            } else if (distance > .03 && distance < .200 && !is200MNotificationShown) {

                                setGeoFencing("" + latitude, "" + longitude);

                                if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, false))
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_will_reach_200m), "Info");

                                is200MNotificationShown = true;

                            } else if (distance < .03 && !isWelcomeNotificationShown) {
                                //start indoor navigation detection
                                setGeoFencing("" + latitude, "" + longitude);

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.welcome_message), "Info");

                                isWelcomeNotificationShown = true;

                                Paper.book().write(CONST.IS_DETECTION_STARTED, true);

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(meetingDateTime);
                                cal.set(Calendar.MINUTE, -10);

                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date());

                                CONST.scheduleExtendMeetingJob(getApplicationContext(),
                                        cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);

                                Intent intent = new Intent();
                                intent.setAction("intent.start.Navigation");
                                sendBroadcast(intent);

                                isJobFinished = true;
                                jobFinished(parameters, false);
                            }
                        }

                    }

                }

            } else {
                if (location != null)
                    stopLocationUpdates();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkForUpcomingMeeting() {
        try {
            Date currentDateTime = new Date();

            Db_Helper db_helper = new Db_Helper(getApplicationContext());
            List<UpcomingMeetings> upcomingMeetingsList = db_helper.getUpcomingMeetings(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            for (int i = 0; i < upcomingMeetingsList.size(); i++) {

                Date meetingDateTime = dateFormat.parse(upcomingMeetingsList.get(i).getFdate()
                        + " " + upcomingMeetingsList.get(i).getFromtime());

                Date meetingDateTimeEnd = dateFormat.parse(upcomingMeetingsList.get(i).getFdate()
                        + " " + upcomingMeetingsList.get(i).getFromtime());

                if (currentDateTime.compareTo(meetingDateTime) > 0) {

                    if (currentDateTime.compareTo(meetingDateTimeEnd) < 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);
                        cal.set(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                        CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(), CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                        Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));

                        break;
                    }

                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTime);
                    cal.set(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                    CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(), CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                    Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));

                    break;
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
