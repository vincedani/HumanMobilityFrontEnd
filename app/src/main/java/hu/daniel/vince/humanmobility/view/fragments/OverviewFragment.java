package hu.daniel.vince.humanmobility.view.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.entities.Overview;
import hu.daniel.vince.humanmobility.view.activity.MaterialProgressBar;
import hu.daniel.vince.humanmobility.view.adapter.OverviewAdapter;
import hu.daniel.vince.humanmobility.view.adapter.item.OverviewGroupItem;
import hu.daniel.vince.humanmobility.view.animation.AnimatorListener;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 20.
 */

public class OverviewFragment extends android.support.v4.app.Fragment {

    // region Members

    private boolean loaded;


    // endregion

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, null);

        if (getActivity() != null)
            initListView(view);

        return view;
    }

    private void initListView(View view) {
        final MaterialProgressBar progressBar =
                (MaterialProgressBar) view.findViewById(R.id.progress);
        final TextView noDataTextView = (
                TextView) view.findViewById(R.id.no_data_text);
        final ExpandableListView listView =
                (ExpandableListView) view.findViewById(R.id.overview_list);

        noDataTextView.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        loaded = false;

        DatabaseHandler.getInstance(getActivity()).clearCache();
        List<Overview> overViews = DatabaseHandler.getInstance(getActivity()).getOverviews();

        if (getActivity() == null)
            return;

        final List<OverviewGroupItem> result = new ArrayList<>();

        List<Overview> overviews = DatabaseHandler.getInstance(getActivity()).getOverviews();
        long positive = 0;
        long error = 0;

        for (Overview ov : overviews) {
            positive += ov.getSuccess();
            error += ov.getError();
        }

        // header -- TOTAL
        result.add(new OverviewGroupItem(null, null, positive, error, true));

        // days
        for (Overview overview : overViews) {
            final DateTime currentDay = overview.getDate();
            final long currentErrors = overview.getError();
            final long currentCount = overview.getSuccess();

            if (currentCount > 0) {
                result.add(new OverviewGroupItem(currentDay, null, currentCount, currentErrors, false));
            }
        }

        final OverviewAdapter overviewAdapter =
                new OverviewAdapter(getActivity(), result);

        getActivity().runOnUiThread(() -> {
            if(overViews == null || overViews.isEmpty())
                replaceViews(progressBar, noDataTextView);

            else {
                listView.setAdapter(overviewAdapter);
                replaceViews(progressBar, listView);
            }
        });
    }

    private void replaceViews(final View showedView, View hidedView) {
        hidedView.setAlpha(0f);
        hidedView.setVisibility(View.VISIBLE);

        final ObjectAnimator show = ObjectAnimator.ofFloat(hidedView, "alpha", 1f);
        show.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loaded = true;
            }
        });
        show.setDuration(300);

        final ObjectAnimator hide = ObjectAnimator.ofFloat(showedView, "alpha", 0f);
        hide.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showedView.setVisibility(View.GONE);
                show.start();
            }
        });
        hide.setDuration(300);
        hide.start();
    }

    public boolean isLoaded() {
        return loaded;
    }

}
