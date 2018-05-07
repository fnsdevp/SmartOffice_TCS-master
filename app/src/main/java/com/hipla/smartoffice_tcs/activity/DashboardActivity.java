package com.hipla.smartoffice_tcs.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.database.Db_Helper;
import com.hipla.smartoffice_tcs.databinding.ActivityDashboardBinding;
import com.hipla.smartoffice_tcs.dialogs.ControlCenterDialog;
import com.hipla.smartoffice_tcs.fragment.AboutUsFragment;
import com.hipla.smartoffice_tcs.fragment.EditProfileFragment;
import com.hipla.smartoffice_tcs.fragment.HomeFragment;
import com.hipla.smartoffice_tcs.fragment.IndboxMessagesFragment;
import com.hipla.smartoffice_tcs.fragment.ManageMeetingFragment;
import com.hipla.smartoffice_tcs.fragment.MeetingDetailFragment;
import com.hipla.smartoffice_tcs.fragment.NotificationFragment;
import com.hipla.smartoffice_tcs.fragment.OrderFoodFragment;
import com.hipla.smartoffice_tcs.fragment.RequestMeetingDetailFragment;
import com.hipla.smartoffice_tcs.fragment.ScheduleMeetingFragment;
import com.hipla.smartoffice_tcs.fragment.SetAvaibilityFragment;
import com.hipla.smartoffice_tcs.model.UpcomingMeetings;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.services.MyNavigationService;
import com.hipla.smartoffice_tcs.fragment.NavigineMapDialogNew;
import com.hipla.smartoffice_tcs.utils.CONST;
import com.hipla.smartoffice_tcs.utils.ErrorMessageDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

public class DashboardActivity extends BaseActivity implements StringRequestListener, ControlCenterDialog.OnDialogEvent {

    private ActivityDashboardBinding binding;
    private MyReceiver myReceiver;
    private boolean isShowingControlCenter = false, isShowingPopup = false;

    public DashboardActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard);
        binding.setActivity(DashboardActivity.this);

        initView();

        CONST.scheduleMeetingFetchJob(DashboardActivity.this, 1300, CONST.FETCH_JOB_ID);
    }

    private void initView() {

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/HelveticaNeue Light.ttf");

        binding.tvsmart.setTypeface(tf);
        binding.tvoffice.setTypeface(tf);

        binding.ivScheduleSettings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //Button Pressed

                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    scheduleMeeting();
                }

                return true;
            }
        });

        goToHome();

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (userData.getUsertype().equalsIgnoreCase("Guest")) {
            binding.llSetAvaibility.setVisibility(View.GONE);
        } else {
            binding.llSetAvaibility.setVisibility(View.VISIBLE);
        }

    }

    public void scheduleMeeting() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting_ac));

        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings_active);

        setFragment(new ScheduleMeetingFragment(), CONST.SCHEDULE_MEETING);
    }

    public void manageMeeting() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));

        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new ManageMeetingFragment(), CONST.MANAGE_MEETING);
    }

    public void meetingsDetail() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));

        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new MeetingDetailFragment(), CONST.MEETING_DETAILS);
    }

    public void requestedMeetingsDetail() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));

        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new RequestMeetingDetailFragment(), CONST.REQUESTED_MEETING_DETAILS);
    }

    public void indboxMessages() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home_active));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));

        setFragment(new IndboxMessagesFragment(), CONST.INDBOX_MESSGAES);
    }

    public void profileManagement() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new EditProfileFragment(), CONST.PROFILE_MANAGMENT);
    }

    public void aboutUs() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new AboutUsFragment(), CONST.ABOUT_US);
    }

    public void logout() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        Paper.book().delete(NetworkUtility.USER_INFO);
        Paper.book().delete(CONST.CURRENT_MEETING_DATA);
        Paper.book().delete(CONST.IS_DETECTION_STARTED);

        Db_Helper db_helper = new Db_Helper(DashboardActivity.this);
        db_helper.deleteNotificationInfo();

        CONST.cancelEndMeetingJob(DashboardActivity.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
        CONST.cancelStartMeetingJob(DashboardActivity.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
        CONST.cancelMeetingETAJob(DashboardActivity.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);

        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    public void orderFoodFragment(){
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new OrderFoodFragment(), CONST.ORDER_FOOD);
    }


    public void setAvaibility() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new SetAvaibilityFragment(), CONST.SET_AVAIBILITY);
    }

    public void setNotification() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification_active));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new NotificationFragment(), CONST.SET_NOTIFICATION);
    }

    public void setControlPanel() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        if(!isShowingControlCenter) {
            ControlCenterDialog mDialog = new ControlCenterDialog();
            mDialog.setOnDismissClickListener(DashboardActivity.this);
            mDialog.show(getSupportFragmentManager(), "contolCenter");
        }
    }


    public void openDrawerOnClick() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void backFromFragment(){
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            boolean done = getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestPostData();

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (userData.getUsertype().equalsIgnoreCase("Guest")) {
            if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                binding.llControlPanel.setVisibility(View.VISIBLE);
            }else{
                binding.llControlPanel.setVisibility(View.GONE);
            }
        } else {
            binding.llControlPanel.setVisibility(View.VISIBLE);
        }

        if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {

            if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                startService(new Intent(DashboardActivity.this, MyNavigationService.class));

            setControlPanel();
        }else{

            if (isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                stopService(new Intent(DashboardActivity.this, MyNavigationService.class));

        }

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("intent.start.Navigation");
        intentFilter.addAction("intent.start.Reschedule.Or.Finish");
        registerReceiver(myReceiver, intentFilter);

        if(Paper.book().read(CONST.NEEDS_TO_RESCHEDULE, false) && !isShowingPopup){
            isShowingPopup = true;

            ErrorMessageDialog.getInstant(DashboardActivity.this, new ErrorMessageDialog.OkCancelClickListener() {
                @Override
                public void onOkClickListener() {
                    isShowingPopup = false;
                }

                @Override
                public void onCancelClickListener() {
                    isShowingPopup = false;

                    if(haveNetworkConnection()) {
                        Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                        stopCurrentMeeting();
                    }else{
                        showNetConnectionDialog();
                    }
                }
            }).show(getString(R.string.want_to_extend), getString(R.string.app_name), "Yes", "No, Thanks");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (myReceiver != null)
            unregisterReceiver(myReceiver);

    }

    private void requestPostData() {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("reg", "" + Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("type", "Android");

            new PostStringRequest(DashboardActivity.this, requestParameter, DashboardActivity.this, "Login",
                    NetworkUtility.BASEURL + NetworkUtility.DEVICE_UPDATE);

        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        switch (type){
            case "setMeetingStatus":
                JSONObject response = new JSONObject(result);


                break;
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    public void setFragment(Fragment fragment, String fragmentName) {
        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(fragmentName);
        if (oldfragment == null) {
            t.replace(R.id.realtabcontent, fragment, fragmentName);
            t.addToBackStack(null);
        } else if (!oldfragment.isAdded()) {
            t.replace(R.id.realtabcontent, oldfragment, fragmentName);
        }
        t.commit();
    }

    public void goToHome() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home_active));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new HomeFragment(), CONST.HOME_FRAGMENT);
    }

    @Override
    public void onBackPressed() {

        Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(CONST.HOME_FRAGMENT);
        if (oldfragment != null && oldfragment.isVisible()) {
            finish();
        } else {
            //super.onBackPressed();
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                boolean done = getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
            stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
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

    @Override
    public void onDismissListener() {
        isShowingControlCenter = false;
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            //Toast.makeText(context, "Action: " + intent.getAction(), Toast.LENGTH_SHORT).show();

            if(intent.getAction().equalsIgnoreCase("intent.start.Navigation")) {
                if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                    startService(new Intent(DashboardActivity.this, MyNavigationService.class));

                setControlPanel();

            }else if(intent.getAction().equalsIgnoreCase("intent.start.Reschedule.Or.Finish") && !isShowingPopup){

                    isShowingPopup = true;

                    ErrorMessageDialog.getInstant(DashboardActivity.this, new ErrorMessageDialog.OkCancelClickListener() {
                        @Override
                        public void onOkClickListener() {
                            isShowingPopup = false;
                        }

                        @Override
                        public void onCancelClickListener() {
                            isShowingPopup = false;

                            if(haveNetworkConnection()) {
                                Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                                stopCurrentMeeting();
                            }else{
                                showNetConnectionDialog();
                            }
                        }
                    }).show(getString(R.string.want_to_extend), getString(R.string.app_name), "Yes", "No, Thanks");
            }
        }

    }

    private void stopCurrentMeeting() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", upcomingMeetings.getId()));
                requestParameter.put("status", String.format("%s", "end"));

                new PostStringRequest(DashboardActivity.this, requestParameter,
                        DashboardActivity.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openIndoorNavigation(){
        NavigineMapDialogNew mapDialogNew = new NavigineMapDialogNew();
        mapDialogNew.show(getSupportFragmentManager(), "indoorMap");
    }

}
