package org.kidinov.justweight.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.splunk.mint.Mint;

import org.kidinov.justweight.App;
import org.kidinov.justweight.R;
import org.kidinov.justweight.adapter.ViewPagerAdapter;
import org.kidinov.justweight.dialog.RatingDialogFragment;
import org.kidinov.justweight.dialog.UnitsPickerDialogFragment;
import org.kidinov.justweight.dialog.WeightPickerDialogFragment;
import org.kidinov.justweight.fragment.BaseFragment;
import org.kidinov.justweight.model.Weight;
import org.kidinov.justweight.util.DbHelper;
import org.kidinov.justweight.util.EnvUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;


public class MainActivity extends BaseActivity implements OnDialogDissmissed {
    private static final String TAG = "MainActivity";
    private static final int SETTING = 3123;

    private ButtonFloat addWeightButton;
    private MaterialTabHost tabHost;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private boolean dialogOpened;

    private InterstitialAd interstitial;

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    public GoogleApiClient googleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.initAndStartSession(MainActivity.this, "35d897f1");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }

//        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("pro", true).commit();
        if (App.IS_TESTING) DbHelper.fillTestData();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icons));
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        tabHost = (MaterialTabHost) this.findViewById(R.id.tabHost);
        pager = (ViewPager) this.findViewById(R.id.pager);

        adapter = new ViewPagerAdapter(this);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < adapter.getCount(); i++) {
            tabHost.addTab(tabHost.newTab().setText(adapter.getPageTitle(i)).setTabListener(new MaterialTabListener() {
                @Override
                public void onTabSelected(MaterialTab materialTab) {
                    pager.setCurrentItem(materialTab.getPosition());
                }

                @Override
                public void onTabReselected(MaterialTab materialTab) {
                }

                @Override
                public void onTabUnselected(MaterialTab materialTab) {
                }
            }));
        }

        addWeightButton = (ButtonFloat) findViewById(R.id.add_button);
        addWeightButton.setBackgroundColor(getResources().getColor(R.color.accent));
        addWeightButton.setRippleColor(getResources().getColor(R.color.accent_ripple));

        addWeightButton.setOnClickListener(view -> {
            if (dialogOpened) {
                return;
            }
            dialogOpened = true;

            WeightPickerDialogFragment.newInstance().show(getSupportFragmentManager(), "WeightPickerDialogFragment");
        });

        if (savedInstanceState == null) {
            if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("first_start", true)) {
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("first_start", false).apply();
                UnitsPickerDialogFragment.newInstance().show(getSupportFragmentManager(), "UnitsPickerDialogFragment");
            } else if (DbHelper.getTodaysRecord() == null) {
                showAds();
                WeightPickerDialogFragment.newInstance().show(getSupportFragmentManager(), "WeightPickerDialogFragment");
            } else {
                showAds();
                animateAddButton();
            }
        } else {
            addWeightButton.setVisibility(View.VISIBLE);
        }

        RatingDialogFragment.launch(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
    }

    private void buildFitnessClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Fitness.HISTORY_API).addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Connected!!!");
                        if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("fit", false)) {
                            Calendar cal = Calendar.getInstance();
                            Date now = new Date();
                            cal.setTime(now);
                            long endTime = cal.getTimeInMillis();
                            cal.add(Calendar.YEAR, -5);
                            long startTime = cal.getTimeInMillis();

                            DataReadRequest readRequest = new DataReadRequest.Builder().read(DataType.TYPE_WEIGHT)
                                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
                            new Thread(() -> {
                                DataReadResult dataReadResult = Fitness.HistoryApi.readData(googleApiClient, readRequest).await(1, TimeUnit.MINUTES);
                                boolean newData = false;
                                for (DataPoint dp : dataReadResult.getDataSet(DataType.TYPE_WEIGHT).getDataPoints()) {
                                    Log.d(TAG, String.format("startTime = %d", dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.d(TAG, String.format("dp weight = %f", dp.getValue(Field.FIELD_WEIGHT).asFloat()));
                                    long time = EnvUtil.getLocalFromString(EnvUtil.getFormattedDate(dp.getStartTime(TimeUnit.MILLISECONDS))).getTime();
                                    Weight todayWeight = DbHelper.getRecordByDate(time);
                                    if (todayWeight == null) {
                                        todayWeight = new Weight(time, (int) (dp.getValue(Field.FIELD_WEIGHT).asFloat() * 10), "kg");
                                        todayWeight.save();
                                        newData = true;
                                    }
                                }

                                if (newData) {
                                    runOnUiThread(() -> {
                                        new Handler().postDelayed(() -> {
                                            Toast.makeText(MainActivity.this, R.string.new_data_from_g_fit, Toast.LENGTH_LONG).show();
                                            updateData();
                                        }, 1000);
                                    });
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                        }
                    }
                }).addOnConnectionFailedListener(result -> {
                    Log.i(TAG, "Connection failed. Cause: " + result.toString());
                    if (!result.hasResolution()) {
                        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), MainActivity.this, 0).show();
                        return;
                    }
                    if (!authInProgress) {
                        try {
                            Log.i(TAG, "Attempting to resolve failed connection");
                            authInProgress = true;
                            result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Exception while starting resolution activity", e);
                        }
                    }
                }).build();
    }

    public void showAds() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pro", false)) {
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId("ca-app-pub-8714215189705577/1690827345");
            AdRequest adRequest = new AdRequest.Builder().build();
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if (Math.random() > 0.5) {
                        new Handler().postDelayed(interstitial::show, 5000);
                    }
                }
            });
            interstitial.loadAd(adRequest);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DbHelper.getTodaysRecord() == null) {
            addWeightButton.setDrawableIcon(getResources().getDrawable(R.drawable.add));
        } else {
            addWeightButton.setDrawableIcon(getResources().getDrawable(R.drawable.edit));
        }
    }


    private void animateAddButton() {
        Animation slideUp = new TranslateAnimation(0, 0, 500, 0);
        slideUp.setDuration(500);
        addWeightButton.setAnimation(slideUp);
        addWeightButton.setVisibility(View.VISIBLE);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateData();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        slideUp.start();
    }

    public void updateData() {
        Fragment fragment = adapter.getActiveFragment(pager, 0);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).updateData();
        }

        fragment = adapter.getActiveFragment(pager, 1);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).updateData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDialogDissmised(DialogFragment dialog) {
        dialogOpened = false;
        if (dialog instanceof UnitsPickerDialogFragment) {
            if (getSupportFragmentManager().findFragmentByTag("WeightPickerDialogFragment") == null) {
                WeightPickerDialogFragment.newInstance().show(getSupportFragmentManager(), "WeightPickerDialogFragment");
            }
        } else if (dialog instanceof WeightPickerDialogFragment) {
            if (DbHelper.getTodaysRecord() == null) {
                addWeightButton.setDrawableIcon(getResources().getDrawable(R.drawable.add));
            } else {
                addWeightButton.setDrawableIcon(getResources().getDrawable(R.drawable.edit));
            }

            if (addWeightButton.getVisibility() == View.INVISIBLE) {
                animateAddButton();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTING);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTING && resultCode == RESULT_OK) {
            updateData();
        }
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (googleApiClient != null && !googleApiClient.isConnecting() && !googleApiClient.isConnected() && PreferenceManager
                        .getDefaultSharedPreferences(this).getBoolean("fit", false)) {
                    googleApiClient.connect();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        if (googleApiClient != null && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("fit", false)) googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

}
