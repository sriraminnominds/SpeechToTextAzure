package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;
import com.sample.microsoft.stt.poc.ui.custom.CircularSeekBar;

/**
 * Created by sgarimella on 25/09/17.
 */

public class SelectTimeFragment extends BaseFragment implements View.OnClickListener, CircularSeekBar.OnCircularSeekBarChangeListener {
    private TextView mProgressView;
    private CircularSeekBar mSeekbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_time, null);
        initialiseViews(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.select_time));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    private void initialiseViews(View view) {
        mProgressView = view.findViewById(R.id.progress);
        mSeekbar = view.findViewById(R.id.seekbar);

        view.findViewById(R.id.next).setOnClickListener(this);
        mSeekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next:
                ((POCApplication) getActivity().getApplication()).setRecordTime(mSeekbar.getProgress());
                ((MicrosoftLandingActivity) getActivity()).setFragment(new SelectTitleFragment());
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

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        mProgressView.setText(String.valueOf(progress));
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {

    }
}
