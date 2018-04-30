package com.hipla.smartoffice_tcs.fragment;


import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.databinding.FragmentManageMeetingBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManageMeetingFragment extends Fragment {

    private FragmentManageMeetingBinding binding;
    private MyAppointmentsFragment mMyAppointmentsFragment;
    private RequestedMeetingsFragment mRequestedMeetingsFragment;
    private int selectedItem = 0, selectedItemOutbox=0;

    public ManageMeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_manage_meeting, container, false);
        binding.setFragment(ManageMeetingFragment.this);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        mMyAppointmentsFragment = new MyAppointmentsFragment();
        mRequestedMeetingsFragment = new RequestedMeetingsFragment();

        binding.viewPagerManageMeetings.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        binding.viewPagerManageMeetings.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                    binding.line1.setVisibility(View.VISIBLE);
                    binding.tvMyappointment.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvRequestReceived.setTextColor(getResources().getColor(R.color.text_light_gray));
                }else if(position==1){
                    binding.line1.setVisibility(View.GONE);
                    binding.tvMyappointment.setTextColor(getResources().getColor(R.color.text_light_gray));

                    binding.line2.setVisibility(View.VISIBLE);
                    binding.tvRequestReceived.setTextColor(getResources().getColor(R.color.text_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.tvMyappointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPagerManageMeetings.setCurrentItem(0);
            }
        });

        binding.tvRequestReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPagerManageMeetings.setCurrentItem(1);
            }
        });

        binding.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowPopupWindow(v);
            }
        });

        binding.etSpotlightSerch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(! binding.etSpotlightSerch.getText().toString().trim().isEmpty()){
                    if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                        mMyAppointmentsFragment.filterByName(binding.etSpotlightSerch.getText().toString().trim());
                    }else{
                        mRequestedMeetingsFragment.filterByName(binding.etSpotlightSerch.getText().toString().trim());
                    }
                }else{
                    if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                        mMyAppointmentsFragment.filterByName("");
                    }else{
                        mRequestedMeetingsFragment.filterByName("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tvcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etSpotlightSerch.setText("");

                if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                    mMyAppointmentsFragment.filterByName("");
                }else{
                    mRequestedMeetingsFragment.filterByName("");
                }
            }
        });
    }

    public void selectMyAppointment(){

    }

    public void selectRequestReceived(){

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return mMyAppointmentsFragment;
                case 1:
                    return mRequestedMeetingsFragment;
                default:
                    return mMyAppointmentsFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void onShowPopupWindow(View v) {
        final PopupWindow popup = new PopupWindow(getActivity());
        View layout = getLayoutInflater().inflate(R.layout.meeting_popup_window, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(v);

        TextView tv_all = layout.findViewById(R.id.tv_all);
        TextView tv_pending = layout.findViewById(R.id.tv_pending);
        TextView tv_confirm = layout.findViewById(R.id.tv_confirm);

        if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
            if (selectedItem == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 1) {
                tv_pending.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 2) {
                tv_confirm.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }else{
            if (selectedItemOutbox == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 1) {
                tv_pending.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 2) {
                tv_confirm.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }

        tv_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 0;
                        mMyAppointmentsFragment.filterMessage("");
                    }
                }else{
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox = 0;
                        mRequestedMeetingsFragment.filterMessage("");
                    }
                }
            }
        });

        tv_pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 1;
                        mMyAppointmentsFragment.filterMessage("pending");
                    }
                }else{
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox=1;
                        mRequestedMeetingsFragment.filterMessage("pending");
                    }
                }
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 2;
                        mMyAppointmentsFragment.filterMessage("confirm");
                    }
                }else{
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox=2;
                        mRequestedMeetingsFragment.filterMessage("confirm");
                    }
                }
            }
        });
    }

}
