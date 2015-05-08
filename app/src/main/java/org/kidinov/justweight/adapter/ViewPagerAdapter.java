package org.kidinov.justweight.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import org.kidinov.justweight.R;
import org.kidinov.justweight.fragment.ChartFragment;
import org.kidinov.justweight.fragment.HistoryFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final ActionBarActivity ctx;

    public ViewPagerAdapter(ActionBarActivity ctx) {
        super(ctx.getSupportFragmentManager());
        this.ctx = ctx;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChartFragment.newInstance();
            case 1:
                return HistoryFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return ctx.getString(R.string.chart);
            case 1:
                return ctx.getString(R.string.history);
        }
        return null;
    }

    public Fragment getActiveFragment(ViewPager container, int position) {
        String name = makeFragmentName(container.getId(), position);
        return  ctx.getSupportFragmentManager().findFragmentByTag(name);
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
}
