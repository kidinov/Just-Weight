package org.kidinov.justweight.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.kidinov.justweight.R;
import org.kidinov.justweight.model.Weight;
import org.kidinov.justweight.util.EnvUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akid on 17/04/15.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "HistoryAdapter";
    private final List<Weight> weights;
    private Context ctx;
    private LayoutInflater li;
    private RecyclerView rv;

    public HistoryAdapter(RecyclerView rv, List<Weight> weights) {
        this.ctx = rv.getContext();
        this.rv = rv;
        this.weights = weights;
        li = LayoutInflater.from(ctx);
    }

    public void addWieght(Weight w, int pos) {
        weights.add(w);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(li.inflate(R.layout.history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int index) {
        ViewHolder h = holder;
        h.bind(index);
    }

    @Override
    public int getItemCount() {
        return weights.size();
    }

    public void remove(Weight w) {
        notifyItemRemoved(weights.indexOf(w));
        w.delete();
        weights.remove(w);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView value;
        private final TextView unit;
        private final ImageView delete;
        private View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;

            date = ((TextView) v.findViewById(R.id.date));
            value = (TextView) v.findViewById(R.id.value);
            unit = (TextView) v.findViewById(R.id.unit);
            delete = (ImageView) v.findViewById(R.id.delete);

        }

        public void bind(int index) {
            Weight w = weights.get(index);
            date.setText(EnvUtil.getFormattedDate(w.getDate()));
            value.setText(EnvUtil.formatWeight(EnvUtil.convertFromKg(ctx, w.getValue())));
            unit.setText(EnvUtil.getLocalUnitString(ctx));
            delete.setOnClickListener(view -> {
                Log.d(TAG, String.format("delete = %d", index));
                remove(w);
            });
        }
    }

}
