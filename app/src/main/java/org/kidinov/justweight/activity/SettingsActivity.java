package org.kidinov.justweight.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jenzz.materialpreference.CheckBoxPreference;

import org.kidinov.justweight.R;
import org.kidinov.justweight.dialog.FilePickerDialogFragment;
import org.kidinov.justweight.dialog.ProVersionDialogFragment;
import org.kidinov.justweight.dialog.UnitsPickerDialogFragment;
import org.kidinov.justweight.util.EnvUtil;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icons));
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, new SettingsFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        public static final int UNIT_SELECT = 423;

        private boolean dialogOpened;
        private GoogleApiClient googleApiClient;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            findPreference("unit").setSummary(EnvUtil.getLocalUnitString(getActivity()));
            findPreference("unit").setOnPreferenceClickListener(p -> {
                if (dialogOpened) {
                    return false;
                }
                dialogOpened = true;
                UnitsPickerDialogFragment fragment = UnitsPickerDialogFragment.newInstance();
                fragment.setTargetFragment(this, UNIT_SELECT);
                fragment.show(getChildFragmentManager(), "UnitsPickerDialogFragment");
                return true;
            });

            findPreference("export").setOnPreferenceClickListener(p -> {
                if (dialogOpened) {
                    return false;
                }
                dialogOpened = true;

                if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pro", false)) {
                    ProVersionDialogFragment fragment = ProVersionDialogFragment.newInstance();
                    fragment.setTargetFragment(this, 3123);
                    fragment.show(getChildFragmentManager(), "ProVersionDialogFragment");
                    return true;
                }

                FilePickerDialogFragment fragment = FilePickerDialogFragment.newInstance(FilePickerDialogFragment.EXPORT);
                fragment.setTargetFragment(this, 3123);
                fragment.show(getChildFragmentManager(), "FilePickerDialogFragment");
                return true;
            });

            findPreference("import_").setOnPreferenceClickListener(p -> {
                if (dialogOpened) {
                    return false;
                }
                dialogOpened = true;

                if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pro", false)) {
                    ProVersionDialogFragment fragment = ProVersionDialogFragment.newInstance();
                    fragment.setTargetFragment(this, 3123);
                    fragment.show(getChildFragmentManager(), "ProVersionDialogFragment");
                    return true;
                }

                FilePickerDialogFragment fragment = FilePickerDialogFragment.newInstance(FilePickerDialogFragment.IMPORT);
                fragment.setTargetFragment(this, 3123);
                fragment.show(getChildFragmentManager(), "FilePickerDialogFragment");
                return true;
            });


            findPreference("g_fit").setOnPreferenceChangeListener((p, b) -> {
                if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pro", false)) {
//                    ((CheckBoxPreference) findPreference("g_fit")).setChecked(false);
                    ProVersionDialogFragment fragment = ProVersionDialogFragment.newInstance();
                    fragment.setTargetFragment(this, 3123);
                    fragment.show(getChildFragmentManager(), "ProVersionDialogFragment");
                    return false;
                }

                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putBoolean("fit", !((CheckBoxPreference) findPreference("g_fit")).isChecked()).commit();

                return true;
            });

            findPreference("pro").setOnPreferenceClickListener(p -> {
                ((BaseActivity) getActivity()).buyPro();
                return true;
            });

            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pro", false)) {
                getPreferenceScreen().removePreference(findPreference("pro"));
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            dialogOpened = false;
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == UNIT_SELECT && resultCode == Activity.RESULT_OK) {
                findPreference("unit").setSummary(EnvUtil.getLocalUnitString(getActivity()));
            }
        }
    }
}