package com.sample.microsoft.stt.poc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.ui.DictationFragment;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MicrosoftLandingActivity extends FragmentActivity {

    private CognitiveServicesHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoft_landing);


        // Initialise Cognitive Services
        mHelper = new CognitiveServicesHelper();
        String language = "en-us";
        String subscriptionKey = this.getString(R.string.primaryKey);
        mHelper.initializeRecoClient(this, language, subscriptionKey, SpeechRecognitionMode.LongDictation);

        getSupportFragmentManager().findFragmentById(R.id.frame_container);
        setFragment(new DictationFragment());
    }

    public CognitiveServicesHelper getSpeechHelper() {
        return mHelper;
    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
}
