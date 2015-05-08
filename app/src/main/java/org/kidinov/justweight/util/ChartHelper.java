package org.kidinov.justweight.util;

import android.animation.TimeInterpolator;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.ChartEntry;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

import org.kidinov.justweight.R;
import org.kidinov.justweight.activity.MainActivity;
import org.kidinov.justweight.model.Weight;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by akid on 17/04/15.
 */
public class ChartHelper {
    private static final String TAG = "ChartHelper";
    private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
    private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();

    private TextView lineTooltip;
    private MainActivity ctx;
    private LineChartView chart;
    private LineSet dataSet;
    private Animation animation;

    public ChartHelper(MainActivity ctx, LineChartView chart) {
        this.ctx = ctx;
        this.chart = chart;
    }

    public void setupChart(LineSet result) {
        Paint mLineGridPaint = new Paint();
        mLineGridPaint.setColor(ctx.getResources().getColor(R.color.divider));
        mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{2, 5}, 0));
        mLineGridPaint.setStyle(Paint.Style.STROKE);
        mLineGridPaint.setAntiAlias(true);
        mLineGridPaint.setStrokeWidth(1f);

        chart.setBorderSpacing(Tools.fromDpToPx(4)).setGrid(LineChartView.GridType.FULL, mLineGridPaint).setXAxis(false);
        chart.setXLabels(XController.LabelPosition.OUTSIDE).setYAxis(false).setYLabels(YController.LabelPosition.OUTSIDE);

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (ChartEntry entry : result.getEntries()) {
            if (entry.getValue() < min) {
                min = entry.getValue();
            }
            if (entry.getValue() > max) {
                max = entry.getValue();
            }
        }

        chart.setAxisBorderValues((int) min - 2, (int) max + 2, 1);

        chart.setOnEntryClickListener(lineEntryListener);
        chart.setOnClickListener(lineClickListener);
    }

    private void scaleResult(List<Weight> input) {
        List<Weight> res = new ArrayList<>();
        if (input.size() > 25) {
            for (int i = 0; i < input.size() - 1; i++) {
                if (i % 2 == 0) {
                    res.add(new Weight(input.get(i).getDate(), input.get(i).getValue(), input.get(i).getUnit()));
                }
            }
            res.add(input.get(input.size() - 1));
            input.clear();
            input.addAll(res);
            scaleResult(input);
        }
    }

    private LineSet getData(List<Weight> weights) {
        if (weights == null || weights.isEmpty()) {
            return null;
        }

        scaleResult(weights);

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Float> yVals = new ArrayList<>();
        for (Weight w : weights) {
            xVals.add(EnvUtil.getFormattedDateForGraph(w.getDate()));
            yVals.add(Float.parseFloat(EnvUtil.formatWeight(EnvUtil.convertFromKg(ctx, w.getValue()))));
        }

        dataSet = new LineSet();
        dataSet.addPoints(xVals.toArray(new String[0]), ArrayUtil.toPrimitive(yVals.toArray(new Float[0]), 0.0F));
        dataSet.setDots(true).setDotsColor(ctx.getResources().getColor(R.color.icons)).setDotsRadius(Tools.fromDpToPx(4))
                .setDotsStrokeThickness(Tools.fromDpToPx(3)).setDotsStrokeColor(ctx.getResources().getColor(R.color.accent))
                .setLineColor(ctx.getResources().getColor(R.color.accent)).setLineThickness(Tools.fromDpToPx(3)).setLineSmooth(true);

        return dataSet;
    }

    public void redrawGraph(Date from, Date to, Runnable r) {
        ChartDataGetter chartDataGeter = new ChartDataGetter(r);
        chartDataGeter.execute(from.getTime(), to.getTime());
    }

    private final OnEntryClickListener lineEntryListener = new OnEntryClickListener() {
        @Override
        public void onClick(int setIndex, int entryIndex, Rect rect) {
            if (lineTooltip == null) {
                showLineTooltip(setIndex, entryIndex, rect);
            } else {
                dismissLineTooltip(setIndex, entryIndex, rect);
            }
        }
    };

    private final View.OnClickListener lineClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lineTooltip != null) dismissLineTooltip(-1, -1, null);
        }
    };

    private void showLineTooltip(int setIndex, int entryIndex, Rect rect) {
        lineTooltip = (TextView) LayoutInflater.from(ctx).inflate(R.layout.circular_tooltip, null);
        lineTooltip.setText(String.valueOf(dataSet.getEntry(entryIndex).getValue()));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
        layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
        layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
        lineTooltip.setLayoutParams(layoutParams);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            lineTooltip.setPivotX(layoutParams.width / 2);
            lineTooltip.setPivotY(layoutParams.height / 2);
            lineTooltip.setAlpha(0);
            lineTooltip.setScaleX(0);
            lineTooltip.setScaleY(0);
            lineTooltip.animate().setDuration(150).alpha(1).scaleX(1).scaleY(1).rotation(360).setInterpolator(enterInterpolator);
        }

        chart.showTooltip(lineTooltip);

        new Handler().postDelayed(() -> {
            dismissLineTooltip(setIndex, -1, rect);
        }, 1000);
    }

    private void dismissLineTooltip(final int setIndex, final int entryIndex, final Rect rect) {
        if (lineTooltip == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            lineTooltip.animate().setDuration(150).scaleX(0).scaleY(0).setInterpolator(exitInterpolator).withEndAction(() -> {
                chart.removeView(lineTooltip);
                lineTooltip = null;
                if (entryIndex != -1) showLineTooltip(setIndex, entryIndex, rect);
            });
        } else {
            chart.dismissTooltip(lineTooltip);
            lineTooltip = null;
            if (entryIndex != -1) showLineTooltip(setIndex, entryIndex, rect);
        }
    }

    class ChartDataGetter extends AsyncTask<Long, Void, LineSet> {

        private Runnable endAction;

        public ChartDataGetter(Runnable r) {
            this.endAction = r;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (ctx != null && ctx.findViewById(R.id.chart_progress) != null) {
                ctx.findViewById(R.id.chart_progress).setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected LineSet doInBackground(Long... params) {
            return getData(DbHelper.getRecordsBetweenDates(params[0], params[1]));
        }

        @Override
        protected void onPostExecute(LineSet result) {
            super.onPostExecute(result);
            if (ctx != null && ctx.findViewById(R.id.chart_progress) != null) {
                new Handler().postDelayed(() -> {
                    if (ctx == null) {
                        return;
                    }
                    try {
                        ctx.findViewById(R.id.chart_progress).setVisibility(View.GONE);
                        if (result == null) {
                            if (ctx.findViewById(R.id.empty) != null) ctx.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            endAction.run();
                            return;
                        } else {
                            ctx.findViewById(R.id.empty).setVisibility(View.GONE);
                        }

                        chart.addData(result);
                        setupChart(result);
                        chart.show(getAnimation(result.size(), endAction));
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }, 1000);
            }
        }
    }

    int mCurrOverlapFactor = 1;
    BaseEasingMethod mCurrEasing = new QuintEaseOut();
    int mCurrStartX = -1;
    int mCurrStartY = 0;
    int mCurrAlpha = -1;

    public Animation getAnimation(int size, Runnable endAction) {
        int[] overlapOrder = new int[size + 1];
        for (int i = 0; i <= size; i++) {
            overlapOrder[i] = i;
        }
        if (animation == null) {
            animation = new Animation().setAlpha(mCurrAlpha).setEasing(mCurrEasing).setOverlap(mCurrOverlapFactor, overlapOrder)
                    .setStartPoint(mCurrStartX, mCurrStartY);
        } else {
            animation.setOverlap(mCurrOverlapFactor, overlapOrder);
        }

        animation.setEndAction(endAction);

        return animation;
    }

}
