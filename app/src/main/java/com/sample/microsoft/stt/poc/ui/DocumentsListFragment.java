package com.sample.microsoft.stt.poc.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;
import com.sample.microsoft.stt.poc.data.Record;
import com.sample.microsoft.stt.poc.ui.custom.TextViewWithImages;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by sgarimella on 25/09/17.
 */

public class DocumentsListFragment extends BaseFragment {
    private final String TAG = "DocumentsListFragment";
    private List<Record> mFiles = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DocumentsListAdapter mListAdapter;
    private TextView mEmptyList;
    private Dialog mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_documents_list, null);
        initialiseViews(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.my_notes));
        getFilesFromDir();
    }

    private void initialiseViews(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialog == null || !mDialog.isShowing()) {
                    showInstructionsDialog();
                }
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mListAdapter = new DocumentsListAdapter(mFiles);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mListAdapter);

        mEmptyList = view.findViewById(R.id.no_list);
        if (mFiles.isEmpty()) {
            mEmptyList.setVisibility(View.VISIBLE);
        } else {
            mEmptyList.setVisibility(View.GONE);
        }
    }

    private void getFilesFromDir() {
        mFiles.clear();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Notes";
        File directory = new File(path);
        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    Date lastModDate = new Date(file.lastModified());
                    Record r = new Record(file.getName(), file.getPath(), lastModDate);
                    Log.v(TAG, r.toString());
                    mFiles.add(r);
                }
                Collections.sort(mFiles, Collections.reverseOrder());
            }
        }
        if (mFiles.isEmpty()) {
            mEmptyList.setVisibility(View.VISIBLE);
        } else {
            mEmptyList.setVisibility(View.GONE);
        }
    }

    public class DocumentsListAdapter extends RecyclerView.Adapter<DocumentsListAdapter.DocumentsListHolder> {
        private List<Record> recordsList;

        public class DocumentsListHolder extends RecyclerView.ViewHolder {
            public TextView name, date, pinnedDate;
            public ImageView more;

            public DocumentsListHolder(View view) {
                super(view);
                name = view.findViewById(R.id.name);
                date = view.findViewById(R.id.date);
                pinnedDate = view.findViewById(R.id.date_pinned);
                more = view.findViewById(R.id.more);
            }
        }

        public DocumentsListAdapter(List<Record> DocumentsListAdapter) {
            this.recordsList = DocumentsListAdapter;
        }

        public void setList(List<Record> list) {
            this.recordsList = list;
        }

        @Override
        public DocumentsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_documents_list_item, parent, false);
            return new DocumentsListHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DocumentsListHolder holder, int position) {
            final Record record = getItem(position);
            if (record != null) {
                Record prevRecord = getItem(Math.max(0, position - 1));
                if ((position == 0) || !isMessageInSameDate(record, prevRecord)) {
                    holder.pinnedDate.setVisibility(View.VISIBLE);
                    SimpleDateFormat df2 = new SimpleDateFormat("MMMM dd, yyyy");
                    holder.pinnedDate.setText(df2.format(record.getLastModifiedDate()));
                } else {
                    holder.pinnedDate.setVisibility(View.GONE);
                }

                holder.name.setText(formatStringToCaps(record.getRecordName()));
                DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                holder.date.setText(dateFormat.format(record.getLastModifiedDate()));

                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOptionsMenu(v, record);
                    }
                });
            }
        }

        public Record getItem(int position) {
            return recordsList.get(position);
        }

        @Override
        public int getItemCount() {
            return recordsList.size();
        }
    }

    public static String formatStringToCaps(String str) {
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            int len = s.length();
            if (len > 1) {
                String cap = s.substring(0, 1).toUpperCase();
                builder.append(cap);
                String rest = s.substring(1, len).toLowerCase();
                builder.append(rest);
                builder.append(" ");
            } else {
                builder.append(s.toUpperCase());
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    public boolean isMessageInSameDate(Record rec1, Record rec2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(rec1.getLastModifiedDate());
        cal2.setTime(rec2.getLastModifiedDate());
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    private void showOptionsMenu(View view, final Record record) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.inflate(R.menu.document_list_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_view:
                        viewFile(record.getPath());
                        break;
                    case R.id.action_delete:
                        deleteFile(record.getPath());
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    private void deleteFile(String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        getFilesFromDir();
        mListAdapter.setList(mFiles);
        mListAdapter.notifyDataSetChanged();
    }

    private void viewFile(String path) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    private void showInstructionsDialog() {
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
                ((POCApplication) getActivity().getApplication()).clear();
                ((POCApplication) getActivity().getApplication()).setRecordTime(5);
                ((MicrosoftLandingActivity) getActivity()).setFragment(new MeetingNotesFragment());
            }
        });
    }
}
