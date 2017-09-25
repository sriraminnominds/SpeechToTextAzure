package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;

/**
 * Created by sgarimella on 25/09/17.
 */

public class SelectModeFragment extends BaseFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_mode, null);
        initialiseViews(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.select_mode));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    private void initialiseViews(View view) {
        view.findViewById(R.id.mode_dictation).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mode_dictation:
                ((MicrosoftLandingActivity) getActivity()).setFragment(new SelectTimeFragment());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
