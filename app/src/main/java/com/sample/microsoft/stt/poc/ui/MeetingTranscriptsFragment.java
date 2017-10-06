package com.sample.microsoft.stt.poc.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.MeetingNotes;
import com.sample.microsoft.stt.poc.data.POCApplication;
import com.sample.microsoft.stt.poc.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgarimella on 05/10/17.
 */

public class MeetingTranscriptsFragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout mLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meeting_transcript, null);
        initialiseViews(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.transcripts));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    private void initialiseViews(View v) {
        mLayout = v.findViewById(R.id.transcript_layout);
        RecyclerView list = v.findViewById(R.id.transcript_list);
        TranscriptListAdapter adapter = new TranscriptListAdapter(((POCApplication) getActivity().getApplication()).getNotes());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(adapter);

        View heading = v.findViewById(R.id.transcript_heading);
        ((TextView) heading.findViewById(R.id.transcript_author)).setText("AUTHOR");
        ((TextView) heading.findViewById(R.id.transcript_date)).setText("DATE");
        ((TextView) heading.findViewById(R.id.transcript_message)).setText("MESSAGE");

        v.findViewById(R.id.transcript_generate).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.transcript_generate:
                String title = ((POCApplication) getActivity().getApplication()).getTitle();
                if (TextUtils.isEmpty(title)) {
                    showTitleDialog();
                } else {
                    if (isStoragePermissionGranted()) {
                        writeToFile();
                    }
                }
                break;
        }
    }

    private void showTitleDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.view_title_dialog);
        final TextInputLayout layout = (TextInputLayout) dialog.findViewById(R.id.input_layout_title);
        final EditText text = dialog.findViewById(R.id.input_title);
        dialog.show();

        TextView acceptButton = (TextView) dialog.findViewById(R.id.next);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = text.getText().toString();
                if (!TextUtils.isEmpty(title)) {
                    dialog.dismiss();
                    ((POCApplication) getActivity().getApplication()).setTitle(title);
                    if (isStoragePermissionGranted()) {
                        writeToFile();
                    }
                } else {
                    layout.setError("Please enter title of document");
                }
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            writeToFile();
        }
    }

    private void writeToFile(){
        String title = ((POCApplication) getActivity().getApplication()).getTitle();
        AppUtils.writeToPdf(getActivity(), title, mLayout);
        ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
    }

    public class TranscriptListAdapter extends RecyclerView.Adapter<TranscriptListAdapter.RestaurantViewHolder> {
        private List<MeetingNotes> notes = new ArrayList<>();

        public TranscriptListAdapter(List<MeetingNotes> notes) {
            this.notes = notes;
        }

        @Override
        public TranscriptListAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_meeting_transcript_item, parent, false);
            RestaurantViewHolder viewHolder = new RestaurantViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(TranscriptListAdapter.RestaurantViewHolder holder, int position) {
            holder.bindRestaurant(notes.get(position));
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        public class RestaurantViewHolder extends RecyclerView.ViewHolder {
            private TextView date;
            private TextView author;
            private TextView message;

            public RestaurantViewHolder(View view) {
                super(view);
                date = (TextView) view.findViewById(R.id.transcript_date);
                author = (TextView) view.findViewById(R.id.transcript_author);
                message = (TextView) view.findViewById(R.id.transcript_message);
            }

            public void bindRestaurant(MeetingNotes notes) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String shortTimeStr = sdf.format(notes.getDate());
                date.setText(shortTimeStr);
                author.setText(notes.getUserName());
                message.setText(notes.getMsg());
            }
        }
    }
}
