package msa.auth.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("Registered")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class HelperActivityBase extends AppCompatActivity {

    protected ActivityHelper mActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mActivityHelper = new ActivityHelper(this, getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityHelper.dismissDialog();
    }

    public void finish(int resultCode, Intent intent) {
        mActivityHelper.finish(resultCode, intent);
    }
}

