package com.hipla.smartoffice_tcs.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.services.FetchMeetingInfoservice;
import com.hipla.smartoffice_tcs.services.ScheduleETADetectionService;
import com.hipla.smartoffice_tcs.services.ScheduleMeetingExtendService;
import com.hipla.smartoffice_tcs.services.ScheduleMeetingFinishService;
import com.hipla.smartoffice_tcs.services.ScheduleMeetingProcessService;

/**
 * Created by FNSPL on 2/9/2018.
 */

public class CONST {

    public static final String MQTT_BROKER_URL = "tcp://192.168.0.111:1883";
    public static final String PUBLISH_TOPIC = "/cmnd/bld1/flr1/rom1/unt1/power1";
    public static final String CLIENT_ID = "androidkt";

    public static final String HOME_FRAGMENT = "homeFragment";
    public static final String SCHEDULE_MEETING = "scheduleMeeting";
    public static final String FIXED_MEETING = "fixedMeeting";
    public static final String CONTACT_DIALOG = "contactDialog";
    public static final String FLEXIBLE_MEETING = "flexibleMeeting";
    public static final String MANAGE_MEETING = "manageMeetings";
    public static final String APPOINTMENT_DATA = "appointmentData";
    public static final String MEETING_DETAILS = "meetingDetails";
    public static final String REQUESTED_MEETING_DETAILS = "requestedMeetingDetails";
    public static final String INDBOX_MESSGAES = "indboxMessages";
    public static final String PROFILE_MANAGMENT = "profileManagement";
    public static final String ORDER_FOOD = "orderfood";
    public static final String DISTANCE_NOTIFICATION = "distanceNotification";
    public static final String ABOUT_US = "aboutUs";
    public static final String SET_AVAIBILITY = "setAvaibility";
    public static final String CURRENT_MEETING_DATA = "currentMeetingData";
    public static final int TIME_BEFORE_DETECTION = -2;
    public static final int TIME_BEFORE_DETECTION_HRS = 2;
    public static final int EXTRA_TIME_BEFORE_DETECTION_MINS = 10;
    public static final int TIME_BEFORE_EXTEND_MEETING_IN_MIN = -10;
    public static final int TIME_BEFORE_CANCEL_MEETING_IN_MIN = -30;
    public static final int TIME_AFTER_MEETING_IN_SEC = 300;
    public static final String IS_DETECTION_STARTED = "isDetectionStarted";
    public static final String NEEDS_TO_RESCHEDULE = "needsToReschedule";
    public static final String SET_NOTIFICATION = "notificationFragment";
    public static final String NEW_MESSAGE = "newMessage";
    public static final String ZONE_ID = "zoneId";
    public static final String POINTY = "pointY";
    public static final String POINTX = "pointX";

    public static double DESTINATION_LAT = 22.5732655;
    public static double DESTINATION_LONG = 88.4527053;

    public static String DISTANCE_API_KEY = "AIzaSyDl-e82qPIBdn8JUks3jyOreYREtvavutw";

    //units are M(Miles), K(Kilometers), N(Nautical Miles)
    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    //::	This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    //::	This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    public static int FETCH_JOB_ID = 100;
    public static int SCHEDULE_ETA_DETECTION_JOB_ID = 101;
    public static int SCHEDULE_START_MEETING_JOB_ID = 102;
    public static int SCHEDULE_EXTEND_MEETING_JOB_ID = 103;
    public static int SCHEDULE_END_MEETING_JOB_ID = 104;

    public static void scheduleMeetingFetchJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, FetchMeetingInfoservice.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(timer); // wait at least
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleMeetingETAJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleETADetectionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(timer); // wait at least
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleStartMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingProcessService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(timer); // wait at least
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleExtendMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingExtendService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(timer); // wait at least
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleEndMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingFinishService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(timer); // wait at least
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void cancelEndMeetingJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingFinishService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void cancelMeetingETAJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleETADetectionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void cancelStartMeetingJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingProcessService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static boolean checkVersion(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
            return true;
        }else{
            return false;
        }
    }

    static String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

    public static void showNotifications(Context context, String title,String content,String info){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(""+title)
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(content)
                .setContentInfo(info);

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

}
