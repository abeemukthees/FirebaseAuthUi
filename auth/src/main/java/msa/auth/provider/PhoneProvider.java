package msa.auth.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.PhoneAuthProvider;

import androidx.annotation.LayoutRes;
import msa.auth.AuthUI;
import msa.auth.R;
import msa.auth.ResultCodes;
import msa.auth.ui.BaseHelper;
import msa.auth.ui.phone.PhoneVerificationActivity;

public class PhoneProvider implements Provider {

    private static final int RC_PHONE_FLOW = 4;

    private Activity mActivity;
    private BaseHelper mHelper;

    public PhoneProvider(Activity activity, BaseHelper helper) {
        mActivity = activity;
        mHelper = helper;
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.provider_name_phone);
    }

    @Override
    @AuthUI.SupportedProvider
    public String getProviderId() {
        return PhoneAuthProvider.PROVIDER_ID;
    }

    @Override
    @LayoutRes
    public int getButtonLayout() {
        return R.layout.provider_button_phone;
    }

    @Override
    public void startLogin(Activity activity) {
        activity.startActivityForResult(
                PhoneVerificationActivity.createIntent(activity, mHelper.getFlowParams(), null),
                RC_PHONE_FLOW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHONE_FLOW && resultCode == ResultCodes.OK) {
            mHelper.finishActivity(mActivity, ResultCodes.OK, data);
        }
    }
}
