package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import org.kidinov.justweight.R;
import org.kidinov.justweight.activity.BaseActivity;


/**
 * Created by akid on 17/04/15.
 */
public class ProVersionDialogFragment extends DialogFragment {
    private MaterialDialog dlg;

    public static ProVersionDialogFragment newInstance() {
        ProVersionDialogFragment f = new ProVersionDialogFragment();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dlg = new MaterialDialog.Builder(getActivity()).title(R.string.pro_version).content(R.string.pro_version_text)
                .callback(mButtonCallback).autoDismiss(false).cancelable(false).positiveText(android.R.string.ok).negativeText(android.R.string.cancel).build();
        dlg.setCanceledOnTouchOutside(false);
        return dlg;
    }

    private final MaterialDialog.ButtonCallback mButtonCallback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog materialDialog) {
            ((BaseActivity)getActivity()).buyPro();
            Fragment f = ProVersionDialogFragment.this.getTargetFragment();
            if (f != null) {
                f.onActivityResult(ProVersionDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
        }

        @Override
        public void onNegative(MaterialDialog materialDialog) {
            materialDialog.dismiss();
            Fragment f = ProVersionDialogFragment.this.getTargetFragment();
            if (f != null) {
                f.onActivityResult(ProVersionDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        Fragment f = ProVersionDialogFragment.this.getTargetFragment();
        if (f != null) {
            f.onActivityResult(ProVersionDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        }

        super.onDismiss(dialog);
    }
}
