package com.hipla.smartoffice_tcs.activity;

import android.os.Bundle;

import com.hipla.smartoffice_tcs.R;

public class ForgetPasswordActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forget_password);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

}
