package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import org.kidinov.justweight.activity.OnDialogDissmissed;

/**
 * Created by akid on 17/04/15.
 */
public abstract class BaseDialogFragment extends DialogFragment{
    protected Activity act;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.act = activity;
    }

    @Override
    public void onDetach() {
        this.act = null;
        super.onDetach();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (act != null && act instanceof OnDialogDissmissed) {
            ((OnDialogDissmissed) act).onDialogDissmised(this);
        }
        super.onDismiss(dialog);
    }
}
