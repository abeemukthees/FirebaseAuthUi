package msa.firebaseauthui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import msa.auth.AuthUI;
import msa.auth.ResultCodes;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    FirebaseApp firebaseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseApp = FirebaseApp.initializeApp(this);

        FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                login();
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                logout();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            if (resultCode == ResultCodes.CANCELED) finish();
        }
    }

    private void login() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = AuthUI.getInstance(firebaseApp)
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(true)
                .setAvailableProviders(
                        Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                        ))
                .alwaysShowAuthMethodPicker(true)
                .setLogo(R.drawable.ic_store_mall_directory)
                .setBackgroundDrawable(R.drawable.bg_auth_picker)
                .setIntentAfterSuccessfulLogin(null)
                .build();
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 11);

       /* startActivityForResult(
                AuthUI.getInstance(firebaseApp)
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(true)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                ))
                        .alwaysShowAuthMethodPicker(true)
                        .setLogo(R.drawable.ic_store_mall_directory)
                        .setBackgroundDrawable(R.drawable.bg_auth_picker)
                        .setIntentAfterSuccessfulLogin(HomeActivity.class.getName())
                        .build(), 1);*/


        /*if (user != null) {
            showToastMessage("Already Signed in");
        } else {
            // No user is signed in
            startActivityForResult(
                    AuthUI.getInstance(firebaseApp)
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                    ))
                            .alwaysShowAuthMethodPicker(true)
                            .setLogo(R.drawable.ic_store_mall_directory)
                            .setBackgroundDrawable(R.drawable.bg_auth_picker)
                            .setIntentAfterSuccessfulLogin(HomeActivity.class.getName())
                            .build(), 1);
        }*/

    }

    private void logout() {

        AuthUI.getInstance(firebaseApp)
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        showToastMessage("Signed out");

                    }
                });

    }

    protected void showToastMessage(String message) {
        if (message != null && message.length() > 0)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
