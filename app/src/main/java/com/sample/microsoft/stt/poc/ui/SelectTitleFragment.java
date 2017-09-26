package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;

/**
 * Created by sgarimella on 26/09/17.
 */

public class SelectTitleFragment extends BaseFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_title, null);
        initialiseViews(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.select_title));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    private void initialiseViews(View view) {
        view.findViewById(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next:
                ((MicrosoftLandingActivity) getActivity()).setFragment(new DictationFragment());
                break;
        }
    }
}
