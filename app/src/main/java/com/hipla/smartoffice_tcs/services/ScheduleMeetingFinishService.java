package com.hipla.smartoffice_tcs.services;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import com.hipla.smartoffice_tcs.activity.DashboardActivity;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;
import com.hipla.smartoffice_tcs.utils.CONST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 4/3/2018.
 */

public class ScheduleMeetingFinishService extends JobService {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (meetingDetail != null) {
                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                if (new Date().compareTo(meetingDateTime) > 0) {
                    Paper.book().delete(CONST.IS_DETECTION_STARTED);
                    Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                    Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                    if (isMyServiceRunning(getApplicationContext(), MyNavigationService.class))
                        stopService(new Intent(getApplicationContext(), MyNavigationService.class));

                    //checkForUpcomingMeeting();
                    CONST.scheduleMeetingFetchJob(ScheduleMeetingFinishService.this, 1300, CONST.FETCH_JOB_ID);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
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

                    if(currentDateTime.compareTo(meetingDateTimeEnd) < 0){
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);
                        cal.add(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                        CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(), CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                        Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));

                        break;
                    }

                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTime);
                    cal.add(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                    CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(), CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                    Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));

                    break;
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
