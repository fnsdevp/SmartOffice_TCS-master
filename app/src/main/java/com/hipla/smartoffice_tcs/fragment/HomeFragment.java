package com.hipla.smartoffice_tcs.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.activity.DashboardActivity;
import com.hipla.smartoffice_tcs.adapter.UpcomingMeetingsAdapter;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.databinding.FragmentHomeBinding;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.model.ZoneInfo;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.utils.CONST;
import com.hipla.smartoffice_tcs.utils.InternetConnectionDetector;
import com.hipla.smartoffice_tcs.utils.MarshmallowPermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

import static com.hipla.smartoffice_tcs.networking.NetworkUtility.UPCOMING_MEETINGS;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BlankFragment implements StringRequestListener, UpcomingMeetingsAdapter.OnDateClickListener {

    private UpcomingMeetingsAdapter mAdapter;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private FragmentHomeBinding binding;
    private MyReceiver myReceiver;
    private static final int REQUEST_CALL_PERMISSION = 100;
    private UpcomingMeetings upcomingData;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        binding.setFragment(HomeFragment.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        mAdapter = new UpcomingMeetingsAdapter(getActivity());
        mAdapter.setOnDateClickListener(this);
        binding.llUpcomingMeetings.setAdapter(mAdapter);


    }

    public void scheduleMeeting(){

        if(getActivity()!=null){
            ((DashboardActivity)getActivity()).scheduleMeeting();
        }

    }

    public void indboxMeeting(){
        if(getActivity()!=null){
            ((DashboardActivity)getActivity()).indboxMessages();
        }
    }

    public void manageMeeting(){

        if(getActivity()!=null){
            ((DashboardActivity)getActivity()).manageMeeting();
        }

    }

    public void orderFood(){

        if(getActivity()!= null){
            ((DashboardActivity)getActivity()).orderFoodFragment();
        }
    }

    private void getUpcomingMeetings(){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, HomeFragment.this, "upcomingMeetings",
                        NetworkUtility.BASEURL + UPCOMING_MEETINGS);
            }

        }catch (Exception ex){

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type){

                case "upcomingMeetings":

                    JSONObject responseObject = new JSONObject(result);
                    if(responseObject.optString("status").equalsIgnoreCase("success")){

                        JSONArray upcomingAppointments = responseObject.getJSONArray("apointments");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        UpcomingMeetings[] upcomingMeetings = gson.fromJson(upcomingAppointments.toString(), UpcomingMeetings[].class);
                        List<UpcomingMeetings> upcomingMeetingsList = Arrays.asList(upcomingMeetings);

                        mAdapter.notifyDataChange(upcomingMeetingsList);

                    }

                    break;

                case "setMeetingStatus":

                    JSONObject meetingStatusResponse = new JSONObject(result);
                    if(meetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            meetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")){

                        getUpcomingMeetings();
                    }else{
                        Toast.makeText(getActivity(), meetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        }catch (Exception ex){

        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onResume() {
        super.onResume();

        if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            getUpcomingMeetings();
        }

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("intent.start.NewMessage");
        getActivity().registerReceiver(myReceiver, intentFilter);

        if(Paper.book().read(CONST.NEW_MESSAGE, false)){
            binding.ivNewMessage.setVisibility(View.VISIBLE);
        }else{
            binding.ivNewMessage.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if(myReceiver!=null){
            getActivity().unregisterReceiver(myReceiver);
        }
    }

    @Override
    public void onDateClick(String date) {

    }

    @Override
    public void onAddToCalender(int position, UpcomingMeetings data) {
        onAddEventClicked(data);
    }

    @Override
    public void onOpenDetails(int position, UpcomingMeetings data) {

    }

    @Override
    public void onCall(int position, UpcomingMeetings data) {
        upcomingData = data;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MarshmallowPermissionHelper.getCallPermission(HomeFragment.this,
                    getActivity(), REQUEST_CALL_PERMISSION)) {
                makeCall(data.getGuest().getPhone());
            }
        }else{
            makeCall(data.getGuest().getPhone());
        }
    }

    @Override
    public void onNavigate(int position, UpcomingMeetings data) {

        Db_Helper db_helper = new Db_Helper(getActivity());
        ZoneInfo zoneInfo = db_helper.getZoneInfoByName(String.format("%s", "Conference Room"));
        if (zoneInfo != null) {
            String[] location = zoneInfo.getCenterPoint().split(",");

            NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
            Bundle bundle = new Bundle();
            bundle.putString(CONST.POINTX, location[0]);
            bundle.putString(CONST.POINTY, location[1]);
            mapDialog.setArguments(bundle);
            if (mapDialog != null && mapDialog.getDialog() != null
                    && mapDialog.getDialog().isShowing()) {
                //dialog is showing so do something
            } else {
                //dialog is not showing
                mapDialog.show(getChildFragmentManager(), "mapDialog");
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_info_available), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onCancelMeeting(int position, UpcomingMeetings data) {
        try {
            Date meetinDate = dateFormat.parse(data.getFdate() + " " + data.getFromtime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(meetinDate);
            calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

            if(new Date().compareTo(calendar.getTime())<0) {
                setMeetingStatus(data, "cancel");
            }else{
                Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equalsIgnoreCase("intent.start.NewMessage")) {
                binding.ivNewMessage.setVisibility(View.VISIBLE);
            }

        }
    }

    public void onAddEventClicked(UpcomingMeetings data) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            Calendar cal = Calendar.getInstance();

            Date meetingDateTime = dateFormat.parse(data.getFdate() + " " + data.getFromtime());
            Date meetingDateTimeEnd = dateFormat.parse(data.getFdate() + " " + data.getTotime());

            cal.setTime(meetingDateTime);
            long startTime = cal.getTimeInMillis();

            cal.setTime(meetingDateTimeEnd);
            long endTime = cal.getTimeInMillis();

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
            //intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

            intent.putExtra(CalendarContract.Events.TITLE, getString(R.string.app_name));
            intent.putExtra(CalendarContract.Events.DESCRIPTION, data.getAgenda());
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "TCS Banyan Park");
            //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

            startActivity(intent);
        }catch (Exception ex){

        }
    }

    private void makeCall(String mobileNo) {
        String uri = "tel:" + mobileNo.trim();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (upcomingData != null)
                        makeCall(upcomingData.getGuest().getPhone());
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void setMeetingStatus(UpcomingMeetings data, String status){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));
                requestParameter.put("appid", String.format("%s",data.getId()));
                requestParameter.put("status", String.format("%s",status));

                new PostStringRequest(getActivity(), requestParameter, HomeFragment.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                /*if(!mDialog.isShowing()){
                    mDialog.show();
                }*/
            }

        }catch (Exception ex){

        }
    }

}
