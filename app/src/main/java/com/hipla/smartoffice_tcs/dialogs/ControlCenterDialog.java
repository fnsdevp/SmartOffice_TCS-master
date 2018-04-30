package com.hipla.smartoffice_tcs.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.databinding.FragmentControlCenterDialogBinding;
import com.hipla.smartoffice_tcs.model.UserData;
import com.hipla.smartoffice_tcs.networking.NetworkUtility;
import com.hipla.smartoffice_tcs.networking.PostStringRequest;
import com.hipla.smartoffice_tcs.networking.StringRequestListener;
import com.hipla.smartoffice_tcs.utils.CONST;
import com.hipla.smartoffice_tcs.utils.ColorPicker;
import com.hipla.smartoffice_tcs.utils.PahoMqttClient;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import io.paperdb.Paper;

public class ControlCenterDialog extends DialogFragment implements ColorPicker.OnColorChangedListener, StringRequestListener {

    private ColorPicker colorPickerView;
    private OnColorSelectedListener onColorSelectedListener;
    private View view;
    private FragmentControlCenterDialogBinding binding;
    private ProgressDialog mDialog;
    private OnDialogEvent mListener;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    public ControlCenterDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_control_center_dialog, container, false);
        initView(binding.getRoot());
        return binding.getRoot();
    }

    private void initView(View mView) {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getActivity(), CONST.MQTT_BROKER_URL, CONST.CLIENT_ID);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        colorPickerView = new ColorPicker(getActivity(), this);
        colorPickerView.setColor(Color.WHITE);
        colorPickerView.setId(R.id.colorpicker);
        binding.rlColorPicker.addView(colorPickerView, layoutParams);

        binding.tvOpenDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.switchThirdFloor.isChecked()) {
                    openDoor();
                    /*try {
                        pahoMqttClient.publishMessage(client, "on", 1, CONST.PUBLISH_TOPIC);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/

                }else{
                    openDoorThirdFloor();
                }
            }
        });

        binding.switchLightOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    openLightOne();
                    openLightTwo();
                    openLightThree();
                }else{
                    cloaseLightOne();
                    cloaseLightTwo();
                    cloaseLightThree();
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(mListener!=null){
            mListener.onDismissListener();
        }
    }

    public interface OnDialogEvent{
        void onDismissListener();
    }

    public void setOnDismissClickListener(OnDialogEvent mListener){
        this.mListener = mListener;
    }


    @Override
    public void onColorChanged(int color) {

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type) {
                case "openDoor":

                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.optString("status").equalsIgnoreCase("success") &&
                            jsonObject.optString("message").equalsIgnoreCase("Door Open")){

                    }else{
                        openDoor();
                    }

                    break;

                case "openDoorThirdFloor":

                    JSONObject resJsonObject1 = new JSONObject(result);
                    if(resJsonObject1.optString("status").equalsIgnoreCase("success") &&
                            resJsonObject1.optString("message").equalsIgnoreCase("Door Open")){

                    }else{
                        openDoorThirdFloor();
                    }

                    break;

                case "lightOnOne":

                    break;

                case "lightCloseOne":

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

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }

    public void openDoor() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("doorstatus", String.format("%s", 0));
                requestParameter.put("firefrom", String.format("%s", "app"));

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "openDoor",
                        NetworkUtility.BASEURL + NetworkUtility.OPEN_DOOR);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    public void openDoorThirdFloor() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("doorstatus", String.format("%s", 0));
                requestParameter.put("firefrom", String.format("%s", "app"));

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "openDoorThirdFloor",
                        NetworkUtility.BASEURL + NetworkUtility.OPEN_DOOR_THIRED_FLOOR);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    public void openLightOne() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightOnOne",
                        NetworkUtility.LIGHT_OPEN_ONE);
            }

        } catch (Exception ex) {

        }
    }

    public void openLightTwo() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightOnOne",
                        NetworkUtility.LIGHT_OPEN_TWO);
            }

        } catch (Exception ex) {

        }
    }

    public void openLightThree() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightOnOne",
                        NetworkUtility.LIGHT_OPEN_THREE);
            }

        } catch (Exception ex) {

        }
    }

    public void cloaseLightOne() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightCloseOne",
                        NetworkUtility.LIGHT_CLOSE_ONE);
            }

        } catch (Exception ex) {

        }
    }

    public void cloaseLightTwo() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightCloseOne",
                        NetworkUtility.LIGHT_CLOSE_TWO);
            }

        } catch (Exception ex) {

        }
    }

    public void cloaseLightThree() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "lightCloseOne",
                        NetworkUtility.LIGHT_CLOSE_THREE);
            }

        } catch (Exception ex) {

        }
    }

}
