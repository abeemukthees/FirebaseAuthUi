/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package msa.auth.ui.idp;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.auth.AuthCredential;

import java.util.ArrayList;
import java.util.List;

import msa.auth.AuthUI;
import msa.auth.AuthUI.IdpConfig;
import msa.auth.IdpResponse;
import msa.auth.R;
import msa.auth.provider.EmailProvider;
import msa.auth.provider.FacebookProvider;
import msa.auth.provider.GoogleProvider;
import msa.auth.provider.IdpProvider;
import msa.auth.provider.IdpProvider.IdpCallback;
import msa.auth.provider.PhoneProvider;
import msa.auth.provider.Provider;
import msa.auth.provider.ProviderUtils;
import msa.auth.provider.TwitterProvider;
import msa.auth.ui.AppCompatBase;
import msa.auth.ui.BaseHelper;
import msa.auth.ui.FlowParameters;
import msa.auth.ui.TaskFailureLogger;
import msa.auth.ui.email.RegisterEmailActivity;
import msa.auth.util.PlayServicesHelper;
import msa.auth.util.signincontainer.SaveSmartLock;

import static msa.auth.AuthUI.NO_BACKGROUND;
import static msa.auth.AuthUI.PHONE_VERIFICATION_PROVIDER;
import static msa.auth.AuthUI.PH_BTN_DEFAULT_COLOR;

/**
 * Presents the list of authentication options for this app to the user. If an
 * identity provider option is selected, a {@link CredentialSignInHandler}
 * is launched to manage the IDP-specific sign-in flow. If email authentication is chosen,
 * the {@link RegisterEmailActivity} is started. if phone authentication is chosen, the
 * {@link msa.auth.ui.phone.PhoneVerificationActivity} is started.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AuthMethodPickerActivity extends AppCompatBase implements IdpCallback {

    private static final String TAG = "AuthMethodPicker";
    private static final String IS_WAITING_FOR_PLAY_SERVICES = "is_waiting_for_play_services";
    private static final int RC_PLAY_SERVICES = 11;
    private static final int RC_ACCOUNT_LINK = 3;

    private boolean mIsWaitingForPlayServices = false;

    private List<Provider> mProviders;
    @Nullable
    private SaveSmartLock mSaveSmartLock;

    private Drawable mPhoneAuthButtonBackgroundDrawable;


    public static Intent createIntent(Context context, FlowParameters flowParams) {
        return BaseHelper.createBaseIntent(context, AuthMethodPickerActivity.class, flowParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isForAuthPickerActivity = true;
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.auth_method_picker_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mSaveSmartLock = mActivityHelper.getSaveSmartLockInstance();

        populateIdpList(mActivityHelper.getFlowParams().providerInfo);


        int logoId = mActivityHelper.getFlowParams().logoId;
        if (logoId == AuthUI.NO_LOGO) {

            findViewById(R.id.logo_layout).setVisibility(View.GONE);
        } else {
            ImageView logo = (ImageView) findViewById(R.id.logo);
            logo.setImageResource(logoId);
        }

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        int backgroundId = mActivityHelper.getFlowParams().backgroundId;
        if (backgroundId != NO_BACKGROUND) coordinatorLayout.setBackgroundResource(backgroundId);

    }


    private void populateIdpList(List<IdpConfig> providers) {
        mProviders = new ArrayList<>();
        for (IdpConfig idpConfig : providers) {
            switch (idpConfig.getProviderId()) {
                case AuthUI.GOOGLE_PROVIDER:
                    mProviders.add(new GoogleProvider(this, idpConfig));
                    break;
                case AuthUI.FACEBOOK_PROVIDER:
                    mProviders.add(new FacebookProvider(
                            idpConfig, mActivityHelper.getFlowParams().themeId));
                    break;
                case AuthUI.TWITTER_PROVIDER:
                    mProviders.add(new TwitterProvider(this));
                    break;
                case AuthUI.EMAIL_PROVIDER:
                    mProviders.add(new EmailProvider(this, mActivityHelper));
                    break;
                case AuthUI.PHONE_VERIFICATION_PROVIDER:
                    mProviders.add(new PhoneProvider(this, mActivityHelper));
                    break;
                default:
                    Log.e(TAG, "Encountered unknown provider parcel with type: "
                            + idpConfig.getProviderId());
            }
        }

        ViewGroup btnHolder = (ViewGroup) findViewById(R.id.btn_holder);
        for (final Provider provider : mProviders) {
            View loginButton = getLayoutInflater()
                    .inflate(provider.getButtonLayout(), btnHolder, false);

            if (provider.getProviderId().equals(PHONE_VERIFICATION_PROVIDER) && mActivityHelper.getFlowParams().phoneButtonBackgroundColor != PH_BTN_DEFAULT_COLOR) {

                mPhoneAuthButtonBackgroundDrawable = ContextCompat.getDrawable(this, R.drawable.idp_button_background_phone);
                mPhoneAuthButtonBackgroundDrawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, mActivityHelper.getFlowParams().phoneButtonBackgroundColor), PorterDuff.Mode.SRC_IN));
                loginButton.setBackground(mPhoneAuthButtonBackgroundDrawable);

            }

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (shouldAllowLogin()) {

                        if (provider instanceof IdpProvider) {
                            mActivityHelper.showLoadingDialog(R.string.progress_dialog_loading);
                        }

                        provider.startLogin(AuthMethodPickerActivity.this);

                    } else if (isOffline())
                        Snackbar.make(findViewById(android.R.id.content), R.string.error_msg_no_internet, Snackbar.LENGTH_SHORT).show();
                    else if (!checkIfPlayServicesAvailable())
                        Snackbar.make(findViewById(android.R.id.content), R.string.error_msg_no_play_services, Snackbar.LENGTH_SHORT).show();
                    else
                        Snackbar.make(findViewById(android.R.id.content), R.string.error_msg_login, Snackbar.LENGTH_SHORT).show();
                }
            });
            if (provider instanceof IdpProvider) {
                ((IdpProvider) provider).setAuthenticationCallback(this);
            }
            btnHolder.addView(loginButton);
        }
    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult = " + resultCode);
        if (requestCode == RC_ACCOUNT_LINK) {
            Log.d(TAG, "onActivityResult -> requestCode == RC_ACCOUNT_LINK");
            launchReceivedActivity();
            finish(resultCode, data);
        } else if (resultCode == ResultCodes.OK) {
            Log.d(TAG, "onActivityResult -> resultCode == ResultCodes.OK");
            launchReceivedActivity();
            finish(resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult -> else");
            for (Provider provider : mProviders) {
                provider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ACCOUNT_LINK) {
            finish(resultCode, data);
        } else {
            for (Provider provider : mProviders) {
                provider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onSuccess(final IdpResponse response) {
        Log.d(TAG, "onSuccess");
        AuthCredential credential = ProviderUtils.getAuthCredential(response);
        mActivityHelper.getFirebaseAuth()
                .signInWithCredential(credential)
                .addOnFailureListener(
                        new TaskFailureLogger(TAG, "Firebase sign in with credential "
                                + credential.getProvider() + " unsuccessful. " +
                                "Visit https://console.firebase.google.com to enable it."))
                .addOnCompleteListener(new CredentialSignInHandler(
                        this,
                        mActivityHelper,
                        mSaveSmartLock,
                        RC_ACCOUNT_LINK,
                        response));
    }

    @Override
    public void onFailure(Bundle extra) {
        // stay on this screen
        mActivityHelper.dismissDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mProviders != null) {
            for (Provider provider : mProviders) {
                if (provider instanceof GoogleProvider) {
                    ((GoogleProvider) provider).disconnect();
                }
            }
        }
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

    private boolean checkIfPlayServicesAvailable() {
        return PlayServicesHelper.makePlayServicesAvailable(
                this,
                RC_PLAY_SERVICES,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //finish(ResultCodes.CANCELED, IdpResponse.getErrorCodeIntent(ErrorCodes.UNKNOWN_ERROR));
                        Snackbar.make(findViewById(android.R.id.content), "Play services not available", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });

    }

    private boolean shouldAllowLogin() {
        return !isOffline() && checkIfPlayServicesAvailable();
    }

    private void launchReceivedActivity() {
        Log.d(TAG, "launchReceivedActivity");
        if (mActivityHelper.getFlowParams().intentToStartAfterSuccessfulLogin != null) {
            //Log.d(TAG, "Received activity name 1 = " + mActivityHelper.getFlowParams().intentToStartAfterSuccessfulLogin);
            ComponentName componentName = new ComponentName(this, mActivityHelper.getFlowParams().intentToStartAfterSuccessfulLogin);
            //Log.d(TAG, "componentName = " + componentName.getClass().getSimpleName());
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
