package com.hipla.smartoffice_tcs.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.adapter.ExpandableListAdapter;
import com.hipla.smartoffice_tcs.databinding.FragmentOrderFoodBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderFoodFragment extends Fragment {

    private FragmentOrderFoodBinding binding ;
    ExpandableListAdapter listAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild ;


    public OrderFoodFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_food, container, false);
        initView();
        return binding.getRoot();
    }


    public void initView(){

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        binding.lvExp.setAdapter(listAdapter);


    }


    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Bevarage");
        listDataHeader.add("Snacks");
        listDataHeader.add("Others");

        // Adding child data
        List<String> bevarage = new ArrayList<String>();
        bevarage.add("Coffee");
        bevarage.add("Green tea");
        bevarage.add("Milk tea");
        bevarage.add("Lemon tea");
        bevarage.add("Masala Tea");
        bevarage.add("Cold Coffee");
        bevarage.add("Water");

        List<String> snacks = new ArrayList<String>();
        snacks.add("Chips");
        snacks.add("Biscuits");
        snacks.add("Sugar Free Biscuits");

        List<String> others = new ArrayList<String>();
        others.add("Mineral water");
        others.add("Luke water");

        listDataChild.put(listDataHeader.get(0), bevarage); // Header, Child data
        listDataChild.put(listDataHeader.get(1), snacks);
        listDataChild.put(listDataHeader.get(2), others);
    }
}
