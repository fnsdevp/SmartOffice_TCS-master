package com.hipla.smartoffice_tcs.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hipla.smartoffice_tcs.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeetingReviewFragment extends BlankFragment {


    public MeetingReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meeting_review, container, false);
    }

}
