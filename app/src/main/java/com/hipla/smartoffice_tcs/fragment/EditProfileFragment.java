package com.hipla.smartoffice_tcs.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.databinding.FragmentEditProfileBinding;
import com.hipla.smartoffice_tcs.dialogs.Dialogs;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.utils.CONST;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements StringRequestListener {

    private FragmentEditProfileBinding binding;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        try {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                binding.tvUserType.setText(String.format(getResources().getString(R.string.user_format), userData.getUsertype()));
                binding.tvFirstName.setText(String.format(getResources().getString(R.string.first_name_format),
                        userData.getFname()));
                binding.tvLastName.setText(String.format(getResources().getString(R.string.last_name_format),
                        userData.getLname()));
                if (userData.getUsertype().equalsIgnoreCase("Employee")) {
                    binding.tvDepartment.setText(String.format(getResources().getString(R.string.department_format),
                            userData.getDepartment()));
                } else {
                    binding.tvDepartment.setText(String.format(getResources().getString(R.string.company_format),
                            userData.getCompany()));
                }
                binding.tvDesignation.setText(String.format(getResources().getString(R.string.designation_format),
                        userData.getDesignation()));
                binding.tvEmail.setText(String.format(getResources().getString(R.string.email_format),
                        userData.getEmail()));
                binding.tvPhone.setText(String.format(getResources().getString(R.string.phone_format),
                        userData.getPhone()));

                binding.toggleButton.setChecked(Paper.book().read(CONST.DISTANCE_NOTIFICATION, false));
            }

            binding.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Paper.book().write(CONST.DISTANCE_NOTIFICATION, isChecked);
                }
            });

            binding.tvUpdateDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    requestPostData();
                }
            });

            binding.tvChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialogs.dialogChangePassword(getActivity(), new Dialogs.OnCallback() {
                        @Override
                        public void onSubmit(String password) {

                            changePasswordRequest(password);
                        }
                    });
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void requestPostData() {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("reg", "" + Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("type", "Android");

            new PostStringRequest(getActivity(), requestParameter, EditProfileFragment.this, "update",
                    NetworkUtility.BASEURL + NetworkUtility.DEVICE_UPDATE);

        }

    }

    private void changePasswordRequest(String newPassword) {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("newpassword", "Android");

            new PostStringRequest(getActivity(), requestParameter, EditProfileFragment.this, "changePassword",
                    NetworkUtility.BASEURL + NetworkUtility.CHANGE_PASSWORD);

        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type) {

                case "update":

                    JSONObject responseObject = new JSONObject(result);
                    if(responseObject.optString("status").equalsIgnoreCase("success")
                            && responseObject.optString("message").equalsIgnoreCase("Updated")){

                        Toast.makeText(getActivity(), getResources().getString(R.string.device_updated), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "changePassword":

                    JSONObject changePasswordResponse = new JSONObject(result);
                    if(changePasswordResponse.optString("status").equalsIgnoreCase("success") &&
                            changePasswordResponse.optString("message").equalsIgnoreCase("you have successfully update your password")){

                        Toast.makeText(getActivity(), changePasswordResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), changePasswordResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

}
