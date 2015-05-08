package org.kidinov.justweight.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import org.kidinov.justweight.R;

public class RatingDialogFragment extends DialogFragment {
    public static final String KEY_LAUNCH_TIMES = "launch_times";
    public static final String KEY_WAS_RATED = "was_rated";


    public static RatingDialogFragment newInstance() {
        RatingDialogFragment f = new RatingDialogFragment();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity()).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                rateNow();
                dialog.dismiss();
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(KEY_WAS_RATED, true).commit();
                dialog.dismiss();
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
                remindMeLater();
                dialog.dismiss();
            }
        }).autoDismiss(false).positiveText(android.R.string.ok).negativeText(android.R.string.cancel).cancelable(false).title(R.string.rate_title)
                .content(R.string.rate_message).positiveText(R.string.rate_rate_now).neutralText(R.string.rate_remind_me_later)
                .negativeText(R.string.rate_no_thanks).build();
        materialDialog.setCanceledOnTouchOutside(false);
        return materialDialog;
    }

    public static void launch(ActionBarActivity ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int lauchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);

        if (lauchTimes > 5 && Math.random() < 0.3 && !pref.getBoolean(KEY_WAS_RATED, false)) {
            RatingDialogFragment.newInstance().show(ctx.getSupportFragmentManager(), "RatingDialogFragment");
        }

        pref.edit().putInt(KEY_LAUNCH_TIMES, ++lauchTimes).apply();
    }

    public void rateNow() {
        String appPackage = getActivity().getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(KEY_WAS_RATED, true).commit();
    }


    public void remindMeLater() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("launch_times", 0).commit();
    }


}