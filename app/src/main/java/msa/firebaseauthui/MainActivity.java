package msa.firebaseauthui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import msa.auth.AuthUI;

public class MainActivity extends AppCompatActivity {

    FirebaseApp firebaseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseApp = FirebaseApp.initializeApp(this);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                login();
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                logout();
            }
        });
    }

    private void login() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        startActivityForResult(
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
                        .build(), 1);


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
