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

package msa.auth.util.signincontainer;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import msa.auth.AuthUI;
import msa.auth.ErrorCodes;
import msa.auth.IdpResponse;
import msa.auth.ResultCodes;
import msa.auth.provider.FacebookProvider;
import msa.auth.provider.GoogleProvider;
import msa.auth.provider.IdpProvider;
import msa.auth.provider.IdpProvider.IdpCallback;
import msa.auth.provider.ProviderUtils;
import msa.auth.provider.TwitterProvider;
import msa.auth.ui.ExtraConstants;
import msa.auth.ui.FlowParameters;
import msa.auth.ui.FragmentBase;
import msa.auth.ui.FragmentHelper;
import msa.auth.ui.TaskFailureLogger;
import msa.auth.ui.User;
import msa.auth.ui.idp.CredentialSignInHandler;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class IdpSignInContainer extends FragmentBase implements IdpCallback {
    private static final String TAG = "IDPSignInContainer";
    private static final int RC_WELCOME_BACK_IDP = 4;

    private IdpProvider mIdpProvider;
    @Nullable
    private SaveSmartLock mSaveSmartLock;

    public static void signIn(FragmentActivity activity, FlowParameters parameters, User user) {
        FragmentManager fm = activity.getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (!(fragment instanceof IdpSignInContainer)) {
            IdpSignInContainer result = new IdpSignInContainer();

            Bundle bundle = FragmentHelper.getFlowParamsBundle(parameters);
            bundle.putParcelable(ExtraConstants.EXTRA_USER, user);
            bundle.putParcelable(ExtraConstants.EXTRA_FLOW_PARAMETERS, parameters);
            result.setArguments(bundle);

            try {
                fm.beginTransaction().add(result, TAG).disallowAddToBackStack().commit();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Cannot add fragment", e);
            }
        }
    }

    public static IdpSignInContainer getInstance(FragmentActivity activity) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment instanceof IdpSignInContainer) {
            return (IdpSignInContainer) fragment;
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSaveSmartLock = mHelper.getSaveSmartLockInstance(getActivity());

        User user = User.getUser(getArguments());
        String provider = user.getProvider();

        AuthUI.IdpConfig providerConfig = null;
        for (AuthUI.IdpConfig config : mHelper.getFlowParams().providerInfo) {
            if (config.getProviderId().equalsIgnoreCase(provider)) {
                providerConfig = config;
                break;
            }
        }

        if (providerConfig == null) {
            // we don't have a provider to handle this
            finish(ResultCodes.CANCELED, IdpResponse.getErrorCodeIntent(ErrorCodes.UNKNOWN_ERROR));
            return;
        }

        if (provider.equalsIgnoreCase(GoogleAuthProvider.PROVIDER_ID)) {
            mIdpProvider = new GoogleProvider(
                    getActivity(),
                    providerConfig,
                    user.getEmail());
        } else if (provider.equalsIgnoreCase(FacebookAuthProvider.PROVIDER_ID)) {
            mIdpProvider = new FacebookProvider(providerConfig, mHelper.getFlowParams().themeId);
        } else if (provider.equalsIgnoreCase(TwitterAuthProvider.PROVIDER_ID)) {
            mIdpProvider = new TwitterProvider(getContext());
        }

        mIdpProvider.setAuthenticationCallback(this);

        if (savedInstanceState == null) {
            mIdpProvider.startLogin(getActivity());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ExtraConstants.HAS_EXISTING_INSTANCE, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSuccess(final IdpResponse response) {
        Log.d(TAG, "onSuccess");
        AuthCredential credential = ProviderUtils.getAuthCredential(response);
        mHelper.getFirebaseAuth()
                .signInWithCredential(credential)
                .addOnFailureListener(
                        new TaskFailureLogger(TAG, "Failure authenticating with credential " +
                                credential.getProvider()))
                .addOnCompleteListener(new CredentialSignInHandler(
                        getActivity(),
                        mHelper,
                        mSaveSmartLock,
                        RC_WELCOME_BACK_IDP,
                        response));

        if (getArguments() != null && getArguments().containsKey(ExtraConstants.EXTRA_FLOW_PARAMETERS)) {
            FlowParameters flowParameters = getArguments().getParcelable(ExtraConstants.EXTRA_FLOW_PARAMETERS);
            if (flowParameters != null && flowParameters.intentToStartAfterSuccessfulLogin != null)
                launchReceivedActivity(new ComponentName(getActivity(), flowParameters.intentToStartAfterSuccessfulLogin));
        }

    }

    @Override
    public void onFailure(Bundle extra) {
        Log.d(TAG, "onFailure");
        finish(ResultCodes.CANCELED, IdpResponse.getErrorCodeIntent(ErrorCodes.UNKNOWN_ERROR));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_WELCOME_BACK_IDP) {
            Log.d(TAG, "onActivityResult -> requestCode == RC_WELCOME_BACK_IDP true");
            finish(resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult -> requestCode == RC_WELCOME_BACK_IDP false");
            mIdpProvider.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void launchReceivedActivity(ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        startActivity(intent);
    }
}
