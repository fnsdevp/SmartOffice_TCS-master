package com.hipla.smartoffice_tcs.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;
import com.hipla.smartoffice_tcs.utils.CONST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 4/3/2018.
 */

public class ScheduleMeetingExtendService extends JobService {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public boolean onStartJob(JobParameters params) {
        try {

            CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                    getString(R.string.meeting_over_can_extend), "Info");

            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            Intent intent = new Intent();
            intent.setAction("intent.start.Reschedule.Or.Finish");
            sendBroadcast(intent);

            Paper.book().write(CONST.NEEDS_TO_RESCHEDULE, true);

            if (meetingDetail != null) {
                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(meetingDateTime);

                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(new Date());

                CONST.scheduleEndMeetingJob(getApplicationContext(),
                        cal.getTimeInMillis() + (CONST.TIME_AFTER_MEETING_IN_SEC * 1000) - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
