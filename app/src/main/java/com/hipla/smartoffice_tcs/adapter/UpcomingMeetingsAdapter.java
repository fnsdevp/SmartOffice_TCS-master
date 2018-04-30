package com.hipla.smartoffice_tcs.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.Appointments;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by FNSPL on 8/21/2017.
 */

public class UpcomingMeetingsAdapter extends BaseAdapter {
    private Context mContext;
    private OnDateClickListener mListener;
    private List<UpcomingMeetings> upcomingMeetingsList = new ArrayList<>();
    private int gridWidth, gridHeight;

    // Constructor
    public UpcomingMeetingsAdapter(Context c) {
        mContext = c;

        getDisplayMetrics((Activity) c);
    }

    public int getCount() {
        return upcomingMeetingsList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = inflater.inflate(R.layout.upcoming_meeting_row, null);
        } else {
            grid = (View) convertView;
        }

        TextView tv_name = (TextView) grid.findViewById(R.id.tv_name);
        TextView tv_day = (TextView) grid.findViewById(R.id.tv_day);
        TextView tv_date = (TextView) grid.findViewById(R.id.tv_date);
        TextView tv_email = (TextView) grid.findViewById(R.id.tv_email);
        TextView tv_cancel = (TextView) grid.findViewById(R.id.tv_cancel);
        TextView tv_phone = (TextView) grid.findViewById(R.id.tv_phone);
        TextView tv_timings = (TextView) grid.findViewById(R.id.tv_timings);

        ImageView iv_calender = (ImageView) grid.findViewById(R.id.iv_calender);
        ImageView iv_memo = (ImageView) grid.findViewById(R.id.iv_memo);
        ImageView iv_call = (ImageView) grid.findViewById(R.id.iv_call);
        ImageView iv_map = (ImageView) grid.findViewById(R.id.iv_map);

        try {
            Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(upcomingMeetingsList.get(position).getFdate());

            tv_day.setText("" + new SimpleDateFormat("dd").format(meetingDate));
            tv_date.setText("" + new SimpleDateFormat("MMM yyyy").format(meetingDate));

            tv_name.setText(String.format("%s", upcomingMeetingsList.get(position).getGuest().getContact()));
            tv_timings.setText(String.format("%s - %s", upcomingMeetingsList.get(position).getFromtime(), upcomingMeetingsList.get(position).getTotime()));
            tv_phone.setText(String.format("%s", upcomingMeetingsList.get(position).getGuest().getPhone()));
            tv_email.setText("" + upcomingMeetingsList.get(position).getGuest().getEmail());

            iv_calender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddToCalender(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            /*holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onOpenDetails(position, values.get(position));
                    }
                }
            });*/

            iv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onCall(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            iv_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onNavigate(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onCancelMeeting(position, upcomingMeetingsList.get(position));
                    }
                }
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return grid;
    }

    public interface OnDateClickListener {
        void onDateClick(String date);

        void onAddToCalender(int position, UpcomingMeetings data);

        void onOpenDetails(int position, UpcomingMeetings data);

        void onCall(int position, UpcomingMeetings data);

        void onNavigate(int position, UpcomingMeetings data);

        void onCancelMeeting(int position, UpcomingMeetings data);
    }

    public void setOnDateClickListener(OnDateClickListener mListenere) {
        this.mListener = mListenere;
    }

    public void notifyDataChange(List<UpcomingMeetings> data) {
        this.upcomingMeetingsList = data;
        notifyDataSetChanged();
    }


    // Method for converting DP/DIP value to pixels
    public int getPixelsFromDPs(Activity activity, float dps) {
        Resources r = activity.getResources();


        int px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
    }

    public void getDisplayMetrics(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        gridHeight = (int) (displayMetrics.heightPixels / 7.2);
        gridWidth = (int) (displayMetrics.widthPixels / 2.2);
    }

}