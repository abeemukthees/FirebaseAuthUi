package msa.auth.ui.email;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import msa.auth.R;
import msa.auth.ResultCodes;
import msa.auth.ui.DialogBase;
import msa.auth.ui.ExtraConstants;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class RecoveryEmailSentDialog extends DialogBase {
    private static final String TAG = "RecoveryEmailSentDialog";

    public static void show(String email, FragmentManager manager) {
        RecoveryEmailSentDialog result = new RecoveryEmailSentDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstants.EXTRA_EMAIL, email);
        result.setArguments(bundle);
        result.show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_confirm_recover_password)
                .setMessage(getString(R.string.confirm_recovery_body,
                        getArguments().getString(ExtraConstants.EXTRA_EMAIL)))
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface anInterface) {
                        finish(ResultCodes.OK, new Intent());
                    }
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
