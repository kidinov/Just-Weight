package org.kidinov.justweight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

import org.kidinov.justweight.R;
import org.kidinov.justweight.adapter.FilesAdapter;
import org.kidinov.justweight.util.DbHelper;
import org.kidinov.justweight.util.EnvUtil;

import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;

public class FilePickerDialogFragment extends DialogFragment {
    public final static int EXPORT = 415;
    public final static int IMPORT = 324;

    private RecyclerView filesPicker;
    private LinearLayoutManager llm;

    public int operation;
    private View rootV;
    public MaterialEditText fileNameEt;
    private MaterialDialog materialDialog;

    public static FilePickerDialogFragment newInstance(int operation) {
        FilePickerDialogFragment f = new FilePickerDialogFragment();
        Bundle b = new Bundle();
        b.putInt("oper", operation);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.operation = getArguments().getInt("oper");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        rootV = LayoutInflater.from(getActivity()).inflate(R.layout.file_picker_dialog, null);

        fileNameEt = (MaterialEditText) rootV.findViewById(R.id.file_name);
        if (operation == IMPORT) {
            fileNameEt.setVisibility(View.GONE);
        } else {
            fileNameEt.setText(String.format("jw_%s.jw", EnvUtil.getFormattedDate(System.currentTimeMillis())));
            fileNameEt.addValidator(new RegexpValidator(getString(R.string.file_name_is_not_valid), "(.*/)*.+\\.(\\w+)$"));
            fileNameEt.setFloatingLabelAlwaysShown(true);
            fileNameEt.setFloatingLabelAnimating(true);
            fileNameEt.setFloatingLabel(1);
            fileNameEt.setFloatingLabelText(getString(R.string.file_name));
        }

        filesPicker = (RecyclerView) rootV.findViewById(R.id.files);

        llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        filesPicker.setLayoutManager(llm);
        SlideInBottomAnimationAdapter adapter = new SlideInBottomAnimationAdapter(
                new FilesAdapter(filesPicker, Environment.getExternalStorageDirectory(), this));
        adapter.setFirstOnly(true);
        filesPicker.setAdapter(adapter);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).callback(mButtonCallback).autoDismiss(false)
                .positiveText(android.R.string.ok).negativeText(android.R.string.cancel).customView(rootV, false)
                .title(operation == IMPORT ? R.string.pick_file : R.string.pick_folder);
        if (operation == IMPORT) {
            builder.positiveText("");
        }
        materialDialog = builder.build();
        return materialDialog;
    }

    public void positiveClickAction() {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                rootV.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }

            @Override
            protected Integer doInBackground(String... strings) {
                if (operation == EXPORT) {
                    return DbHelper.exportAllDataToFile(strings[0]);
                } else if (operation == IMPORT) {
                    return DbHelper.importDataFromFile(strings[0]);
                }
                return -1;
            }

            @Override
            protected void onPostExecute(Integer aVoid) {
                super.onPostExecute(aVoid);
                if (aVoid < 0) {
                    Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                    rootV.findViewById(R.id.progress).setVisibility(View.GONE);
                    return;
                }
                new Handler().postDelayed(() -> {
                    rootV.findViewById(R.id.progress).setVisibility(View.GONE);
                    if (getActivity() != null) Toast.makeText(getActivity(), R.string.done, Toast.LENGTH_LONG).show();
                    materialDialog.dismiss();
                }, 1000);
            }
        }.execute(FilesAdapter.file.getAbsolutePath() + "/" + fileNameEt.getText().toString());
    }


    private final MaterialDialog.ButtonCallback mButtonCallback = new MaterialDialog.ButtonCallback() {

        @Override
        public void onPositive(MaterialDialog materialDialog) {
            if (!fileNameEt.validate()) {
                return;
            }

            positiveClickAction();
        }

        @Override
        public void onNegative(MaterialDialog materialDialog) {
            materialDialog.dismiss();
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        Fragment f = FilePickerDialogFragment.this.getTargetFragment();
        if (f != null) {
            f.onActivityResult(FilePickerDialogFragment.this.getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        }
    }

}
