package msa.auth.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class FragmentBase extends Fragment {
    protected FragmentHelper mHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new FragmentHelper(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHelper.dismissDialog();
    }

    public void finish(int resultCode, Intent resultIntent) {
        mHelper.finish(resultCode, resultIntent);
    }
}
