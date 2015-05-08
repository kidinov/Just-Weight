package org.kidinov.justweight.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.kidinov.justweight.R;
import org.kidinov.justweight.dialog.DatePickerDialog;
import org.kidinov.justweight.util.EnvUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by akid on 20/04/15.
 */
public abstract class BaseFragment extends Fragment {
    protected EditText fromDate;
    protected EditText toDate;
    protected View refresh;

    private boolean dialogOpened;

    protected void initView(View v, Bundle b) {
        fromDate = (EditText) v.findViewById(R.id.from_et);
        if (b == null) fromDate.setText(EnvUtil.getFormattedDate(EnvUtil.getDateMinusDays(-20).getTime()));
        else fromDate.setText(b.getString("from"));
        fromDate.setOnClickListener(view -> {
            if (dialogOpened) {
                return;
            }
            dialogOpened = true;

            Date frD = EnvUtil.getLocalFromString(fromDate.getText().toString());
            Date toD = EnvUtil.getLocalFromString(toDate.getText().toString());
            Calendar c = Calendar.getInstance();
            c.setTime(toD);
            c.add(Calendar.DATE, -1);
            DialogFragment f = DatePickerDialog.newInstance(frD, DatePickerDialog.FROM_DATE_PICKED, c.getTime());
            f.setTargetFragment(this, DatePickerDialog.FROM_DATE_PICKED);
            f.show(getActivity().getSupportFragmentManager(), "DatePickerDialog");
        });

        toDate = (EditText) v.findViewById(R.id.to_et);
        if (b == null) toDate.setText(EnvUtil.getFormattedDate(EnvUtil.getDateMinusDays(0).getTime()));
        else toDate.setText(b.getString("to"));
        toDate.setOnClickListener(view -> {
            if (dialogOpened) {
                return;
            }
            dialogOpened = true;

            Date frD = EnvUtil.getLocalFromString(fromDate.getText().toString());
            Date toD = EnvUtil.getLocalFromString(toDate.getText().toString());
            Calendar c = Calendar.getInstance();
            c.setTime(frD);
            c.add(Calendar.DATE, +1);
            DialogFragment f = DatePickerDialog.newInstance(toD, DatePickerDialog.TO_DATE_PICKED, c.getTime());
            f.setTargetFragment(this, DatePickerDialog.TO_DATE_PICKED);
            f.show(getActivity().getSupportFragmentManager(), "DatePickerDialog");
        });

        refresh = v.findViewById(R.id.refresh);
        refresh.setOnClickListener(view -> {
            updateData();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        dialogOpened = false;

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case DatePickerDialog.FROM_DATE_PICKED:
                CalendarDay day = data.getParcelableExtra("date");
                fromDate.setText(EnvUtil.getFormattedDate(day.getDate().getTime()));
                break;
            case DatePickerDialog.TO_DATE_PICKED:
                day = data.getParcelableExtra("date");
                toDate.setText(EnvUtil.getFormattedDate(day.getDate().getTime()));
                break;
        }

        updateData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("from", fromDate.getText().toString());
        outState.putString("to", toDate.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public abstract void updateData();
}
