package com.sample.microsoft.stt.poc.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;
import com.sample.microsoft.stt.poc.data.SemanticError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by sgarimella on 26/09/17.
 */

public class SemanticsValidationFragment extends BaseFragment implements View.OnClickListener {
    private final String TAG = "SemanticsValidationFragment";
    private TextView mRecordedView;

    private final String API_END_POINT = "https://languagetool.org/api/v2/check";
    private OkHttpClient mClient = new OkHttpClient();

    private List<SemanticError> mErrors = new ArrayList<>();
    private String mSemanticText;

    private PopupWindow mPopupWindow;
    private SpannableString mErrorSpannable;
    private View mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_semantics_validation, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        mProgressBar = view.findViewById(R.id.loading_progress_bar);

        mSemanticText = ((POCApplication) getActivity().getApplication()).getRecordedText();
        mRecordedView = ((TextView) view.findViewById(R.id.recordeddata));
        mRecordedView.setText(mSemanticText);

        view.findViewById(R.id.icontinue).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.check_errors));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();

        checkForErrors();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.icontinue:
                if ((mPopupWindow != null) && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                String title = ((POCApplication) getActivity().getApplication()).getTitle();
                if (TextUtils.isEmpty(title)) {
                    showTitleDialog();
                } else {
                    if (isStoragePermissionGranted()) {
                        writeToPdf();
                        ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
                    }
                }
                break;
            case R.id.recordeddata:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ((mPopupWindow != null) && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    private void showErrorSpans() {
        String text = ((POCApplication) getActivity().getApplication()).getRecordedText();
        mErrorSpannable = new SpannableString(text);
        for (SemanticError error : mErrors) {
            mErrorSpannable.setSpan(new ErrorClickableSpan(this, error), error.getOffset(), error.getOffset() + error.getLength(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mRecordedView.setText(mErrorSpannable);
        mRecordedView.setMovementMethod(LinkMovementMethod.getInstance());
        mRecordedView.setHighlightColor(Color.YELLOW);
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
                        writeToPdf();
                        ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
                    }
                } else {
                    layout.setError("Please enter title of document");
                }
            }
        });

    }

    private static class ErrorClickableSpan extends ClickableSpan {
        private SemanticsValidationFragment fragment;
        private SemanticError error;

        public ErrorClickableSpan(SemanticsValidationFragment fragment, SemanticError error) {
            this.fragment = fragment;
            this.error = error;
        }

        @Override
        public void onClick(View view) {
            fragment.showSuggestions(view, error);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    private void showSuggestions(View view, final SemanticError error) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.view_semantic_options, null);
        mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        });

        TextView heading = (TextView) customView.findViewById(R.id.options_heading);
        heading.setText(error.getMessage());

        ListView optionsList = customView.findViewById(R.id.options_list);
        String[] options = new String[error.getOptions().size()];
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, error.getOptions().toArray(options));
        optionsList.setAdapter(itemsAdapter);
        optionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
                String sub = mSemanticText.substring(error.getOffset(), error.getOffset() + error.getLength());
                mSemanticText = mSemanticText.replace(sub, error.getOptions().get(position));
                ((POCApplication) getActivity().getApplication()).setRecordedText(mSemanticText);
                checkForErrors();
            }
        });

        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


    private void checkForErrors() {
        mProgressBar.setVisibility(View.VISIBLE);
        mSemanticText = ((POCApplication) getActivity().getApplication()).getRecordedText();
        ;
        StringBuilder payload = new StringBuilder();
        payload.append("disabledRules=WHITESPACE_RULE&allowIncompleteResults=true&text=");
        payload.append(mSemanticText);
        payload.append("&language=en-US");

        MediaType json = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, payload.toString());

        Request request = new Request.Builder()
                .url(API_END_POINT)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        getOkHttpCall(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        mErrors.clear();
                        String jsonData = response.body().string();
                        JSONObject jResponse = new JSONObject(jsonData);
                        JSONArray jMatches = jResponse.getJSONArray("matches");
                        for (int i = 0; i < jMatches.length(); i++) {
                            JSONObject jMessage = jMatches.getJSONObject(i);
                            int offset = jMessage.getInt("offset");
                            int length = jMessage.getInt("length");
                            String message = jMessage.getString("message");
                            JSONArray jReplacements = jMessage.getJSONArray("replacements");
                            List<String> suggestions = new ArrayList<String>();
                            for (int j = 0; j < jReplacements.length(); j++) {
                                JSONObject jSuggestion = jReplacements.getJSONObject(j);
                                String value = jSuggestion.getString("value");
                                suggestions.add(value);
                            }
                            SemanticError error = new SemanticError(message, suggestions, offset, length);
                            mErrors.add(error);
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    showErrorSpans();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Prepare the OKHttpClient
     */
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    /**
     * Prepare the OKHttpCall
     */
    private Call getOkHttpCall(Request req, Callback callback) {
        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(req);
        if (callback != null) {
            call.enqueue(callback);
        }
        return call;
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
            writeToPdf();
            ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
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
