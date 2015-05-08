package org.kidinov.justweight.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.kidinov.justweight.R;
import org.kidinov.justweight.adapter.HistoryAdapter;
import org.kidinov.justweight.model.Weight;
import org.kidinov.justweight.util.DbHelper;
import org.kidinov.justweight.util.EnvUtil;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class HistoryFragment extends BaseFragment {
    private static final String TAG = "HistoryFragment";
    private View rootView;
    private RecyclerView historyRv;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        initView(rootView, savedInstanceState);

        historyRv = (RecyclerView) rootView.findViewById(R.id.history);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        historyRv.setLayoutManager(llm);

        Long from = EnvUtil.getLocalFromString(fromDate.getText().toString()).getTime();
        Long to = EnvUtil.getLocalFromString(toDate.getText().toString()).getTime();


        new HistoryTask().execute(from, to);

        return rootView;
    }

    private class HistoryTask extends AsyncTask<Long, Void, List<Weight>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (rootView != null && rootView.findViewById(R.id.progress) != null) {
                rootView.findViewById(R.id.progress).setVisibility(View.VISIBLE);

                HistoryAdapter historyAdapter = new HistoryAdapter(historyRv, new ArrayList<>());
                historyRv.setItemAnimator(new SlideInLeftAnimator());
                historyRv.setAdapter(historyAdapter);
                rootView.findViewById(R.id.history).setVisibility(View.GONE);
            }
        }


        @Override
        protected List<Weight> doInBackground(Long... longs) {
            return DbHelper.getRecordsBetweenDatesDesc(longs[0], longs[1]);
        }

        @Override
        protected void onPostExecute(List<Weight> result) {
            super.onPostExecute(result);
            if (rootView != null && rootView.findViewById(R.id.progress) != null) {

                new Handler().postDelayed(() -> {
                    rootView.findViewById(R.id.progress).setVisibility(View.GONE);
                    if (result == null || result.isEmpty()) {
                        rootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        historyRv.setAdapter(new HistoryAdapter(historyRv, new ArrayList<>()));
                        return;
                    } else {
                        rootView.findViewById(R.id.empty).setVisibility(View.GONE);
                    }

                    HistoryAdapter historyAdapter = new HistoryAdapter(historyRv, result);
                    historyRv.setItemAnimator(new SlideInLeftAnimator());
                    historyRv.setAdapter(historyAdapter);

                    rootView.findViewById(R.id.history).setVisibility(View.VISIBLE);
                    if (getActivity() != null) {
                        Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                        rootView.findViewById(R.id.history).startAnimation(fadeInAnimation);
                    }
                }, 1000);
            }
        }
    }

    @Override
    public void updateData() {
        new HistoryTask().execute(EnvUtil.getLocalFromString(fromDate.getText().toString()).getTime(),
                EnvUtil.getLocalFromString(toDate.getText().toString()).getTime());
    }
}
