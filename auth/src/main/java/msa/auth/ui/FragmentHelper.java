package msa.auth.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class FragmentHelper extends BaseHelper {
    private Fragment mFragment;

    public FragmentHelper(Fragment fragment) {
        super(fragment.getContext().getApplicationContext(),
                (FlowParameters) fragment.getArguments().getParcelable(ExtraConstants.EXTRA_FLOW_PARAMS));
        mFragment = fragment;
    }

    public static Bundle getFlowParamsBundle(FlowParameters params) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ExtraConstants.EXTRA_FLOW_PARAMS, params);
        return bundle;
    }

    public void finish(int resultCode, Intent intent) {
        finishActivity(mFragment.getActivity(), resultCode, intent);
    }

    @Override
    public void showLoadingDialog(String message) {
        dismissDialog();
        ContextThemeWrapper context =
                new ContextThemeWrapper(mFragment.getContext(), getFlowParams().themeId);
        mProgressDialog = ProgressDialog.show(context, "", message, true);
    }

    public void startIntentSenderForResult(IntentSender sender, int requestCode)
            throws IntentSender.SendIntentException {
        mFragment.startIntentSenderForResult(sender, requestCode, null, 0, 0, 0, null);
    }
}
