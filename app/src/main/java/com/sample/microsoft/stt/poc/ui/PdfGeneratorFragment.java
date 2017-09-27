package com.sample.microsoft.stt.poc.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        String text = ((POCApplication) getActivity().getApplication()).getRecordedText();
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
                    writeToPdf();
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
            writeToPdf();
        }
    }

    private void writeToPdf() {
        FileOutputStream fOut = null;
        try {
            String title = ((POCApplication) getActivity().getApplication()).getTitle();
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Notes";
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String fileName = title + ".pdf";
            final File file = new File(path, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            PdfDocument document = new PdfDocument();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            mRecordedView.draw(page.getCanvas());

            document.finishPage(page);
            document.writeTo(fOut);
            document.close();
        } catch (IOException e) {
            Log.i("error", e.getLocalizedMessage());
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
