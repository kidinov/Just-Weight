package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Date;

import org.kidinov.justweight.R;

/**
 * Created by akid on 18/04/15.
 */
public class DatePickerDialog extends BaseDialogFragment {
    public final static int FROM_DATE_PICKED = 1;
    public final static int TO_DATE_PICKED = 2;
    private static final String TAG = "DatePickerDialog";

    private Date date;
    private Date maxDate;
    private Date minDate;
    private int type;

    public static DatePickerDialog newInstance(Date d) {
        DatePickerDialog f = new DatePickerDialog();
        Bundle b = new Bundle();
        b.putSerializable("date", d);
        b.putSerializable("max_date", new Date());
        f.setArguments(b);
        return f;
    }

    public static DatePickerDialog newInstance(Date d, int type, Date maxMinDate) {
        DatePickerDialog f = new DatePickerDialog();
        Bundle b = new Bundle();
        b.putSerializable("date", d);
        b.putInt("type", type);
        b.putSerializable("max_date", new Date());
        if (type == FROM_DATE_PICKED) {
            b.putSerializable("max_date", maxMinDate);
        } else if (type == TO_DATE_PICKED) {
            b.putSerializable("min_date", maxMinDate);
        }
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            date = (Date) getArguments().getSerializable("date");
            type = getArguments().getInt("type");
            maxDate = (Date) getArguments().getSerializable("max_date");
            minDate = (Date) getArguments().getSerializable("min_date");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(act).inflate(R.layout.date_picker_dialog, null);
        MaterialCalendarView calendarView = (MaterialCalendarView) v.findViewById(R.id.calendarView);
        if (maxDate != null) {
            Log.d(TAG, String.format("Set max date = %s", maxDate));
            calendarView.setMaximumDate(new Date());
        }
        Log.d(TAG, String.format("Set current date = %s", date));

        calendarView.setCurrentDate(date);
        calendarView.setSelectedDate(date);

        MaterialDialog dlg = new MaterialDialog.Builder(getActivity()).customView(v, false).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                Fragment f = DatePickerDialog.this.getTargetFragment();
                if (f != null) {
                    Intent i = new Intent();
                    i.putExtra("date", calendarView.getSelectedDate());
                    f.onActivityResult(DatePickerDialog.this.getTargetRequestCode(), Activity.RESULT_OK, i);
                }

                dialog.dismiss();
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);

                dialog.dismiss();

                Fragment f = DatePickerDialog.this.getTargetFragment();
                if (f != null) {
                    f.onActivityResult(DatePickerDialog.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                }
            }
        }).autoDismiss(false).positiveText(android.R.string.ok).negativeText(android.R.string.cancel).title(R.string.date).build();
        return dlg;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Fragment f = DatePickerDialog.this.getTargetFragment();
        if (f != null) {
            f.onActivityResult(DatePickerDialog.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        }
        super.onDismiss(dialog);
    }
}
