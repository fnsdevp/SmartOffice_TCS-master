package com.hipla.smartoffice_tcs.firebase;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.activity.LoginActivity;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.utils.CONST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "Firebase";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Notification Rewceived");

        if (remoteMessage != null && remoteMessage.getData() != null) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            //Log.d(TAG, "Notification TripMessageData Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Notification TripMessageData Data: " + remoteMessage.getData().toString());

            Map<String, String> data = remoteMessage.getData();

            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            // Get info from the currently active task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            String activityName = taskInfo.get(0).topActivity.getClassName();

            //String body = remoteMessage.getNotification().getBody();

            try {

                UserData userDetails = Paper.book().read(NetworkUtility.USER_INFO);

                if(userDetails!=null) {
                    if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("ConfirmAppointment")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("Cancel")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("Confirm")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");
                    }else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("End")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");
                    }else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("message")) {

                        Paper.book().write(CONST.NEW_MESSAGE, true);

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewMessage");
                        sendBroadcast(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //sendNotification(data.get("pushType")+data.get("body"));

        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))
                        .setVibrate(new long[]{1000, 1000, 1000})
                        .setLights(Color.RED, 3000, 3000)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText("" + message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LoginActivity.class);
        //resultIntent.putExtra("NotificationMessage", "This is from notification");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher_round : R.mipmap.ic_launcher;
    }

    private boolean setBluetoothEnable(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = false;

        if (bluetoothAdapter != null) {
            isEnabled = bluetoothAdapter.isEnabled();
        } else {
            return false;
        }

        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }


    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
