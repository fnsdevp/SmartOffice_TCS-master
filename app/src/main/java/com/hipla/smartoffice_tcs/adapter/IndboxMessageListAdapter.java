package com.hipla.smartoffice_tcs.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.IndboxMessages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class IndboxMessageListAdapter extends BaseAdapter {

    public List<IndboxMessages> _data;
    public List<IndboxMessages> _dataCopy;
    private Context _c;
    private ViewHolder v;
    private OnMessageClick callback;

    public IndboxMessageListAdapter(List<IndboxMessages> selectUsers, Context context) {
        _data = selectUsers;
        _dataCopy = selectUsers;
        _c = context;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_indbox_messages, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.tv_name = (TextView) view.findViewById(R.id.tv_name);
        v.tv_message = (TextView) view.findViewById(R.id.tv_message);
        v.tv_title = (TextView) view.findViewById(R.id.tv_title);
        v.tv_day = (TextView) view.findViewById(R.id.tv_day);
        v.tv_date = (TextView) view.findViewById(R.id.tv_date);
        v.rowLayout = (RelativeLayout) view.findViewById(R.id.contactinfo_ll_parent);

        final IndboxMessages data = (IndboxMessages) _data.get(i);
        String[] nameArray = data.getFrom().getName().split("\\s+");
        String firstLetter = "" + nameArray[0].charAt(0);

        if (nameArray.length > 1)
            firstLetter = firstLetter + "" + nameArray[1].charAt(0);

        v.tv_name.setText("" + firstLetter);
        v.tv_message.setText(Html.fromHtml(data.getMsg()));
        v.tv_title.setText(data.getTitle());

        if(data.getStatus().equalsIgnoreCase("unread")){
            v.tv_title.setTypeface(null, Typeface.BOLD);
        }else{
            v.tv_title.setTypeface(null, Typeface.NORMAL);
        }

        try {
            Date messageDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getTime());

            v.tv_day.setText(""+new SimpleDateFormat("dd").format(messageDate));
            v.tv_date.setText(""+new SimpleDateFormat("MMM yyyy").format(messageDate));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        v.rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null)
                    callback.selectMessage(data);

            }
        });

        view.setTag(data);
        return view;
    }

    public void notifyAdpater(List<IndboxMessages> selectUsers) {
        _data = selectUsers;
        _dataCopy = selectUsers;
        notifyDataSetChanged();
    }

    public interface OnMessageClick {
        void selectMessage(IndboxMessages review);
    }

    public void setOnMessageClick(OnMessageClick callback){
        this.callback = callback;
    }

    static class ViewHolder {
        TextView tv_name, tv_message, tv_title, tv_day, tv_date;
        RelativeLayout rowLayout;
    }

    public void filter(String text) {
        _data = new ArrayList<>();
        if(text.isEmpty()){
            _data.addAll(_dataCopy);
        } else{
            text = text.toLowerCase();
            for(IndboxMessages item: _dataCopy){
                if(item.getStatus().toLowerCase().equalsIgnoreCase(text)){
                    _data.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByName(String text) {
        _data = new ArrayList<>();
        if(text.isEmpty()){
            _data.addAll(_dataCopy);
        } else{
            text = text.toLowerCase();
            for(IndboxMessages item: _dataCopy){
                if(item.getFrom().getName().toLowerCase().contains(text)){
                    _data.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}

