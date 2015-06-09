package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.kidinov.justweight.R;
import org.kidinov.justweight.activity.MainActivity;
import org.kidinov.justweight.adapter.WeightPickerAdapter;
import org.kidinov.justweight.model.Weight;
import org.kidinov.justweight.util.DbHelper;
import org.kidinov.justweight.util.EnvUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class WeightPickerDialogFragment extends BaseDialogFragment {
    public final static int WEIGHT_UNIT_PICK = 41;
    public final static int DATE_PICK = 42;
    private static final String TAG = "WeightPickerDialogFrag";


    private RecyclerView weightPicker;
    private LinearLayoutManager llm;
    private TextView unitType;
    private SharedPreferences preferences;
    private EditText dateValue;
    private boolean dialogOpened;

    public static WeightPickerDialogFragment newInstance() {
        WeightPickerDialogFragment f = new WeightPickerDialogFragment();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootV = LayoutInflater.from(act).inflate(R.layout.weight_picker_dialog, null);

        preferences = PreferenceManager.getDefaultSharedPreferences(act);

        final EditText weightValueEt = (EditText) rootV.findViewById(R.id.weight_value);
        weightPicker = (RecyclerView) rootV.findViewById(R.id.weight_picker);
        unitType = (TextView) rootV.findViewById(R.id.unit_type);
        dateValue = (EditText) rootV.findViewById(R.id.date);

        dateValue.setText(EnvUtil.getFormattedDate(System.currentTimeMillis()));
        dateValue.setOnClickListener(v -> {
            if (dialogOpened) {
                return;
            }
            dialogOpened = true;

            Date d = EnvUtil.getLocalFromString(dateValue.getText().toString());
            DialogFragment f = DatePickerDialog.newInstance(d);
            f.setTargetFragment(WeightPickerDialogFragment.this, DATE_PICK);
            f.show(getChildFragmentManager(), "DatePickerDialog");
        });

        unitType.setText(EnvUtil.getLocalUnitString(act));
        unitType.setOnClickListener(v -> {
            if (dialogOpened) {
                return;
            }
            dialogOpened = true;

            UnitsPickerDialogFragment f = UnitsPickerDialogFragment.newInstance();
            f.setTargetFragment(WeightPickerDialogFragment.this, WEIGHT_UNIT_PICK);
            f.show(getChildFragmentManager(), "UnitsPickerDialogFragment");
            preferences.edit().putInt("last_weight", getValue()).apply();
        });

        llm = new LinearLayoutManager(act, LinearLayoutManager.HORIZONTAL, false);
        weightPicker.setLayoutManager(llm);
        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(new WeightPickerAdapter(act));
        adapter.setFirstOnly(false);
        adapter.setDuration(500);
        weightPicker.setAdapter(adapter);

        weightPicker.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                weightValueEt.setText(EnvUtil.formatWeight(getValue()));
            }
        });

        Log.d(TAG, String.format("geting = %d", preferences.getInt("last_weight", 700)));

        new Handler().postDelayed(() -> {
            weightPicker.scrollToPosition(
                    (int) (preferences.getInt("last_weight", 700) + Math.ceil((llm.findLastVisibleItemPosition() - llm.findFirstVisibleItemPosition()) / 2)));
        }, 100);

        MaterialDialog materialDialog = new MaterialDialog.Builder(act).callback(mButtonCallback).autoDismiss(false).positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel).cancelable(false).customView(rootV, false).title(R.string.pick_weight_dialog_title).build();
        materialDialog.setCanceledOnTouchOutside(false);
        return materialDialog;
    }


    private int getValue() {
        return llm.findFirstVisibleItemPosition() + (llm.findLastVisibleItemPosition() - llm.findFirstVisibleItemPosition()) / 2;
    }

    private final MaterialDialog.ButtonCallback mButtonCallback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog materialDialog) {
//            int kg = EnvUtil.convertToKg(act, getValue());
            Log.d(TAG, String.format("Saving = %d", getValue()));
            preferences.edit().putInt("last_weight", getValue()).apply();

            long time = EnvUtil.getLocalFromString(dateValue.getText().toString()).getTime();
            Weight todayWeight = DbHelper.getRecordByDate(time);
            if (todayWeight == null) {
                todayWeight = new Weight(time, getValue(), EnvUtil.getCurrentUnit(act));
            } else {
                todayWeight.setValue(getValue());
                todayWeight.setUnit(EnvUtil.getCurrentUnit(act));
            }
            todayWeight.save();

            if (PreferenceManager.getDefaultSharedPreferences(act).getBoolean("fit", false)) {
                insertInFit(todayWeight, materialDialog);
            } else {
                ((MainActivity) getActivity()).updateData();
                materialDialog.dismiss();
            }
        }

        @Override
        public void onNegative(MaterialDialog materialDialog) {
            materialDialog.dismiss();
        }
    };

    private void insertInFit(Weight todayWeight, MaterialDialog materialDialog) {
        if (!((MainActivity) getActivity()).googleApiClient.isConnected()) {
            Log.i(TAG, String.format("NOT CONNECTED"));
            return;
        }
        DataSource dataSource = new DataSource.Builder().setAppPackageName(getActivity()).setDataType(DataType.TYPE_WEIGHT).setName(TAG + " - weight")
                .setType(DataSource.TYPE_RAW).build();

        DataSet dataSet = DataSet.create(dataSource);

        Log.i(TAG, String.format("inserting at time = %d", todayWeight.getDate()));
        DataPoint dataPoint = dataSet.createDataPoint().setTimestamp(todayWeight.getDate() + 1, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_WEIGHT).setFloat((float) EnvUtil.getKgValue(todayWeight, act) / 10);
        dataSet.add(dataPoint);

        Log.i(TAG, "Inserting the dataset in the History API");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                DataReadRequest readRequest = new DataReadRequest.Builder().read(DataType.TYPE_WEIGHT)
                        .setTimeRange(todayWeight.getDate() - 1, todayWeight.getDate() + 1, TimeUnit.MILLISECONDS).build();
                DataReadResult dataReadResult = Fitness.HistoryApi.readData(((MainActivity) getActivity()).googleApiClient, readRequest)
                        .await(1, TimeUnit.MINUTES);
                if (!dataReadResult.getDataSet(DataType.TYPE_WEIGHT).isEmpty()) {
                    DataDeleteRequest deleteRequest = new DataDeleteRequest.Builder()
                            .setTimeInterval(todayWeight.getDate() - 1, todayWeight.getDate() + 1, TimeUnit.MILLISECONDS).addDataType(DataType.TYPE_WEIGHT)
                            .build();
                    com.google.android.gms.common.api.Status deleteData = Fitness.HistoryApi
                            .deleteData(((MainActivity) getActivity()).googleApiClient, deleteRequest).await(1, TimeUnit.MINUTES);
                    if (!deleteData.isSuccess()) {
                        Log.i(TAG, "There was a problem inserting the dataset.");
                    } else {
                        Log.i(TAG, "Data was deleted successfully!");
                    }
                }

                com.google.android.gms.common.api.Status insertStatus = Fitness.HistoryApi.insertData(((MainActivity) getActivity()).googleApiClient, dataSet)
                        .await(1, TimeUnit.MINUTES);

                if (!insertStatus.isSuccess()) {
                    Log.i(TAG, "There was a problem inserting the dataset.");
                    return null;
                }

                Log.i(TAG, "Data insert was successful!");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((MainActivity) getActivity()).updateData();
                materialDialog.dismiss();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        dialogOpened = false;
        switch (requestCode) {
            case WEIGHT_UNIT_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    unitType.setText(EnvUtil.getLocalUnitString(act));
                    weightPicker.scrollToPosition(preferences.getInt("last_weight", 700));
                }
                break;
            case DATE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    CalendarDay day = data.getParcelableExtra("date");
                    dateValue.setText(EnvUtil.getFormattedDate(day.getDate().getTime()));
                }
                break;
        }
    }

}
