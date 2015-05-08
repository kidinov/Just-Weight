package org.kidinov.justweight.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.kidinov.justweight.App;
import org.kidinov.justweight.R;
import org.kidinov.justweight.billing.IabHelper;
import org.kidinov.justweight.util.EnvUtil;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends ActionBarActivity {
    private static final String TAG = "BaseActivity";
    private IabHelper iabHelper;
    private final String SKU_PREMIUM = "premium";

    IabHelper.QueryInventoryFinishedListener gotInventoryListener = (result, inventory) -> {

        if (result.isFailure()) {
            Log.e("IAP", "message =" + result);
        } else if (inventory.hasPurchase(SKU_PREMIUM)){
            PreferenceManager.getDefaultSharedPreferences(BaseActivity.this).edit().putBoolean("pro", true).commit();
        }
    };

    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = (result, purchase) -> {
        if (result.isFailure()) {
            Log.d("IAP", "Error purchasing: " + result);
            Log.e("IAP", "message =" + result);
        } else if (purchase.getSku().equals(SKU_PREMIUM)) {
            PreferenceManager.getDefaultSharedPreferences(BaseActivity.this).edit().putBoolean("pro", true).commit();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iabHelper = new IabHelper(this, EnvUtil.getPubKey());
        iabHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                Log.e("IAP", "message =" + result);
                Toast.makeText(BaseActivity.this, R.string.iap_not_available, Toast.LENGTH_LONG).show();
                return;
            }
            List additionalSkuList = new ArrayList<>();
            additionalSkuList.add(SKU_PREMIUM);
            iabHelper.queryInventoryAsync(true, additionalSkuList, gotInventoryListener);
        });

        Tracker t = ((App) getApplication()).getTracker();
        t.setScreenName(getClass().getName());
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper != null && !iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void buyPro() {
        try {
            iabHelper.launchPurchaseFlow(this, SKU_PREMIUM, 10001, purchaseFinishedListener, "");
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) iabHelper.dispose();
        iabHelper = null;
    }
}
