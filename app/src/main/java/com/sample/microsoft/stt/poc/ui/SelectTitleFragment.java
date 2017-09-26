package com.sample.microsoft.stt.poc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;

/**
 * Created by sgarimella on 26/09/17.
 */

public class SelectTitleFragment extends BaseFragment implements View.OnClickListener {
    private EditText mInput;
    private TextInputLayout mInputLayout;

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
        mInput = view.findViewById(R.id.input_title);
        mInputLayout = view.findViewById(R.id.input_layout_title);

        //set previous set data
        mInput.setText(((POCApplication) getActivity().getApplication()).getTitle());

        mInput.setFocusable(true);
        showKeyboard();
        mInputLayout.setError("");
        view.findViewById(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next:
                String title = mInput.getText().toString();
                if (!TextUtils.isEmpty(title)) {
                    hideKeyboard(mInput);
                    ((POCApplication) getActivity().getApplication()).setTitle(title);
                    ((MicrosoftLandingActivity) getActivity()).setFragment(new DictationFragment());
                } else {
                    mInputLayout.setError("Please set document title.");
                }
                break;
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyboard(EditText view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard(mInput);
    }
}
