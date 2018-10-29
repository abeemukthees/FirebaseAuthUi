package msa.auth.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.EmailAuthProvider;

import androidx.annotation.LayoutRes;
import msa.auth.AuthUI;
import msa.auth.R;
import msa.auth.ResultCodes;
import msa.auth.ui.BaseHelper;
import msa.auth.ui.email.RegisterEmailActivity;

public class EmailProvider implements Provider {
    private static final int RC_EMAIL_FLOW = 2;

    private Activity mActivity;
    private BaseHelper mHelper;

    public EmailProvider(Activity activity, BaseHelper helper) {
        mActivity = activity;
        mHelper = helper;
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.provider_name_email);
    }

    @Override
    @AuthUI.SupportedProvider
    public String getProviderId() {
        return EmailAuthProvider.PROVIDER_ID;
    }

    @Override
    @LayoutRes
    public int getButtonLayout() {
        return R.layout.provider_button_email;
    }

    @Override
    public void startLogin(Activity activity) {
        activity.startActivityForResult(
                RegisterEmailActivity.createIntent(activity, mHelper.getFlowParams()),
                RC_EMAIL_FLOW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_EMAIL_FLOW && resultCode == ResultCodes.OK) {
            mHelper.finishActivity(mActivity, ResultCodes.OK, data);
        }
    }
}
