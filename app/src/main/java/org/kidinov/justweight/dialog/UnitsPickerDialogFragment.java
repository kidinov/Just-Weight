package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import org.kidinov.justweight.R;


/**
 * Created by akid on 17/04/15.
 */
public class UnitsPickerDialogFragment extends BaseDialogFragment {
    private MaterialDialog dlg;

    public static UnitsPickerDialogFragment newInstance() {
        UnitsPickerDialogFragment f = new UnitsPickerDialogFragment();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dlg = new MaterialDialog.Builder(getActivity()).title(R.string.weight_unit)
                .items(new CharSequence[]{getString(R.string.kilograms), getString(R.string.pounds)})
                .itemsCallbackSingleChoice(preferences.getString("units", "kg").equals("kg") ? 0 : 1, (materialDialog, view, i, charSequence) -> true)
                .callback(mButtonCallback).autoDismiss(false).cancelable(false).positiveText(android.R.string.ok).build();
        dlg.setCanceledOnTouchOutside(false);
        return dlg;
    }

    private final MaterialDialog.ButtonCallback mButtonCallback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog materialDialog) {
            Fragment f = UnitsPickerDialogFragment.this.getTargetFragment();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("units", dlg.getSelectedIndex() == 0 ? "kg" : "lbs").commit();
            if (f != null) {
                f.onActivityResult(UnitsPickerDialogFragment.this.getTargetRequestCode(), Activity.RESULT_OK, null);
            }
            materialDialog.dismiss();
        }

        @Override
        public void onNegative(MaterialDialog materialDialog) {
            Fragment f = UnitsPickerDialogFragment.this.getTargetFragment();
            if (f != null) {
                f.onActivityResult(UnitsPickerDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
            materialDialog.dismiss();
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        Fragment f = UnitsPickerDialogFragment.this.getTargetFragment();
        if (f != null) {
            f.onActivityResult(UnitsPickerDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        }

        super.onDismiss(dialog);
    }
}
