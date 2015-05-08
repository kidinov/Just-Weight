package org.kidinov.justweight.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.kidinov.justweight.R;
import org.kidinov.justweight.util.EnvUtil;

/**
 * Created by akid on 17/04/15.
 */
public class WeightPickerAdapter extends RecyclerView.Adapter {

    private Context ctx;
    private LayoutInflater li;

    public WeightPickerAdapter(Context ctx) {
        this.ctx = ctx;
        li = LayoutInflater.from(ctx);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(li.inflate(R.layout.weight_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int index) {
        ViewHolder h = (ViewHolder) holder;
        h.bind(index);
    }

    @Override
    public int getItemCount() {
        return 5000;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout view;
        private TextView textView;
        private View notch;

        public ViewHolder(View v) {
            super(v);
            view = (RelativeLayout) v;
            textView = ((TextView) view.findViewById(R.id.text));
            notch = view.findViewById(R.id.notch);
        }

        public void bind(int index) {
            if (index % 10 != 0) {
                textView.setText(EnvUtil.formatWeightOnlyDec(index));
                textView.setTextSize(10);
                textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                textView.setTextColor(notch.getResources().getColor(R.color.secondary_text));
                notch.setVisibility(View.VISIBLE);
            } else {
                textView.setText(String.valueOf(index / 10));
                textView.setTextSize(35);
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(notch.getResources().getColor(R.color.primary_text));
                notch.setVisibility(View.INVISIBLE);
            }
        }
    }
}
