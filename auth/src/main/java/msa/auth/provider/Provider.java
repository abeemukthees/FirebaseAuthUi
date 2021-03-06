package msa.auth.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.LayoutRes;

public interface Provider {
    /**
     * Retrieves the name of the IDP, for display on-screen.
     */
    String getName(Context context);

    /**
     * Retrieves the id of the IDP, e.g. {@link GoogleAuthProvider#PROVIDER_ID}.
     */
    String getProviderId();

    /**
     * Retrieves the layout id of the button to inflate and/or set a click listener.
     */
    @LayoutRes
    int getButtonLayout();

    /**
     * Start the login process for the IDP, e.g. show the Google sign-in activity.
     */
    void startLogin(Activity activity);

    /**
     * Handle the sign result by either finishing the calling activity or sending an {@link
     * IdpProvider.IdpCallback} response.
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
