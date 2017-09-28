package com.sample.microsoft.stt.poc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.ui.DocumentsListFragment;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MicrosoftLandingActivity extends AppCompatActivity {

    private CognitiveServicesHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoft_landing);

        getSupportFragmentManager().findFragmentById(R.id.frame_container);
        setFragment(new DocumentsListFragment());
    }

    public void initialiseCognitiveServices() {
        // Initialise Cognitive Services
        mHelper = new CognitiveServicesHelper();
        String language = "en-us";
        String subscriptionKey = this.getString(R.string.primaryKey);
        mHelper.initializeRecoClient(this, language, subscriptionKey, SpeechRecognitionMode.LongDictation);
    }

    public CognitiveServicesHelper getSpeechHelper() {
        return mHelper;
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, fragment, fragment.getClass().getName());
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.commit();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void enableBackButton() {
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void disableBackButton() {
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            manager.popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
