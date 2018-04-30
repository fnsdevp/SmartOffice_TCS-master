package com.hipla.smartoffice_tcs.adapter;

/**
 * Created by User on 8/3/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.Appointments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RequestMeetingListAdapter extends RecyclerView.Adapter<RequestMeetingListAdapter.ViewHolder> {

    private List<Appointments> values;
    private List<Appointments> valuesCopy;
    private Context context;
    private OnAppointmentClickListener mListener;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_day, tv_date, tv_fname, tv_confirm, tv_status, tv_cancel, tv_email, tv_phone, tv_timings;
        public TextView tv_door_id, tv_door_pin;
        public View layout;
        public LinearLayout ll_direction;
        public RelativeLayout rl_date;
        public ImageView iv_calender, iv_memo, iv_call, iv_map, iv_meeting_status;

        public ViewHolder(View v) {
            super(v);
            layout = v;

            tv_day = (TextView) v.findViewById(R.id.tv_day);
            tv_date = (TextView) v.findViewById(R.id.tv_date);
            iv_memo = (ImageView) v.findViewById(R.id.iv_memo);
            iv_calender = (ImageView) v.findViewById(R.id.iv_calender);
            tv_fname = (TextView) v.findViewById(R.id.tv_fname);
            tv_confirm = (TextView) v.findViewById(R.id.tv_confirm);
            tv_status = (TextView) v.findViewById(R.id.tv_status);
            tv_cancel = (TextView) v.findViewById(R.id.tv_cancel);
            tv_email = (TextView) v.findViewById(R.id.tv_email);
            tv_phone = (TextView) v.findViewById(R.id.tv_phone);
            tv_timings = (TextView) v.findViewById(R.id.tv_timings);
            iv_call = (ImageView) v.findViewById(R.id.iv_call);
            iv_map = (ImageView) v.findViewById(R.id.iv_map);
            iv_meeting_status = (ImageView) v.findViewById(R.id.iv_meeting_status);
            ll_direction = (LinearLayout) v.findViewById(R.id.ll_direction);
            rl_date = (RelativeLayout) v.findViewById(R.id.rl_date);

            /*Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/futura_bk_bt.ttf");
            tv_student_name.setTypeface(custom_font);
            tv_in_time.setTypeface(custom_font);
            tv_out_time.setTypeface(custom_font);*/
        }
    }

    public void notifyDataChange(List<Appointments> data) {
        this.values = data;
        this.valuesCopy = data;
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RequestMeetingListAdapter(Context context, List<Appointments> myDataset) {
        this.context = context;
        values = myDataset;
        valuesCopy = myDataset;
    }

    @Override
    public int getItemViewType(int position) {
        if (values.size() > 0 && position == values.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RequestMeetingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        if (viewType == TYPE_ITEM) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v =
                    inflater.inflate(R.layout.meeting_row, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        } else {
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v =
                    inflater.inflate(R.layout.outbox_footer, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            if (position != values.size()) {
                Appointments appointments = values.get(position);

                Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(appointments.getFdate());

                holder.tv_day.setText("" + new SimpleDateFormat("dd").format(meetingDate));
                holder.tv_date.setText("" + new SimpleDateFormat("MMM yyyy").format(meetingDate));

                if (appointments.getStatus().equalsIgnoreCase("end")) {
                    holder.iv_meeting_status.setImageResource(R.drawable.ic_end);
                    holder.tv_status.setText(context.getString(R.string.ended));
                    holder.tv_status.setVisibility(View.GONE);
                    holder.tv_confirm.setVisibility(View.GONE);
                    holder.tv_cancel.setVisibility(View.GONE);
                    holder.ll_direction.setVisibility(View.GONE);
                    holder.rl_date.setBackgroundResource(R.drawable.normal_card_red);
                } else if (appointments.getStatus().equalsIgnoreCase("pending")) {
                    holder.iv_meeting_status.setImageResource(R.drawable.ic_pending);
                    holder.tv_status.setVisibility(View.GONE);
                    holder.tv_confirm.setVisibility(View.VISIBLE);
                    holder.tv_cancel.setVisibility(View.VISIBLE);
                    holder.ll_direction.setVisibility(View.GONE);
                    holder.rl_date.setBackgroundResource(R.drawable.normal_card_orange);
                } else if (appointments.getStatus().equalsIgnoreCase("confirm")) {
                    holder.iv_meeting_status.setImageResource(R.drawable.ic_meetingconfirm);
                    holder.tv_status.setText(context.getString(R.string.confirmed));
                    holder.tv_status.setVisibility(View.GONE);
                    holder.tv_confirm.setVisibility(View.GONE);
                    holder.tv_cancel.setVisibility(View.VISIBLE);
                    holder.ll_direction.setVisibility(View.VISIBLE);
                    holder.rl_date.setBackgroundResource(R.drawable.normal_card_green);
                } else if (appointments.getStatus().equalsIgnoreCase("cancel")) {
                    holder.iv_meeting_status.setImageResource(R.drawable.ic_cancel);
                    holder.tv_status.setText(context.getString(R.string.cancelled));
                    holder.tv_status.setVisibility(View.GONE);
                    holder.tv_confirm.setVisibility(View.GONE);
                    holder.tv_cancel.setVisibility(View.GONE);
                    holder.ll_direction.setVisibility(View.GONE);
                    holder.rl_date.setBackgroundResource(R.drawable.normal_card_red);
                }

                holder.tv_fname.setText("" + appointments.getUserdetails().getContact());
                holder.tv_email.setText("" + appointments.getUserdetails().getEmail());
                holder.tv_phone.setText("" + appointments.getUserdetails().getPhone());
                holder.tv_timings.setText(String.format(context.getString(R.string.timing_format), appointments.getFromtime(), appointments.getTotime()));

                holder.iv_calender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onAddToCalender(position, values.get(position));
                        }
                    }
                });

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onOpenDetails(position, values.get(position));
                        }
                    }
                });

                holder.iv_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onCall(position, values.get(position));
                        }
                    }
                });

                holder.iv_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onNavigate(position, values.get(position));
                        }
                    }
                });

                holder.tv_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onConfirmMeeting(position, values.get(position));
                        }
                    }
                });

                holder.tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onCancelMeeting(position, values.get(position));
                        }
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

    public interface OnAppointmentClickListener {
        void onAddToCalender(int position, Appointments data);

        void onOpenDetails(int position, Appointments data);

        void onCall(int position, Appointments data);

        void onNavigate(int position, Appointments data);

        void onConfirmMeeting(int position, Appointments data);

        void onCancelMeeting(int position, Appointments data);
    }

    public void setOnAttendanceClickListener(OnAppointmentClickListener mListener) {
        this.mListener = mListener;
    }

    public void filter(String text) {
        values = new ArrayList<>();
        if (text.isEmpty()) {
            values.addAll(valuesCopy);
        } else {
            text = text.toLowerCase();
            for (Appointments item : valuesCopy) {
                if (item.getStatus().toLowerCase().equalsIgnoreCase(text)) {
                    values.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByName(String text) {
        values = new ArrayList<>();
        if (text.isEmpty()) {
            values.addAll(valuesCopy);
        } else {
            text = text.toLowerCase();
            for (Appointments item : valuesCopy) {
                if (item.getUserdetails().getContact().toLowerCase().contains(text)) {
                    values.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}