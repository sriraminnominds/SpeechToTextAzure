package com.sample.microsoft.stt.poc.ui;

import android.app.Dialog;
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
import com.sample.microsoft.stt.poc.ui.custom.TextViewWithImages;

/**
 * Created by sgarimella on 25/09/17.
 */

public class SelectModeFragment extends BaseFragment implements View.OnClickListener {
    private Dialog mDialog;

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
        view.findViewById(R.id.mode_meeting_notes).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mode_dictation:
                if (mDialog == null || !mDialog.isShowing()) {
                    showDictationInstructionsDialog();
                }
                break;
            case R.id.mode_meeting_notes:
                ((MicrosoftLandingActivity) getActivity()).setFragment(new MeetingNotesFragment());
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

    private void showDictationInstructionsDialog() {
        mDialog = new Dialog(getActivity());
        mDialog.setContentView(R.layout.view_instructions_dialog);
        mDialog.setTitle("Instructions");
        TextViewWithImages text = (TextViewWithImages) mDialog.findViewById(R.id.dialog_message);
        text.setText(getString(R.string.instructions));
        mDialog.show();

        TextView acceptButton = (TextView) mDialog.findViewById(R.id.dialog_ok);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                mDialog.dismiss();
                ((POCApplication) getActivity().getApplication()).clearDictation();
                ((POCApplication) getActivity().getApplication()).setRecordTime(5);
                ((MicrosoftLandingActivity) getActivity()).setFragment(new DictationFragment());
            }
        });
    }
}
