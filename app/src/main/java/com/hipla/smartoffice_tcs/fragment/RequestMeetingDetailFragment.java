package com.hipla.smartoffice_tcs.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.activity.DashboardActivity;
import com.hipla.smartoffice_tcs.adapter.ReviewListAdapter;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.databinding.FragmentMeetingDetailBinding;
import com.hipla.smartoffice_tcs.dialogs.Dialogs;
import com.hipla.smartoffice_tcs.dialogs.RescheduleFixedMeetingDialog;
import com.hipla.smartoffice_tcs.dialogs.RescheduleFlexibleMeetingDialog;
import com.hipla.smartoffice_tcs.model.Appointments;
import com.hipla.smartoffice_tcs.model.ReviewMessage;
import com.hipla.smartoffice_tcs.model.RoomData;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.model.ZoneInfo;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.utils.CONST;
import com.hipla.smartoffice_tcs.utils.MarshmallowPermissionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestMeetingDetailFragment extends Fragment implements StringRequestListener, RescheduleFixedMeetingDialog.OnDialogEvent, RescheduleFlexibleMeetingDialog.OnDialogEvent {

    private static final int REQUEST_CALL_PERMISSION = 100;
    private FragmentMeetingDetailBinding binding;
    private ProgressDialog mDialog;
    private Appointments appointmentsData;
    private ReviewListAdapter mAdapter;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    public RequestMeetingDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meeting_detail, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        appointmentsData = Paper.book().read(CONST.APPOINTMENT_DATA);

        if (appointmentsData != null) {
            setAppointmentData(appointmentsData);

            mAdapter = new ReviewListAdapter(new ArrayList<ReviewMessage>(), getActivity());
            binding.listComments.setAdapter(mAdapter);

            binding.btnPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createReview(appointmentsData, binding.etMessage.getText().toString());
                }
            });

            binding.llReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(appointmentsData.getAppointmentType().equalsIgnoreCase("flexible")){

                        RescheduleFlexibleMeetingDialog mDialog = new RescheduleFlexibleMeetingDialog();
                        mDialog.setOnDismissClickListener(RequestMeetingDetailFragment.this);
                        mDialog.show(getChildFragmentManager(), "dialog");
                    }else{

                        RescheduleFixedMeetingDialog mDialog = new RescheduleFixedMeetingDialog();
                        mDialog.setOnDismissClickListener(RequestMeetingDetailFragment.this);
                        mDialog.show(getChildFragmentManager(), "dialog");
                    }

                }
            });
        }

        getAllReview(appointmentsData);
    }

    public void setAppointmentData(final Appointments appointments) {
        try {
            final Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(appointments.getFdate());

            binding.tvDay.setText("" + new SimpleDateFormat("dd").format(meetingDate));
            binding.tvDate.setText("" + new SimpleDateFormat("MMM yyyy").format(meetingDate));

            if (appointments.getStatus().equalsIgnoreCase("end")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_end);

                binding.llActions.setVisibility(View.GONE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.GONE);

            } else if (appointments.getStatus().equalsIgnoreCase("pending")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_pending);

                binding.llActions.setVisibility(View.VISIBLE);
                binding.ivAction.setImageResource(R.drawable.ic_pendings);
                binding.llReschedule.setVisibility(View.VISIBLE);
                binding.llCall.setVisibility(View.GONE);
                binding.llIndoorNavigation.setVisibility(View.GONE);
                binding.llOutdoorNavigation.setVisibility(View.GONE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.VISIBLE);

            } else if (appointments.getStatus().equalsIgnoreCase("confirm")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_meetingconfirm);

                binding.llActions.setVisibility(View.VISIBLE);
                binding.ivAction.setImageResource(R.drawable.ic_ends);
                binding.llReschedule.setVisibility(View.VISIBLE);
                binding.llCall.setVisibility(View.VISIBLE);
                binding.llIndoorNavigation.setVisibility(View.VISIBLE);
                binding.llOutdoorNavigation.setVisibility(View.VISIBLE);
                binding.tvAddReceipent.setVisibility(View.VISIBLE);
                binding.tvCancel.setVisibility(View.VISIBLE);

            } else if (appointments.getStatus().equalsIgnoreCase("cancel")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_cancel);

                binding.llActions.setVisibility(View.GONE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.GONE);
            }

            binding.tvFname.setText("" + appointments.getUserdetails().getContact());
            binding.tvEmail.setText("" + appointments.getUserdetails().getEmail());
            binding.tvPhone.setText("" + appointments.getUserdetails().getPhone());
            binding.tvMeetingTime.setText(String.format(getString(R.string.timing_format),
                    appointments.getFromtime(), appointments.getTotime()));
            binding.tvMeetingDate.setText(String.format(getString(R.string.meeting_date_value), appointments.getFdate()));
            binding.tvMeetingType.setText(String.format(getString(R.string.meeting_value), appointments.getAppointmentType()));
            binding.tvLocation.setText(String.format(getString(R.string.meeting_location_value), appointments.getLocation()));
            binding.tvAgenda.setText(String.format(getString(R.string.agenda_value), appointments.getAgenda()));

            binding.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (appointments.getStatus().equalsIgnoreCase("pending")) {
                        try {
                            Date meetingDateTime = mDateFormat.parse(appointments.getFdate() + " " + appointments.getTotime());

                            if (new Date().compareTo(meetingDateTime) < 0) {
                                confirmMeeting(appointments);
                            }else{
                                Toast.makeText(getActivity(), getString(R.string.you_cant_start_meeting_now), Toast.LENGTH_SHORT).show();
                            }

                        }catch (Exception ex){

                        }

                    } else if (appointments.getStatus().equalsIgnoreCase("confirm")) {
                        try {
                            Date meetingDateTime = mDateFormat.parse(appointments.getFdate() + " " + appointments.getTotime());

                            if (new Date().compareTo(meetingDateTime) > 0) {
                                setMeetingStatus(appointments, "end");
                            }else{
                                Toast.makeText(getActivity(), getString(R.string.you_cant_end_meeting_now), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            binding.ivCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Date meetinDate = mDateFormat.parse(appointments.getFdate() + " " + appointments.getFromtime());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(meetinDate);
                        calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

                        if(new Date().compareTo(calendar.getTime())<0) {
                            setMeetingStatus(appointments, "cancel");
                        }else{
                            Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });

            binding.llCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MarshmallowPermissionHelper.getCallPermission(RequestMeetingDetailFragment.this,
                            getActivity(), REQUEST_CALL_PERMISSION)) {
                        makeCall(appointments.getUserdetails().getPhone());
                    }
                }
            });

            binding.llOutdoorNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openGoogleMaps();
                }
            });

            binding.llIndoorNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openIndoorMap();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (appointmentsData != null)
                        makeCall(appointmentsData.getUserdetails().getPhone());
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void makeCall(String mobileNo) {
        String uri = "tel:" + mobileNo.trim();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    private void confirmMeeting(final Appointments data) {
        if (data.getAppointmentType().equalsIgnoreCase("flexible")) {
            Dialogs.dialogShowDates(getActivity(), data.getFdate(), data.getSdate(), new Dialogs.OnDateSelect() {
                @Override
                public void setRowClick(String date) {

                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                        confirmFlexibleMeetingRequest(data, "confirm", date);
                    } else {
                        showRoomDialogs(data, "confirm", date);
                    }

                }
            });
        } else {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                confirmFixedMeetingRequest(data, "confirm");
            } else {
                showRoomDialogs(data, "confirm");
            }

        }
    }

    private void showRoomDialogs(final Appointments data, final String status, final String date) {
        Dialogs.dialogShowRooms(getActivity(), new Dialogs.OnOptionSelect() {
            @Override
            public void setRowClick(RoomData roomData) {

                confirmFlexibleMeetingRequest(data, status, date, roomData.getRoomName());
            }
        });
    }

    private void showRoomDialogs(final Appointments data, final String status) {
        Dialogs.dialogShowRooms(getActivity(), new Dialogs.OnOptionSelect() {
            @Override
            public void setRowClick(RoomData roomData) {

                confirmFixedMeetingRequest(data, status, roomData.getRoomName());
            }
        });
    }

    private void setMeetingStatus(Appointments data, String status) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void createReview(Appointments data, String review) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("review", String.format("%s", review));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "createReview",
                        NetworkUtility.BASEURL + NetworkUtility.CREATE_REVIEW);
            }

        } catch (Exception ex) {

        }
    }

    private void getAllReview(Appointments data) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("appid", String.format("%s", data.getId()));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "getAllReview",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ALL_REVIEW);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }


    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type) {

                case "setMeetingStatus":

                    JSONObject meetingStatusResponse = new JSONObject(result);
                    if (meetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            meetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        String status = meetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = meetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);

                    } else {
                        Toast.makeText(getActivity(), meetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "setFixedMeetingStatus":

                    JSONObject fixedMeetingStatusResponse = new JSONObject(result);
                    if (fixedMeetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            fixedMeetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        String status = fixedMeetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = fixedMeetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);
                    } else {
                        Toast.makeText(getActivity(), fixedMeetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "setFlexibleMeetingStatus":

                    JSONObject flexibleMeetingStatusResponse = new JSONObject(result);
                    if (flexibleMeetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            flexibleMeetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        String status = flexibleMeetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = flexibleMeetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);

                    } else {
                        Toast.makeText(getActivity(), flexibleMeetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "createReview":

                    JSONObject createReviewResponse = new JSONObject(result);
                    if (createReviewResponse.optString("status").equalsIgnoreCase("success") &&
                            createReviewResponse.optString("message").equalsIgnoreCase("Review Saved")) {

                        getAllReview(appointmentsData);

                    } else {
                        Toast.makeText(getActivity(), createReviewResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "getAllReview":

                    JSONObject getAllReviewResponse = new JSONObject(result);
                    if (getAllReviewResponse.optString("status").equalsIgnoreCase("success")) {

                        binding.etMessage.setText("");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        ReviewMessage[] reviewMessages = gson.fromJson(getAllReviewResponse.getJSONArray("review").toString()
                                , ReviewMessage[].class);
                        List<ReviewMessage> mData = Arrays.asList(reviewMessages);

                        if (mData.size() > 0) {
                            View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.outbox_footer, null, false);
                            binding.listComments.addFooterView(footerView);
                        }

                        if (mAdapter != null) {
                            mAdapter.notifyAdpater(mData);
                        }

                    } else {
                        Toast.makeText(getActivity(), getAllReviewResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStarted() {

    }

    private void confirmFixedMeetingRequest(Appointments data, String status) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "setFixedMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFixedMeetingRequest(Appointments data, String status, String roomName) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("location", String.format("%s", roomName));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "setFixedMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFlexibleMeetingRequest(Appointments data, String status, String meetingDate) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("date", String.format("%s", meetingDate));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "setFlexibleMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFlexibleMeetingRequest(Appointments data, String status, String meetingDate, String roomName) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("date", String.format("%s", meetingDate));
                requestParameter.put("location", String.format("%s", roomName));

                new PostStringRequest(getActivity(), requestParameter, RequestMeetingDetailFragment.this, "setFlexibleMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void openGoogleMaps() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1")
                .appendQueryParameter("destination", CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG);
        String url = builder.build().toString();
        Log.d("Directions", url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void openIndoorMap(){
        Db_Helper db_helper = new Db_Helper(getActivity());
        ZoneInfo zoneInfo = db_helper.getZoneInfoByName(String.format("%s", appointmentsData.getLocation()));
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
    public void onDismissListener() {
        appointmentsData = Paper.book().read(CONST.APPOINTMENT_DATA);

        if (appointmentsData != null) {
            setAppointmentData(appointmentsData);
        }

        if(getActivity()!=null){
            ((DashboardActivity) getActivity()).backFromFragment();
        }
    }
}
