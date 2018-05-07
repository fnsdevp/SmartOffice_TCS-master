package com.hipla.smartoffice_tcs.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.activity.DashboardActivity;
import com.hipla.smartoffice_tcs.databinding.FragmentScheduleMeetingBinding;
import com.hipla.smartoffice_tcs.utils.CONST;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleMeetingFragment extends BlankFragment {

    private FragmentScheduleMeetingBinding binding;
    private FixedMeetingFragment fixedMeetingFragment;
    private FlexibleMeetingFragment flexibleMeetingFragment;
    private WebExMeetingFragment webexMeetingFragment;
    private MyPagerAdapter myPagerAdapter;

    public ScheduleMeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_meeting, container, false);
        binding.setFragment(ScheduleMeetingFragment.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        fixedMeetingFragment = new FixedMeetingFragment();

        flexibleMeetingFragment = new FlexibleMeetingFragment();

        webexMeetingFragment = new WebExMeetingFragment();

        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        binding.vpMeetings.setAdapter(myPagerAdapter);

        binding.vpMeetings.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.line1.setVisibility(View.VISIBLE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));
                } else if (position == 1) {
                    binding.line1.setVisibility(View.GONE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));

                    binding.line2.setVisibility(View.VISIBLE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));
                } else if (position == 2) {
                    binding.line1.setVisibility(View.GONE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_light_gray));

                    binding.line3.setVisibility(View.VISIBLE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void setFixedMeeting() {
        binding.vpMeetings.setCurrentItem(0);
    }

    public void setFlexibleMeeting() {
        binding.vpMeetings.setCurrentItem(1);
    }

    public void setWebexMeeting() {
        binding.vpMeetings.setCurrentItem(2);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return fixedMeetingFragment;
                case 1:
                    return flexibleMeetingFragment;
                case 2:
                    return webexMeetingFragment;
                default:
                    return fixedMeetingFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
