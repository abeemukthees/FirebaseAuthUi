package msa.auth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RestrictTo;
import msa.auth.ui.ActivityHelper;
import msa.auth.ui.ExtraConstants;
import msa.auth.ui.FlowParameters;
import msa.auth.ui.HelperActivityBase;
import msa.auth.util.signincontainer.SignInDelegate;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class KickoffActivity extends HelperActivityBase {
    private static final String TAG = "KickoffActivity";
    private static final String IS_WAITING_FOR_PLAY_SERVICES = "is_waiting_for_play_services";
    private static final int RC_PLAY_SERVICES = 1;

    private boolean mIsWaitingForPlayServices = false;

    public static Intent createIntent(Context context, FlowParameters flowParams) {
        return ActivityHelper.createBaseIntent(context, KickoffActivity.class, flowParams);
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        start();
        Log.d(TAG, "onCreate");

        // This code checks for internet & playservices & finishes AuthPickerActivity/AuthUi Activity immediately,
        // So it has been disabled to keep the auth picker activity visible
        /*if (savedInstance == null || savedInstance.getBoolean(IS_WAITING_FOR_PLAY_SERVICES)) {
            if (isOffline()) {
                Log.d(TAG, "No network connection");
                finish(ResultCodes.CANCELED,
                        IdpResponse.getErrorCodeIntent(ErrorCodes.NO_NETWORK));
                return;
            }

            boolean isPlayServicesAvailable = PlayServicesHelper.makePlayServicesAvailable(
                    this,
                    RC_PLAY_SERVICES,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish(ResultCodes.CANCELED,
                                    IdpResponse.getErrorCodeIntent(
                                            ErrorCodes.UNKNOWN_ERROR));
                        }
                    });

            if (isPlayServicesAvailable) {
                start();
            } else {
                mIsWaitingForPlayServices = true;
            }
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // It doesn't matter what we put here, we just don't want outState to be empty
        outState.putBoolean(ExtraConstants.HAS_EXISTING_INSTANCE, true);
        outState.putBoolean(IS_WAITING_FOR_PLAY_SERVICES, mIsWaitingForPlayServices);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == RC_PLAY_SERVICES) {
            Log.d(TAG, "onActivityResult requestCode RC_PLAY_SERVICES = " + RC_PLAY_SERVICES);
            if (resultCode == ResultCodes.OK) {
                Log.d(TAG, "onActivityResult requestCode 2 = " + ResultCodes.OK);
                start();
            } else {
                Log.d(TAG, "onActivityResult requestCode Cancelled = " + ResultCodes.CANCELED);
                finish(ResultCodes.CANCELED,
                        IdpResponse.getErrorCodeIntent(ErrorCodes.UNKNOWN_ERROR));
            }
        } else {
            Log.d(TAG, "onActivityResult requestCode = something else ");
            SignInDelegate delegate = SignInDelegate.getInstance(this);
            if (delegate != null) delegate.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void start() {
        Log.d(TAG, "start()");
        FlowParameters flowParams = mActivityHelper.getFlowParams();
        SignInDelegate.delegate(this, flowParams);
    }

    /**
     * Check if there is an active or soon-to-be-active network connection.
     *
     * @return true if there is no network connection, false otherwise.
     */
    private boolean isOffline() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return !(manager != null
                && manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isConnectedOrConnecting());
    }
}
