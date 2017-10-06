package com.sample.microsoft.stt.poc.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.utils.AppUtils;

/**
 * Created by sgarimella on 26/09/17.
 */

public class PdfGeneratorFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "PdfGeneratorFragment";
    private TextView mRecordedView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_generate_pdf, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        String text = ((MicrosoftLandingActivity) getActivity()).getData().getRecordedText();
        mRecordedView = ((TextView) view.findViewById(R.id.recordeddata));
        mRecordedView.setText(text);

        view.findViewById(R.id.save).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.pdf_generator));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if (isStoragePermissionGranted()) {
                    String title = ((MicrosoftLandingActivity) getActivity()).getData().getTitle();
                    AppUtils.writeToPdf(getActivity(), title, mRecordedView);
                    ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
                }
                break;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            String title = ((MicrosoftLandingActivity) getActivity()).getData().getTitle();
            AppUtils.writeToPdf(getActivity(), title, mRecordedView);
        }
    }
}
